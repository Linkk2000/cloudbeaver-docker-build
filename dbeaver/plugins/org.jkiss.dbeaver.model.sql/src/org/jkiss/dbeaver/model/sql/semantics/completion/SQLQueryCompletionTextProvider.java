/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2025 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.model.sql.semantics.completion;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.sql.SQLDialect;
import org.jkiss.dbeaver.model.sql.SQLTableAliasInsertMode;
import org.jkiss.dbeaver.model.sql.SQLUtils;
import org.jkiss.dbeaver.model.sql.completion.SQLCompletionAnalyzer;
import org.jkiss.dbeaver.model.sql.completion.SQLCompletionRequest;
import org.jkiss.dbeaver.model.sql.semantics.SQLQuerySymbol;
import org.jkiss.dbeaver.model.sql.semantics.SQLQuerySymbolEntry;
import org.jkiss.dbeaver.model.sql.semantics.completion.SQLQueryCompletionItem.*;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSObjectContainer;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureParameter;
import org.jkiss.utils.CommonUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SQLQueryCompletionTextProvider implements SQLQueryCompletionItemVisitor<String> {

    private static final Log log = Log.getLog(SQLQueryCompletionTextProvider.class);

    private final SQLCompletionRequest request;
    private final SQLQueryCompletionContext queryCompletionContext;
    private final SQLTableAliasInsertMode aliasMode;
    private final char structSeparator;
    private final Set<String> localKnownColumnNames;

    private final DBRProgressMonitor monitor;
    private final DBSObjectContainer activeContext;

    public SQLQueryCompletionTextProvider(
        @NotNull SQLCompletionRequest request,
        @NotNull SQLQueryCompletionContext queryCompletionContext,
        @NotNull DBRProgressMonitor monitor
    ) {
        this.request = request;
        this.queryCompletionContext = queryCompletionContext;
        this.aliasMode = SQLTableAliasInsertMode.fromPreferences(request.getContext().getSyntaxManager().getPreferenceStore());
        this.structSeparator = request.getContext().getDataSource().getSQLDialect().getStructSeparator();
        this.localKnownColumnNames = queryCompletionContext.getDataContext() == null
            ? Collections.emptySet()
            : queryCompletionContext.getDataContext().getColumnsList().stream()
                .map(c -> c.symbol.getName())
                .collect(Collectors.toSet());
        this.monitor = monitor;
        this.activeContext = request.getContext().getExecutionContext() == null
            ? null
            : DBUtils.getSelectedObject(request.getContext().getExecutionContext()) instanceof DBSObjectContainer c ? c : null;
    }

    @NotNull
    @Override
    public String visitSubqueryAlias(@NotNull SQLRowsSourceAliasCompletionItem subqueryAlias) {
        return this.prepareDefiningEntryName(subqueryAlias.symbol);
    }

    @NotNull
    public String visitCompositeField(@NotNull SQLCompositeFieldCompletionItem compositeField) {
        return compositeField.memberInfo.name();
    }

    @NotNull
    @Override
    public String visitColumnName(@NotNull SQLColumnNameCompletionItem columnName) {
        String preparedColumnName = this.convertCaseIfNeeded(columnName.columnInfo.symbol.getName());
//        String suffix;
//        if (this.queryCompletionContext.getInspectionResult().expectingColumnIntroduction() &&
//            this.aliasMode != SQLTableAliasInsertMode.NONE && this.localKnownColumnNames.contains(preparedColumnName) &&
//            columnName.sourceInfo != null && columnName.sourceInfo.aliasOrNull != null) {
//            DBPDataSource ds = this.request.getContext().getDataSource();
//            String alias = DBUtils.getUnQuotedIdentifier(ds, columnName.sourceInfo.aliasOrNull.getName())
//                + DBUtils.getUnQuotedIdentifier(ds, preparedColumnName);
//            suffix = this.prepareAliasPrefix() + this.convertCaseIfNeeded(DBUtils.getQuotedIdentifier(ds, alias));
//        } else {
//            suffix = "";
//        }

        String prefix;
        if (columnName.sourceInfo != null && this.queryCompletionContext.getInspectionResult().expectingColumnReference() && columnName.absolute) {
            boolean forceFullName = this.queryCompletionContext.isColumnNameConflicting(columnName.columnInfo.symbol.getName());
            if (this.request.getContext().isUseFQNames() || forceFullName) {
                if (columnName.sourceInfo.aliasOrNull != null) {
                    prefix = this.prepareDefiningEntryName(columnName.sourceInfo.aliasOrNull) + this.structSeparator;
                } else if (columnName.sourceInfo.tableOrNull != null) {
                    prefix = this.prepareObjectName(columnName.sourceInfo.tableOrNull) + this.structSeparator;
                } else {
                    prefix = "";
                }
            } else {
                prefix = "";
            }
        } else {
            prefix = "";
        }

        return prefix + preparedColumnName;
    }

    @NotNull
    @Override
    public String visitTableName(@NotNull SQLTableNameCompletionItem tableName) {
        String suffix;

        if (this.queryCompletionContext.getInspectionResult().expectingTableSourceIntroduction() &&
            this.aliasMode != SQLTableAliasInsertMode.NONE) {
            // It is table name completion after FROM. Auto-generate table alias
            SQLDialect sqlDialect = SQLUtils.getDialectFromObject(tableName.object);
            String alias = SQLUtils.generateEntityAlias(tableName.object,
                s -> sqlDialect.getKeywordType(s) != null ||
                    this.queryCompletionContext.getAliasesInUse().contains(s) ||
                    (this.queryCompletionContext.getDataContext() != null
                        && this.queryCompletionContext.getDataContext().resolveSource(monitor, List.of(s)) != null)
            );
            suffix = this.prepareAliasPrefix() + this.convertCaseIfNeeded(alias);
        } else {
            suffix = "";
        }

        return this.prepareObjectName(tableName) + suffix;
    }

    @NotNull
    private String prepareAliasPrefix() {
        return this.aliasMode == SQLTableAliasInsertMode.EXTENDED
            ? (" " + SQLCompletionAnalyzer.convertKeywordCase(this.request, "as", false) + " ") : " ";
    }

    @NotNull
    @Override
    public String visitReservedWord(@NotNull SQLReservedWordCompletionItem reservedWord) {
        return SQLCompletionAnalyzer.convertKeywordCase(this.request, reservedWord.text, false);
    }

    @NotNull
    @Override
    public String visitNamedObject(@NotNull SQLDbNamedObjectCompletionItem namedObject) {
        return this.prepareObjectName(namedObject);
    }

    @Nullable
    @Override
    public String visitJoinCondition(@NotNull SQLJoinConditionCompletionItem joinCondition) {
        return joinCondition.left.apply(this) + " = " + joinCondition.right.apply(this);
    }

    private String prepareObjectName(@NotNull SQLDbObjectCompletionItem<?> objectCompletionItem) {
        String name;
        if (objectCompletionItem.resolvedContext != null) {
            String accomplishedPart;
            if (objectCompletionItem.resolvedContext.preventFullName()) {
                accomplishedPart = this.convertCaseIfNeeded(DBUtils.getQuotedIdentifier(objectCompletionItem.getObject()));
            } else {
                accomplishedPart = this.prepareQualifiedName(objectCompletionItem.object, objectCompletionItem.resolvedContext.object());
            }
            name = objectCompletionItem.resolvedContext.string() + this.convertCaseIfNeeded(accomplishedPart);
        } else {
            name = this.prepareObjectName(objectCompletionItem.object);
        }
        return name;
    }

    @NotNull
    private <T extends DBSObject> String prepareObjectName(@NotNull DBSObject namedObject) {
        boolean forceFullName = !this.objectBelongsToTheActiveContext(namedObject) || this.activeContextHasConflictingName(namedObject);

        String shortName = DBUtils.getQuotedIdentifier(namedObject);
        String name;
        if (this.request.getContext().isUseShortNames() && !forceFullName) {
            name = shortName;
        } else if (this.request.getContext().isUseFQNames() || forceFullName) {
            name = DBUtils.getObjectFullName(namedObject, DBPEvaluationContext.DML);
            if (name.equals(shortName)) { // catalog name is not being included in full name for some reason sometimes
                name = this.prepareQualifiedName(namedObject, null);
            }
        } else {
            name = shortName;
        }
        return this.convertCaseIfNeeded(name);
    }

    private boolean objectBelongsToTheActiveContext(@NotNull DBSObject object) {
        return object.getParentObject() instanceof DBSObjectContainer objectContainer &&
            this.queryCompletionContext.getExposedContexts().contains(objectContainer);
    }

    private boolean activeContextHasConflictingName(@NotNull DBSObject object) {
        try {
            if (this.activeContext != null) {
                DBSObject child = activeContext.getChild(this.monitor, object.getName());
                return child != null && !child.equals(object);
            }
        } catch (DBException e) {
            log.debug("Failed to validate database object completion name ambiguity", e);
        }
        return false;
    }

    private String prepareQualifiedName(@NotNull DBSObject object, DBSObject knownSubroot) {
        List<String> parts = SQLQueryCompletionItem.prepareQualifiedNameParts(object, knownSubroot);
        return String.join(Character.toString(object.getDataSource().getSQLDialect().getStructSeparator()), parts);
    }

    @NotNull
    private String convertCaseIfNeeded(@NotNull String name) {
        String result;
        if (this.request.getWordDetector().isQuoted(name)) {
            result = name;
        } else {
            result = SQLCompletionAnalyzer.convertKeywordCase(this.request, name, true);
        }
        return result;
    }

    @NotNull
    private String prepareDefiningEntryName(@NotNull SQLQuerySymbol symbol) {
        return symbol.getDefinition() instanceof SQLQuerySymbolEntry entry ? entry.getRawName() : symbol.getName();
    }

    @NotNull
    @Override
    public String visitProcedure(@NotNull SQLProcedureCompletionItem procedure) {
        String name = this.prepareObjectName(procedure);

        try {
            String text;
            Collection<? extends DBSProcedureParameter> parameters = procedure.getObject().getParameters(monitor);
            if (!CommonUtils.isEmpty(parameters)) {
                StringBuilder sb = new StringBuilder();
                sb.append(name).append("(");
                int index = 0;
                for (DBSProcedureParameter param : parameters) {
                    if (param.getParameterKind().isInput()) {
                        if (index++ > 0) {
                            sb.append(", ");
                        }
                        sb.append(":").append(param.getName());
                    }
                }
                sb.append(")");
                text = sb.toString();
            } else {
                text = name + "()";
            }
            return text;
        } catch (DBException e) {
            log.error("Failed to obtain procedure parameters info", e);
            return name;
        }
    }

    @Nullable
    @Override
    public String visitBuiltinFunction(@NotNull SQLBuiltinFunctionCompletionItem function) {
        return function.name + "()";
    }

    @Nullable
    @Override
    public String visitSpecialText(@NotNull SQLSpecialTextCompletionItem specialText) {
        return specialText.text;
    }
}

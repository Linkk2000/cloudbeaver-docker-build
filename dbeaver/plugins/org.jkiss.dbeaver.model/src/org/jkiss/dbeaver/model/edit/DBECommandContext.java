/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2024 DBeaver Corp and others
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

package org.jkiss.dbeaver.model.edit;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPContextProvider;
import org.jkiss.dbeaver.model.DBPObject;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;

import java.util.Collection;
import java.util.Map;

/**
 * Command context.
 * Provides facilities for object edit commands, undo/redo, save/revert
 */
public interface DBECommandContext extends DBPContextProvider {

    // Do not use transactions in conect save
    String OPTION_AVOID_TRANSACTIONS = "avoidTransactions";

    boolean isDirty();

    @Nullable
    DBECommand getUndoCommand();

    @Nullable
    DBECommand getRedoCommand();

    void saveChanges(@NotNull DBRProgressMonitor monitor, @NotNull Map<String, Object> options) throws DBException;

    void resetChanges(boolean undoCommands);

    void undoCommand();

    void redoCommand();

    @NotNull
    Collection<? extends DBECommand<?>> getFinalCommands();

    @NotNull
    Collection<? extends DBECommand<?>> getUndoCommands();

    @NotNull
    Collection<DBPObject> getEditedObjects();

    void addCommand(@NotNull DBECommand command, @Nullable DBECommandReflector reflector);

    void addCommand(DBECommand command, DBECommandReflector reflector, boolean execute);

    //void addCommandBatch(List<DBECommand> commands, DBECommandReflector reflector, boolean execute);

    void removeCommand(DBECommand<?> command);

    void updateCommand(DBECommand<?> command, DBECommandReflector commandReflector);

    void addCommandListener(DBECommandListener listener);

    void removeCommandListener(DBECommandListener listener);

    Map<Object, Object> getUserParams();

}
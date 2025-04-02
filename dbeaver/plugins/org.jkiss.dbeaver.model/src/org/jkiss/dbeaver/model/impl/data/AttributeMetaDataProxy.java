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
package org.jkiss.dbeaver.model.impl.data;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.DBPImage;
import org.jkiss.dbeaver.model.DBPImageProvider;
import org.jkiss.dbeaver.model.DBValueFormatting;
import org.jkiss.dbeaver.model.exec.DBCAttributeMetaData;
import org.jkiss.dbeaver.model.exec.DBCEntityMetaData;
import org.jkiss.dbeaver.model.struct.DBSAttributeBase;
import org.jkiss.dbeaver.model.struct.DBSEntity;
import org.jkiss.dbeaver.model.struct.DBSObject;

/**
 * Type attribute value binding info
 */
public class AttributeMetaDataProxy implements DBCAttributeMetaData, DBPImageProvider {
    @NotNull
    protected final DBSAttributeBase attribute;

    public AttributeMetaDataProxy(
        @NotNull DBSAttributeBase attribute)
    {
        this.attribute = attribute;
    }

    @NotNull
    public DBSAttributeBase getProxyAttribute() {
        return attribute;
    }

    /**
     * Attribute index in result set
     * @return attribute index (zero based)
     */
    @Override
    public int getOrdinalPosition()
    {
        return attribute.getOrdinalPosition();
    }

    @Override
    public boolean isRequired() {
        return attribute.isRequired();
    }

    @Override
    public boolean isAutoGenerated() {
        return attribute.isAutoGenerated();
    }

    @Nullable
    @Override
    public DBSObject getSource() {
        if (attribute instanceof DBSObject) {
            return ((DBSObject)attribute).getParentObject();
        }
        return null;
    }

    /**
     * Attribute label
     */
    @NotNull
    public String getLabel()
    {
        DBSAttributeBase proxyAttribute = getProxyAttribute();
        if (proxyAttribute instanceof DBCAttributeMetaData attributeMetaData) {
            return attributeMetaData.getLabel();
        }

        return attribute.getName();
    }

    @Nullable
    @Override
    public String getEntityName() {
        DBSObject source = getSource();
        if (source instanceof DBSEntity) {
            return source.getName();
        }
        return null;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Nullable
    @Override
    public DBCEntityMetaData getEntityMetaData() {
        DBSAttributeBase proxyAttribute = getProxyAttribute();
        if (proxyAttribute instanceof DBCAttributeMetaData attributeMetaData) {
            return attributeMetaData.getEntityMetaData();
        }
        return null;
    }

    /**
     * Attribute name
     */
    @NotNull
    public String getName()
    {
        return attribute.getName();
    }

    @Nullable
    @Override
    public DBPImage getObjectImage() {
        return DBValueFormatting.getObjectImage(attribute);
    }

    @NotNull
    @Override
    public String getTypeName() {
        return attribute.getTypeName();
    }

    @NotNull
    @Override
    public String getFullTypeName() {
        return attribute.getFullTypeName();
    }

    @Override
    public int getTypeID() {
        return attribute.getTypeID();
    }

    @NotNull
    @Override
    public DBPDataKind getDataKind() {
        return attribute.getDataKind();
    }

    @Nullable
    @Override
    public Integer getScale() {
        return attribute.getScale();
    }

    @Nullable
    @Override
    public Integer getPrecision() {
        return attribute.getPrecision();
    }

    @Override
    public long getMaxLength() {
        return attribute.getMaxLength();
    }

    @Override
    public long getTypeModifiers() {
        return attribute.getTypeModifiers();
    }

    @Override
    public String toString() {
        return attribute.toString();
    }

    @Override
    public int hashCode() {
        return attribute.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AttributeMetaDataProxy &&
            attribute.equals(((AttributeMetaDataProxy) obj).attribute);
    }
}

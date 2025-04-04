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
package io.cloudbeaver.model;

import io.cloudbeaver.WebServiceUtils;
import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.model.DBIcon;
import org.jkiss.dbeaver.model.connection.DBPDriverLibrary;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.registry.driver.DriverFileInfo;

public class WebDriverLibraryFileInfo {

    @NotNull
    private final DriverFileInfo fileInfo;

    public WebDriverLibraryFileInfo(@NotNull DriverFileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }


    @Property
    public String getId() {
        return fileInfo.getId();
    }

    @Property
    public String getFileName() {
        return fileInfo.toString();
    }

    @Property
    public String getIcon() {
        if (fileInfo.getType() == DBPDriverLibrary.FileType.license) {
            return WebServiceUtils.makeIconId(DBIcon.TYPE_TEXT);
        }
        return WebServiceUtils.makeIconId(DBIcon.JAR);
    }
}

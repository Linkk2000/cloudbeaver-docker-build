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
package io.cloudbeaver.service;

import io.cloudbeaver.model.app.ServletAppConfiguration;
import io.cloudbeaver.model.app.ServletApplication;
import io.cloudbeaver.model.app.ServletServerConfiguration;
import io.cloudbeaver.model.session.WebSession;
import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;

/**
 * Web service implementation
 */
public interface DBWServiceServerConfigurator extends DBWServiceBinding {

    void configureServer(
        @NotNull ServletApplication application,
        @Nullable WebSession session,
        @NotNull ServletServerConfiguration serverConfiguration,
        @NotNull ServletAppConfiguration appConfig
    ) throws DBException;

    default void migrateConfigurationIfNeeded(@NotNull ServletApplication application) throws DBException {

    }

    void reloadConfiguration(@NotNull ServletAppConfiguration appConfig) throws DBException;

}

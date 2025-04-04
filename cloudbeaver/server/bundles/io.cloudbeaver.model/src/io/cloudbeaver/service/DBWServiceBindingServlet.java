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

import io.cloudbeaver.model.app.ServletApplication;
import org.jkiss.dbeaver.DBException;

/**
 * Servlet service
 */
public interface DBWServiceBindingServlet<APPLICATION extends ServletApplication> extends DBWServiceBinding {
    default boolean isApplicable(ServletApplication application) {
        return true;
    }

    void addServlets(APPLICATION application, DBWServletContext servletContext) throws DBException;
}

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
package org.jkiss.dbeaver.ext.altibase.ui.config;

import org.jkiss.dbeaver.ext.altibase.model.AltibaseSequence;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEObjectConfigurator;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSEntityType;
import org.jkiss.dbeaver.ui.UITask;
import org.jkiss.dbeaver.ui.editors.object.struct.EntityEditPage;

import java.util.Map;

public class AltibaseSequenceConfigurator implements DBEObjectConfigurator<AltibaseSequence> {

    @Override
    public AltibaseSequence configureObject(DBRProgressMonitor monitor, DBECommandContext commandContext,
            Object container, AltibaseSequence sequence, Map<String, Object> options) {
        return UITask.run(() -> {
            EntityEditPage page = new EntityEditPage(sequence.getDataSource(), DBSEntityType.SEQUENCE);
            if (!page.edit()) {
                return null;
            }

            sequence.setName(page.getEntityName());

            return sequence;
        });
    }
}
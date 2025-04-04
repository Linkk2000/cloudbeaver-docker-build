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

package org.jkiss.dbeaver.ui.config.migration.wizards.navicat;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.connection.DBPDriver;
import org.jkiss.dbeaver.ui.config.migration.ImportConfigMessages;
import org.jkiss.dbeaver.ui.config.migration.wizards.ConfigImportWizardPage;
import org.jkiss.dbeaver.ui.config.migration.wizards.ImportConnectionInfo;
import org.jkiss.dbeaver.ui.config.migration.wizards.ImportData;
import org.jkiss.dbeaver.ui.config.migration.wizards.ImportDriverInfo;
import org.jkiss.utils.CommonUtils;
import org.jkiss.utils.xml.XMLException;
import org.jkiss.utils.xml.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigImportWizardPageNavicatConnections extends ConfigImportWizardPage {
    
    private NavicatEncrypt decryptor;
    
    protected ConfigImportWizardPageNavicatConnections()
    {
        super(ImportConfigMessages.config_import_wizard_navicat_name);
        setTitle(ImportConfigMessages.config_import_wizard_navicat_name);
        setDescription(ImportConfigMessages.config_import_wizard_navicat_description);
        decryptor = new NavicatEncrypt();
    }

    @Override
    protected void loadConnections(ImportData importData) throws DBException
    {
        setErrorMessage(null);

        ConfigImportWizardNavicat wizard = (ConfigImportWizardNavicat) getWizard();
        final DBPDriver driver = wizard.getDriver();

        ImportDriverInfo driverInfo = new ImportDriverInfo(driver.getId(), driver.getName(), driver.getSampleURL(),
                driver.getDriverClassName());
        importData.addDriver(driverInfo);

        File inputFile = wizard.getInputFile();
        try (InputStream is = new FileInputStream(inputFile)) {
            try (Reader reader = new InputStreamReader(is, wizard.getInputFileEncoding())) {
                importNCX(importData, driverInfo, reader);
            }
        } catch (Exception e) {
            setErrorMessage(e.getMessage());
        }
    }

    private void importNCX(ImportData importData, ImportDriverInfo driver, Reader reader) throws XMLException
    {
        Document document = XMLUtils.parseDocument(reader);
        for (Element conElement : XMLUtils.getChildElementList(document.getDocumentElement())) {
            Map<String, String> conProps = new HashMap<>();
            NamedNodeMap attrs = conElement.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Attr attr = (Attr) attrs.item(i);
                conProps.put(attr.getName(), attr.getValue());
            }
            makeConnectionFromProps(importData, driver, conProps);
        }
    }

    private void makeConnectionFromProps(ImportData importData, ImportDriverInfo driver, Map<String, String> conProps)
    {
        String name = conProps.get("ConnectionName");
        String password = decryptPassword(conProps.get("Password"));

        if (CommonUtils.isEmpty(name)) {
            return;
        }
        importData.addConnection(new ImportConnectionInfo(
            driver, 
            name, 
            name, 
            "",
            conProps.get("Host"), 
            conProps.get("Port"), 
            conProps.get("Database"), 
            conProps.get("UserName"), 
            password
         ));
    }
    
    /**
     * Decrypts password chiper-text
     * 
     * @param encryptedPassword
     * @return Plain-text of password or empty string if unable to decrypt
     */
    private String decryptPassword(String encryptedPassword)
    {
        try {
            return decryptor.decrypt(encryptedPassword);
        } catch (Exception e) {
            return "";
        }
    }

}

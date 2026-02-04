/*
 * Copyright (C) 2009 Tolven Inc

 * This library is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU Lesser General Public License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;  
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 *
 * Contact: info@tolvenhealth.com 
 *
 * @author Joseph Isaac
 * @version $Id$
 */
package org.tolven.surescripts.postgresqlmgr;

import java.io.File;

import org.apache.log4j.Logger;
import org.java.plugin.registry.ExtensionPoint;
import org.tolven.plugin.TolvenCommandPlugin;
import org.tolven.surescripts.postgresqlmgr.gui.PostgresqlConnectionPanel;
import org.tolven.surescripts.postgresqlmgr.gui.PostgresqlMgrPanel;
import org.tolven.tools.ant.TolvenSQL;

/**
 * This plugin generate new schemas for care plan project
 */
public class SurescriptsPostgresqlMgrPlugin extends TolvenCommandPlugin {

    public static final String CMD_LINE_TEST_ADMIN_DB_OPTION = "testAdminDB";
    public static final String CMD_LINE_TEST_APPSERVER_DB_OPTION = "testAppServerDB";
    public static final String CMD_LINE_UPDATE_SCHEMAS_OPTION = "updateSchemas";
    public static final String CMD_LINE_UPDATE_INDEXES_OPTION = "updateIndexes";
    public static final String CMD_LINE_GUI_OPTION = "gui";

    public static final String EXTENSION_DATASOURCE = "dataSource";
    public static final String EXTENSION_POINT_JDBCDRIVER = "jdbcDriver";
    public static final String EXTENSION_POINT_JDBCDRIVER_CLASS = "jdbcDriverClass";

    private PostgresqlMgr postgresqlMgr;
    private PostgresqlMgrPanel postgresqlMgrPanel;
    private Logger logger = Logger.getLogger(SurescriptsPostgresqlMgrPlugin.class);
 

    private PostgresqlMgr getPostgresqlMgr() {
        if (postgresqlMgr == null) {
            ExtensionPoint dataSourceExtensionPoint = getDescriptor().getExtensionPoint(EXTENSION_DATASOURCE);
            ExtensionPoint parentDataSourceExtensionPoint = getParentExtensionPoint(dataSourceExtensionPoint);
            String hostname = parentDataSourceExtensionPoint.getParameterDefinition("database.hostname").getDefaultValue();
            String eval_hostname = (String) evaluate(hostname, parentDataSourceExtensionPoint.getDeclaringPluginDescriptor());
            if (eval_hostname == null) {
                throw new RuntimeException(parentDataSourceExtensionPoint.getUniqueId() + "@database.hostname" + "evaluated to null using: " + hostname);
            }
            String port = parentDataSourceExtensionPoint.getParameterDefinition("database.port").getDefaultValue();
            String eval_port = (String) evaluate(port, parentDataSourceExtensionPoint.getDeclaringPluginDescriptor());
            if (eval_port == null) {
                throw new RuntimeException(parentDataSourceExtensionPoint.getUniqueId() + "@database.port" + "evaluated to null using: " + port);
            }
            String databaseName = parentDataSourceExtensionPoint.getParameterDefinition("database.databaseName").getDefaultValue();
            String eval_databaseName = (String) evaluate(databaseName, parentDataSourceExtensionPoint.getDeclaringPluginDescriptor());
            if (eval_databaseName == null) {
                throw new RuntimeException(parentDataSourceExtensionPoint.getUniqueId() + "@database.databaseName" + "evaluated to null using: " + databaseName);
            }
            String connectionString = "jdbc:postgresql://" + eval_hostname + ":" + eval_port + "/" + eval_databaseName + "?ssl=true";
            ExtensionPoint jdbcDriverClassExtensionPoint = getDescriptor().getExtensionPoint(EXTENSION_POINT_JDBCDRIVER_CLASS);
            ExtensionPoint parentJDBCDriverClassExtensionPoint = getParentExtensionPoint(jdbcDriverClassExtensionPoint);
            String jdbcDriverClass = parentJDBCDriverClassExtensionPoint.getParameterDefinition("jdbcDriverClass").getDefaultValue();
            String eval_jdbcDriverClass = (String) evaluate(jdbcDriverClass, parentJDBCDriverClassExtensionPoint.getDeclaringPluginDescriptor());
            if (eval_jdbcDriverClass == null) {
                throw new RuntimeException(parentDataSourceExtensionPoint.getUniqueId() + "@database.jdbcDriverClass" + "evaluated to null using: " + jdbcDriverClass);
            }
            File testSQLFile = getFilePath(getDescriptor().getAttribute("testSQL").getValue());
            postgresqlMgr = new PostgresqlMgr();
            postgresqlMgr.setHostname(eval_hostname);
            postgresqlMgr.setPort(eval_port);
            postgresqlMgr.setDBName(eval_databaseName);
            postgresqlMgr.setConnectionString(connectionString);
            postgresqlMgr.setJDBCDriverClass(eval_jdbcDriverClass);
            postgresqlMgr.setSQLFile(testSQLFile);
            postgresqlMgr.setTolvenConfigWrapper(getTolvenConfigWrapper());
        }
        return postgresqlMgr;
    }

    private PostgresqlMgrPanel getPostgresqlMgrPanel() {
        if (postgresqlMgrPanel == null) {
            postgresqlMgrPanel = new PostgresqlMgrPanel();
            postgresqlMgrPanel.addTab("Connection", new PostgresqlConnectionPanel(getPostgresqlMgr()));
        }
        return postgresqlMgrPanel;
    }

    @Override
    protected void doStart() throws Exception {
        logger.info("*** start ***");
    }

    @Override
    public void execute(String[] args) {
        logger.info("*** execute ***");
        try {
            updateSchemas();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(getNestedMessage(ex));
        }
    }

    public void updateSchemas() {
        File sqlFile = getFilePath(getDescriptor().getAttribute("updateSchemasSQL").getValue());
        executeSQL(sqlFile);
    }

    public void executeSQL(File sqlFile) {
        ExtensionPoint dataSourceExtensionPoint = getDescriptor().getExtensionPoint(EXTENSION_DATASOURCE);
        ExtensionPoint parentDataSourceExtensionPoint = getParentExtensionPoint(dataSourceExtensionPoint);
        String hostname = (String) evaluate(parentDataSourceExtensionPoint.getParameterDefinition("database.hostname").getDefaultValue(), parentDataSourceExtensionPoint.getDeclaringPluginDescriptor());
        String port = (String) evaluate(parentDataSourceExtensionPoint.getParameterDefinition("database.port").getDefaultValue(), parentDataSourceExtensionPoint.getDeclaringPluginDescriptor());
        String databaseName = (String) evaluate(parentDataSourceExtensionPoint.getParameterDefinition("database.databaseName").getDefaultValue(), parentDataSourceExtensionPoint.getDeclaringPluginDescriptor());
        String url = "jdbc:postgresql://" + hostname + ":" + port + "/" + databaseName + "?ssl=true";
        ExtensionPoint jdbcDriverClassExtensionPoint = getDescriptor().getExtensionPoint(EXTENSION_POINT_JDBCDRIVER_CLASS);
        ExtensionPoint parentJDBCDriverClassExtensionPoint = getParentExtensionPoint(jdbcDriverClassExtensionPoint);
        String jdbcDriverClass = (String) evaluate(parentJDBCDriverClassExtensionPoint.getParameterDefinition("jdbcDriverClass").getDefaultValue(), parentJDBCDriverClassExtensionPoint.getDeclaringPluginDescriptor());
        String dbUserId = getTolvenConfigWrapper().getDBServer().getUser();
        String dbServerRootPasswordId = getTolvenConfigWrapper().getDBServerRootPasswordId();
        char[] password = getTolvenConfigWrapper().getPassword(dbServerRootPasswordId);
        logger.info("Execute SQL file " + sqlFile.getPath() + " with URL: " + url);
        TolvenSQL.sql(sqlFile, url, jdbcDriverClass, dbUserId, password);
    }
    private String getNestedMessage(Exception ex) {
        StringBuffer buff = new StringBuffer();
        Throwable throwable = ex;
        do {
            buff.append(throwable.getMessage() + "\n");
            throwable = throwable.getCause();
        } while (throwable != null);
        return buff.toString();
    }
 
    @Override
    protected void doStop() throws Exception {
        logger.info("*** stop ***");
    }

}

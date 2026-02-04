package org.tolven.surescripts.postgresqlmgr;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.tolven.config.model.TolvenConfigWrapper;
import org.tolven.tools.ant.TolvenSQL;

public class PostgresqlMgr {

    public static String ABOUT_TO_TEST_MESSAGE = "About To Test";
    public static String SUCCESSFUL_CONNECTION_MESSAGE = "Connection Successful";

    private File sqlFile;
    private String hostname;
    private String port;
    private String dbName;
    private String jdbcDriverClass;
    private String connectionString;
    private TolvenConfigWrapper tolvenConfigWrapper;

    private Logger logger = Logger.getLogger(PostgresqlMgr.class);

    public PostgresqlMgr() {
    }

    public File getSQLFile() {
        return sqlFile;
    }

    public void setSQLFile(File sqlFile) {
        this.sqlFile = sqlFile;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDBName() {
        return dbName;
    }

    public void setDBName(String dbName) {
        this.dbName = dbName;
    }

    public String getJDBCDriverClass() {
        return jdbcDriverClass;
    }

    public void setJDBCDriverClass(String jdbcDriverClass) {
        this.jdbcDriverClass = jdbcDriverClass;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public TolvenConfigWrapper getTolvenConfigWrapper() {
        return tolvenConfigWrapper;
    }

    public void setTolvenConfigWrapper(TolvenConfigWrapper tolvenConfigWrapper) {
        this.tolvenConfigWrapper = tolvenConfigWrapper;
    }

    public String getAdminId() {
        return getTolvenConfigWrapper().getAdminId();
    }

    public String getDBId() {
        return getTolvenConfigWrapper().getDBServerId();
    }

    public String getDBRootUser() {
        return getTolvenConfigWrapper().getDBServer().getUser();
    }

    public char[] getDBRootUserPassword() {
        return getTolvenConfigWrapper().getPassword(getTolvenConfigWrapper().getDBServerRootPasswordId());
    }

    public String getAppServerId() {
        return getTolvenConfigWrapper().getAppServerId();
    }

    public String testAdminDBConnection() {
        getTolvenConfigWrapper().initializeJSSE();
        String adminId = getTolvenConfigWrapper().getAdminId();
        String messageSuffix = " for db user: " + getDBRootUser() + " to: " + getConnectionString() + " using: " + adminId + " SSL certificate\n";
        String preTestString = "\n" + ABOUT_TO_TEST_MESSAGE + messageSuffix;
        logger.debug(preTestString);
        System.out.println(preTestString);
        testDBConnection();
        String result = "\n" + SUCCESSFUL_CONNECTION_MESSAGE + messageSuffix + "\n";
        logger.debug(result);
        System.out.println(result);
        return result;
    }

    public String testSimulatedAppServerDBConnection() {
        try {
            String appServerSSLId = getTolvenConfigWrapper().getAppServer().getId();
            char[] appServerSSLPassword = getTolvenConfigWrapper().getPassword(appServerSSLId);
            getTolvenConfigWrapper().initializeJSSE(appServerSSLId, appServerSSLPassword);
            String messageSuffix = " for db user: " + getDBRootUser() + " to: " + getConnectionString() + " using: " + appServerSSLId + " SSL certificate\n";
            String preTestString = "\n" + ABOUT_TO_TEST_MESSAGE + messageSuffix;
            logger.debug(preTestString);
            System.out.println(preTestString);
            testDBConnection();
            String result = "\n" + SUCCESSFUL_CONNECTION_MESSAGE + messageSuffix + "\n";
            logger.debug(result);
            System.out.println(result);
            return result;
        } finally {
            getTolvenConfigWrapper().initializeJSSE();
        }
    }

    public void testDBConnection() {
        try {
            logger.info("Execute SQL file " + getSQLFile().getPath() + " with URL: " + getConnectionString());
            TolvenSQL.sql(getSQLFile(), getConnectionString(), getJDBCDriverClass(), getDBRootUser(), getDBRootUserPassword());
        } catch (Exception ex) {
            String sql;
            try {
                sql = FileUtils.readFileToString(getSQLFile());
            } catch (IOException e) {
                throw new RuntimeException("Could not read SQL from file: " + getSQLFile(), e);
            }
            throw new RuntimeException("Failed to execute SQL: " + sql + "\nfor: " + getDBRootUser() + "\nusing:\n" + getConnectionString(), ex);
        }
    }

}

package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Properties;

/*
 * Created by IntelliJ IDEA.
 * User: bill
 * Date: 19-Oct-2002
 * Time: 16:58:04
 * To change this template use Options | File Templates.
 */

public class TestHelper {

    private static final Log LOG = LogFactory.getLog(TestHelper.class);

    private static final String USER = "sa";

    private static final String PASSWORD = "";

    protected static final String PROXOOL_DRIVER = "org.logicalcobwebs.proxool.ProxoolDriver";

    protected static final String HYPERSONIC_DRIVER = "org.hsqldb.jdbcDriver";

    protected static final String HYPERSONIC_URL = "jdbc:hsqldb:test";

    private static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";

    private static final String ORACLE_URL = "jdbc:oracle:thin:@172.27.52.112:1527:hpgs1";

    protected static final String SQL_INSERT_INTO_TEST = "INSERT INTO TEST VALUES(1);";

    protected static final String SQL_CHECK_TEST = "SELECT COUNT(*) FROM TEST;";

    protected static Properties buildProperties() {
        Properties info = new Properties();
        info.setProperty("user", USER);
        info.setProperty("password", PASSWORD);
        info.setProperty("proxool.verbose", "true");
        return info;
    }

    protected static void registerPool(String alias) throws SQLException {
        registerPool(alias, TestHelper.buildProperties());
    }

    protected static void registerPool(String alias, Properties info) throws SQLException {
        ProxoolFacade.registerConnectionPool(getFullUrl(alias), info);
    }

    protected static void registerOraclePool(String alias, Properties info) throws SQLException {
        ProxoolFacade.registerConnectionPool(getFullOracleUrl(alias), info);
    }

    protected static Connection getProxoolConnection(String url) throws ClassNotFoundException, SQLException {
        return getProxoolConnection(url, buildProperties());
    }

    protected static Connection getProxoolConnection(String url, Properties info) throws ClassNotFoundException, SQLException {
        Connection connection = null;
        Class.forName(PROXOOL_DRIVER);
        connection = DriverManager.getConnection(url, info);
        return connection;
    }

    protected static String getSimpleUrl(String alias) {
        String url = ProxoolConstants.PROXOOL
                + ProxoolConstants.ALIAS_DELIMITER
                + alias;
        return url;
    }

    protected static String buildProxoolUrl(String alias, String driver, String delegateUrl) {
        String url = ProxoolConstants.PROXOOL
                + ProxoolConstants.ALIAS_DELIMITER
                + alias
                + ProxoolConstants.URL_DELIMITER
                + driver
                + ProxoolConstants.URL_DELIMITER
                + delegateUrl;
        return url;
    }

    protected static String getFullUrl(String alias) {
        String url = ProxoolConstants.PROXOOL
                + ProxoolConstants.ALIAS_DELIMITER
                + alias
                + ProxoolConstants.URL_DELIMITER
                + HYPERSONIC_DRIVER
                + ProxoolConstants.URL_DELIMITER
                + HYPERSONIC_URL;
        return url;
    }

    protected static String getFullOracleUrl(String alias) {
        try {
            Class.forName(ORACLE_DRIVER);
        } catch (ClassNotFoundException e) {
            LOG.error(ORACLE_DRIVER, e);
        }
        String url = ProxoolConstants.PROXOOL
                + ProxoolConstants.ALIAS_DELIMITER
                + alias
                + ProxoolConstants.URL_DELIMITER
                + ORACLE_DRIVER
                + ProxoolConstants.URL_DELIMITER
                + ORACLE_URL;
        return url;
    }

    protected static Connection getDirectConnection() throws ClassNotFoundException, SQLException {
        Connection connection = null;
        Class.forName(HYPERSONIC_DRIVER);
        connection = DriverManager.getConnection(HYPERSONIC_URL, buildProperties());
        return connection;
    }

    protected static Connection getDirectOracleConnection() throws ClassNotFoundException, SQLException {
        Connection connection = null;
        Class.forName(ORACLE_DRIVER);
        Properties info = buildProperties();
        info.setProperty("user", "hsi_servlet");
        info.setProperty("password", "hsi_servlet");
        connection = DriverManager.getConnection(ORACLE_URL, info);
        return connection;
    }

    protected static int getCount(Connection connection, String table) throws SQLException {
        Statement statement = null;
        ResultSet resultSet = null;
        int count = -1;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + table);
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            } else {
                LOG.warn("No rows returned from " + table);
            }
        } finally {
            if (statement != null) {
                try {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    if (statement != null) {
                        statement.close();
                    }
                } catch (SQLException e) {
                    LOG.error("Couldn't close statement", e);
                }
            }
        }
        return count;
    }

    protected static void execute(Connection connection, String sql) throws SQLException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute(sql);
        } finally {
            if (statement != null) {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                } catch (SQLException e) {
                    LOG.error("Couldn't close statement", e);
                }
            }
        }
    }

    protected static void createTable(String table) throws Exception {
        try {
            execute(getDirectConnection(), "CREATE TABLE " + table + " (A INT)");
        } catch (Exception e) {
            LOG.error("Error creating table " + table, e);
            throw e;
        }
    }

    protected static void dropTable(String table) throws SQLException, ClassNotFoundException {
        execute(getDirectConnection(), "DROP TABLE " + table);
    }

    protected static void insertRow(Connection connection, String table) throws SQLException {
        execute(connection, "INSERT INTO " + table + " VALUES(1)");
    }

    protected static void testConnection(Connection connection) throws SQLException {
        execute(connection, "SELECT SYSDATE FROM DUAL");
    }

}

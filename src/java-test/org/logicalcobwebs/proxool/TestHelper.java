package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.xml.DOMConfigurator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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

    private static final String PROXOOL_DRIVER = "org.logicalcobwebs.proxool.ProxoolDriver";

    private static final String HYPERSONIC_DRIVER = "org.hsqldb.jdbcDriver";

    private static final String HYPERSONIC_URL = "jdbc:hsqldb:.";

    private static final String SQL_CREATE_TEST_TABLE = "create table test (a int, b varchar)";

    private static final String SQL_DROP_TEST_TABLE = "drop table test";

    private static final String SQL_SELECT_FROM_TEST = "SELECT * FROM test";

    protected static Properties buildProperties() {
        Properties info = new Properties();
        info.setProperty("user", USER);
        info.setProperty("password", PASSWORD);
        info.setProperty("proxool.debug-level", "1");
        return info;
    }

    protected static void registerPool(String alias) throws SQLException {
        registerPool(alias, TestHelper.buildProperties());
    }

    protected static void registerPool(String alias, Properties info) throws SQLException {
        ProxoolFacade.registerConnectionPool(getFullUrl(alias), info);
    }

    protected static Connection getProxoolConnection(String url) throws ClassNotFoundException, SQLException {
        return getProxoolConnection(url, null);
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

    protected static Connection getDirectConnection() throws ClassNotFoundException, SQLException {
        Connection connection = null;
        Class.forName(HYPERSONIC_DRIVER);
        connection = DriverManager.getConnection(HYPERSONIC_URL, buildProperties());
        return connection;
    }

    protected static void execute(Connection connection, String sql) throws SQLException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute(sql);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOG.error("Couldn't close statement", e);
                }
            }
        }
    }

    protected static void setupDatabase() throws SQLException, ClassNotFoundException {
        execute(getDirectConnection(), SQL_CREATE_TEST_TABLE);
    }

    protected static void tearDownDatabase() throws SQLException, ClassNotFoundException {
        execute(getDirectConnection(), SQL_DROP_TEST_TABLE);
    }

    protected static void testConnection(Connection connection) throws SQLException {
        execute(connection, SQL_SELECT_FROM_TEST);
    }

}

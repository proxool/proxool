package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Helper for all tests
 *
 * @version $Revision: 1.20 $, $Date: 2003/03/05 23:28:55 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 */
public class TestHelper {

    private static final Log LOG = LogFactory.getLog(TestHelper.class);

    /**
     * Builds a complete set of proxool properties, with all values set to
     * something different than the default vaule.
     * @return the properties that was buildt.
     */
    public static Properties buildCompleteAlternativeProperties() {
        Properties properties = new Properties();
        properties.setProperty("user", "sa");
        properties.setProperty("password", "");
        properties.setProperty(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY, "40000");
        properties.setProperty(ProxoolConstants.HOUSE_KEEPING_TEST_SQL_PROPERTY, "select CURRENT_DATE");
        properties.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "10");
        properties.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, "3");
        properties.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_LIFETIME_PROPERTY, "18000000");
        properties.setProperty(ProxoolConstants.SIMULTANEOUS_BUILD_THROTTLE_PROPERTY, "5");
        properties.setProperty(ProxoolConstants.RECENTLY_STARTED_THRESHOLD_PROPERTY, "40000");
        properties.setProperty(ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME_PROPERTY, "50000");
        properties.setProperty(ProxoolConstants.MAXIMUM_ACTIVE_TIME_PROPERTY, "60000");
        properties.setProperty(ProxoolConstants.VERBOSE_PROPERTY, "true");
        properties.setProperty(ProxoolConstants.TRACE_PROPERTY, "true");
        properties.setProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY, "Fatal error");
        properties.setProperty(ProxoolConstants.PROTOTYPE_COUNT_PROPERTY, "2");
        return properties;
    }

    /**
     * Test that the given ConnectionPoolDefinitionIF contains all the properties as defined in
     * {@link #buildCompleteAlternativeProperties}.
     * @param connectionPoolDefinition the ConnectionPoolDefinitionIF to be tested.
     * @throws ProxoolException if any properties are missing or have the wrong value.
     */
    public static void equalsCompleteAlternativeProperties(ConnectionPoolDefinitionIF connectionPoolDefinition)
            throws ProxoolException {
        checkProperty("user", "sa", connectionPoolDefinition.getDelegateProperties().getProperty("user"));
        checkProperty("password", "", connectionPoolDefinition.getDelegateProperties().getProperty("password"));
        checkProperty(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME, 40000,
                connectionPoolDefinition.getHouseKeepingSleepTime());
        checkProperty(ProxoolConstants.HOUSE_KEEPING_TEST_SQL, "select CURRENT_DATE",
                connectionPoolDefinition.getHouseKeepingTestSql());
        checkProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT, 10,
                connectionPoolDefinition.getMaximumConnectionCount());
        checkProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT, 3,
                connectionPoolDefinition.getMinimumConnectionCount());
        checkProperty(ProxoolConstants.MAXIMUM_CONNECTION_LIFETIME, 18000000,
                connectionPoolDefinition.getMaximumConnectionLifetime());
        checkProperty(ProxoolConstants.SIMULTANEOUS_BUILD_THROTTLE, 5,
                connectionPoolDefinition.getSimultaneousBuildThrottle());
        checkProperty(ProxoolConstants.RECENTLY_STARTED_THRESHOLD, 40000,
                connectionPoolDefinition.getRecentlyStartedThreshold());
        checkProperty(ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME, 50000,
                connectionPoolDefinition.getOverloadWithoutRefusalLifetime());
        checkProperty(ProxoolConstants.MAXIMUM_ACTIVE_TIME, 60000,
                connectionPoolDefinition.getMaximumActiveTime());
        checkProperty(ProxoolConstants.VERBOSE, "true",
                new Boolean(connectionPoolDefinition.isVerbose()).toString());
        checkProperty(ProxoolConstants.TRACE, "true",
                new Boolean(connectionPoolDefinition.isTrace() == true).toString());
        checkProperty(ProxoolConstants.FATAL_SQL_EXCEPTION, "Fatal error",
                connectionPoolDefinition.getFatalSqlExceptions().iterator().next().toString());
        checkProperty(ProxoolConstants.PROTOTYPE_COUNT, 2,
                connectionPoolDefinition.getPrototypeCount());
    }

    private static void checkProperty(String name, String correctValue, String candidateValue) throws ProxoolException {
        if (candidateValue == null) {
            throw new ProxoolException(name + " was null.");
        } else if (!candidateValue.equals(correctValue)) {
            throw new ProxoolException("Expected value for " + name + " was " + correctValue + " but the value was "
                    + candidateValue + ".");
        }
    }

    private static void checkProperty(String name, int correctValue, int candidateValue) throws ProxoolException {
        checkProperty(name, String.valueOf(correctValue), String.valueOf(candidateValue));
    }

    /**
     * Build a valid Proxool URL
     * @param alias identifies the pool
     * @param driver the delegate driver
     * @param delegateUrl the url to send to the delegate driver
     * @return proxool.alias:driver:delegateUrl
     */
    public static String buildProxoolUrl(String alias, String driver, String delegateUrl) {
        String url = ProxoolConstants.PROXOOL
                + ProxoolConstants.ALIAS_DELIMITER
                + alias
                + ProxoolConstants.URL_DELIMITER
                + driver
                + ProxoolConstants.URL_DELIMITER
                + delegateUrl;
        return url;
    }

    /**
     * Build a valid Proxool URL
     * @param alias identifies the pool
     * @return proxool.alias
     */
    public static String buildProxoolUrl(String alias) {
        String url = ProxoolConstants.PROXOOL
                + ProxoolConstants.ALIAS_DELIMITER
                + alias;
        return url;
    }

    public static Connection getDirectConnection() throws ClassNotFoundException, SQLException {
        Connection connection = null;
        Class.forName(TestConstants.HYPERSONIC_DRIVER);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        connection = DriverManager.getConnection(TestConstants.HYPERSONIC_TEST_URL, info);
        return connection;
    }

}

/*
 Revision history:
 $Log: TestHelper.java,v $
 Revision 1.20  2003/03/05 23:28:55  billhorsman
 deprecated maximum-new-connections property in favour of
 more descriptive simultaneous-build-throttle

 Revision 1.19  2003/03/04 10:58:44  billhorsman
 checkstyle

  */
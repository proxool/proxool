/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;
import org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF;
import org.logicalcobwebs.proxool.ConnectionPoolStatisticsIF;
import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ProxoolFacade;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Various tests
 *
 * @version $Revision: 1.1 $, $Date: 2002/09/13 08:14:24 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class GeneralTests extends TestCase {

    private static final String USER = "sa";

    private static final String PASSWORD = "";

    public GeneralTests(String name) {
        super(name);
    }

    /**
     * Can we refer to the same pool by either the complete URL or the alias?
     */
    public void testAlias() throws SQLException {

        String alias = "alias";

        // Register pool
        getDate(prefix + alias + urlSuffix);

        // Get it back by url
        getDate(prefix + alias + urlSuffix);

        // Get it back by name
        getDate(prefix + alias);

        ConnectionPoolStatisticsIF connectionPoolStatistics = ProxoolFacade.getConnectionPoolStatistics(alias);

        // If the above calls all used the same pool then it should have served exactly 3 connections.s
        assertEquals(3L, connectionPoolStatistics.getConnectionsServedCount());

    }

    /**
     * Can we update a pool definition by passing a new Properties object?
     */
    public void testUpdate() throws SQLException {

        String alias = "update";

        getDate(prefix + alias + urlSuffix);

        ConnectionPoolDefinitionIF cpd = ProxoolFacade.getConnectionPoolDefinition(alias);
        long mcc1 = cpd.getMaximumConnectionCount();

        {
            // Update explicitly using ProxoolFacade
            Properties info = buildProperties();
            info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "2");
            ProxoolFacade.updateConnectionPool(alias, info);
            cpd = ProxoolFacade.getConnectionPoolDefinition(alias);
            long mcc2 = cpd.getMaximumConnectionCount();

            assertTrue(mcc1 != mcc2);
            assertTrue(mcc2 == 2);
        }

        {
            // Update on-the-fly using the driver
            Properties info = buildProperties();
            info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "1");
            getDate(prefix + alias + urlSuffix, info);
            cpd = ProxoolFacade.getConnectionPoolDefinition(alias);
            long mcc2 = cpd.getMaximumConnectionCount();
            assertTrue(mcc1 != mcc2);
            assertTrue(mcc2 == 1);
        }

    }

    /**
     * Check that the logging works
     */
    public void testLog() throws SQLException {

        String alias = "log";

        Properties info = buildProperties();
        ProxoolFacade.registerConnectionPool(prefix + alias + urlSuffix, info);
        getDate(prefix + alias + urlSuffix);

        // Wait for a while for some prototyping and for the log to write.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.out.println(e);
        }

    }

    /**
     * Check that the FileLogger works
     */
    public void testDefinition() throws SQLException {

        String alias = "def";

        Properties info = buildProperties();
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "17");
        ProxoolFacade.registerConnectionPool(prefix + alias + urlSuffix, info);

        {
            ConnectionPoolDefinitionIF cpd = ProxoolFacade.getConnectionPoolDefinition(alias);
            assertTrue(cpd != null);
            assertEquals(17, cpd.getMaximumConnectionCount());
        }

        {
            ConnectionPoolDefinitionIF cpd = ProxoolFacade.getConnectionPoolDefinition("proxool." + alias);
            assertTrue(cpd != null);
            assertEquals(17, cpd.getMaximumConnectionCount());
        }

        {
            ConnectionPoolDefinitionIF cpd = ProxoolFacade.getConnectionPoolDefinition(prefix + alias + urlSuffix);
            assertTrue(cpd != null);
            assertEquals(17, cpd.getMaximumConnectionCount());
        }
    }

    /**
     * If we ask for more simultaneous connections then we have allowed we should gracefully
     * refuse them.
     */
    public void testLoad() throws SQLException {

        String alias = "load";
        Properties info = buildProperties();
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "5");
        ProxoolFacade.registerConnectionPool(prefix + alias + urlSuffix, info);

        for (int i = 0; i < maxThreads; i++) {
            Thread t = new Thread(new Load(alias));
            t.setDaemon(true);
            t.start();
        }

        while (threadCount > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Who cares?
            }
        }

        ConnectionPoolStatisticsIF cps = ProxoolFacade.getConnectionPoolStatistics(alias);
        assertEquals(maxThreads * maxLoops, cps.getConnectionsServedCount() + cps.getConnectionsRefusedCount());
    }

    private Properties buildProperties() {
        Properties info = new Properties();
        info.setProperty("user", USER);
        info.setProperty("password", PASSWORD);
        return info;
    }
    /**
     * Can we have multiple pools?
     */
    public void testMultiple() throws SQLException {

        String alias1 = "pool#1";
        String alias2 = "pool#2";

        // #1
        getDate(prefix + alias1 + urlSuffix);

        // #2
        getDate(prefix + alias2 + urlSuffix);
        getDate(prefix + alias2 + urlSuffix);

        ConnectionPoolStatisticsIF cps1 = ProxoolFacade.getConnectionPoolStatistics(alias1);
        assertEquals(1L, cps1.getConnectionsServedCount());

        ConnectionPoolStatisticsIF cps2 = ProxoolFacade.getConnectionPoolStatistics(alias2);
        assertEquals(2L, cps2.getConnectionsServedCount());

    }

    private static void getDate(String urlToUse) {
        getDate(urlToUse, null);
    }

    private static void getDate(String urlToUse, Properties info) {
        Connection connection = null;
        try {
            Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");

            if (info != null) {
                info.setProperty("user", USER);
                info.setProperty("password", PASSWORD);
                connection = DriverManager.getConnection(urlToUse, info);
            } else {
                connection = DriverManager.getConnection(urlToUse, USER, PASSWORD);
            }

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select CURRENT_DATE");
            resultSet.next();

            // System.out.println("date=" + resultSet.getString(1) );

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Who cares?
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            // Ignore (it could just be a "can't get connection" message)
        } finally {
            try {
                if (connection != null) {
                    // This doesn't really close the connection. It just makes it
                    // available in the pool again.
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    private static String urlSuffix = ":org.hsqldb.jdbcDriver:jdbc:hsqldb:.";

    private static String prefix = "proxool.";

    private int threadCount;

    private int maxThreads = 20;

    private int maxLoops = 10;

    class Load implements Runnable {

        private String alias;

        Load(String alias) {
            threadCount++;
            this.alias = alias;
        }

        public void run() {
            for (int i = 0; i < maxLoops; i++) {
                try {
                    getDate(prefix + alias);
                } catch (Exception e) {
                    // Ignore
                }
                Thread.yield();
            }
            threadCount--;
        }

    }

}

/*
 Revision history:
 $Log: GeneralTests.java,v $
 Revision 1.1  2002/09/13 08:14:24  billhorsman
 Initial revision

 Revision 1.5  2002/08/24 20:07:48  billhorsman
 renamed tests

 Revision 1.4  2002/08/24 19:44:13  billhorsman
 fixes for logging

 Revision 1.3  2002/07/10 16:14:47  billhorsman
 widespread layout changes and move constants into ProxoolConstants

 Revision 1.2  2002/07/10 10:04:03  billhorsman
 fixed compile bug. silly me :(

 Revision 1.1  2002/07/04 09:01:53  billhorsman
 More tests

 Revision 1.2  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.1  2002/07/02 09:10:35  billhorsman
 Junit tests

*/

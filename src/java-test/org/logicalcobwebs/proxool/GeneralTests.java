/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.xml.DOMConfigurator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Various tests
 *
 * @version $Revision: 1.3 $, $Date: 2002/09/18 13:48:56 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class GeneralTests extends TestCase {

    private static final String USER = "sa";

    private static final String PASSWORD = "";

    private static final Log LOG = LogFactory.getLog(GeneralTests.class);

    public GeneralTests(String name) {
        super(name);

        DOMConfigurator.configure("log4j.xml");

    }

    protected void setUp() throws Exception {
        super.setUp();
        execute(prefix + "setup" + urlSuffix, "create table test (a int, b varchar)");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        execute(prefix + "setup" + urlSuffix, "drop table test");
    }

    /**
     * Can we refer to the same pool by either the complete URL or the alias?
     */
    public void testAlias() throws SQLException {

        String alias = "alias";

        // Register pool
        execute(prefix + alias + urlSuffix, SELECT_SQL);

        // Get it back by url
        execute(prefix + alias + urlSuffix, SELECT_SQL);

        // Get it back by name
        execute(prefix + alias, SELECT_SQL);

        ConnectionPoolStatisticsIF connectionPoolStatistics = ProxoolFacade.getConnectionPoolStatistics(alias);

        // If the above calls all used the same pool then it should have served exactly 3 connections.s
        assertEquals(3L, connectionPoolStatistics.getConnectionsServedCount());

    }

    /**
     * Can we update a pool definition by passing a new Properties object?
     */
    public void testUpdate() throws SQLException {

        String alias = "update";

        execute(prefix + alias + urlSuffix, SELECT_SQL);

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
            execute(prefix + alias + urlSuffix, info, SELECT_SQL);
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
        execute(prefix + alias + urlSuffix, SELECT_SQL);

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

        final int load = 6;
        final int count = 200;
        int goodHits = 0;

        Connection[] connections = new Connection[count];
        for (int i = 0; i < count; i++) {

            if (i >= load && connections[i - load] != null) {
                connections[i - load].close();
            }

            connections[i] = null;
            try {
                Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");

                if (info == null) {
                    info = buildProperties();
                }
                connections[i] = DriverManager.getConnection(prefix + alias, info);

                Statement statement = null;
                try {
                    statement = connections[i].createStatement();
                    statement.execute(SELECT_SQL);
                    goodHits++;
                } finally {
                    if (statement != null) {
                        try {
                            statement.close();
                        } catch (SQLException e) {
                            LOG.error("Couldn't close statement", e);
                        }
                    }
                }

            } catch (ClassNotFoundException e) {
                LOG.error("Problem finding driver?", e);
            } catch (SQLException e) {
                LOG.debug("Ignorable SQLException", e);
            } catch (Exception e) {
                LOG.error("Unexpected Exception", e);
            }

        }

        ConnectionPoolStatisticsIF cps = ProxoolFacade.getConnectionPoolStatistics(alias);
        LOG.info("Served: " + cps.getConnectionsServedCount());
        LOG.info("Refused: " + cps.getConnectionsRefusedCount());
        assertEquals(count, cps.getConnectionsServedCount() + cps.getConnectionsRefusedCount());
        assertEquals(goodHits, cps.getConnectionsServedCount());
    }

    private static Properties buildProperties() {
        Properties info = new Properties();
        info.setProperty("user", USER);
        info.setProperty("password", PASSWORD);
        info.setProperty("proxool.debug-level", "1");
        return info;
    }

    /**
     * Can we have multiple pools?
     */
    public void testMultiple() throws SQLException {

        String alias1 = "pool#1";
        String alias2 = "pool#2";

        // #1
        execute(prefix + alias1 + urlSuffix, SELECT_SQL);

        // #2
        execute(prefix + alias2 + urlSuffix, SELECT_SQL);
        execute(prefix + alias2 + urlSuffix, SELECT_SQL);

        ConnectionPoolStatisticsIF cps1 = ProxoolFacade.getConnectionPoolStatistics(alias1);
        assertEquals(1L, cps1.getConnectionsServedCount());

        ConnectionPoolStatisticsIF cps2 = ProxoolFacade.getConnectionPoolStatistics(alias2);
        assertEquals(2L, cps2.getConnectionsServedCount());

    }

    private static void execute(String urlToUse, String sql) {
        execute(urlToUse, null, sql);
    }

    private static void execute(String urlToUse, Properties info, String sql) {
        Connection connection = null;
        try {
            Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");

            if (info == null) {
                info = buildProperties();
            }
            connection = DriverManager.getConnection(urlToUse, info);

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

        } catch (ClassNotFoundException e) {
            LOG.error("Problem finding driver?", e);
        } catch (SQLException e) {
            LOG.debug("Ignorable SQLException", e);
        } finally {
            try {
                if (connection != null) {
                    // This doesn't really close the connection. It just makes it
                    // available in the pool again.
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.error("Problem closing connection", e);
            }
        }

    }

    private static String urlSuffix = ":org.hsqldb.jdbcDriver:jdbc:hsqldb:.";

    private static String prefix = "proxool.";

    private static final String SELECT_SQL = "SELECT * FROM test";

}

/*
 Revision history:
 $Log: GeneralTests.java,v $
 Revision 1.3  2002/09/18 13:48:56  billhorsman
 checkstyle and doc

 Revision 1.2  2002/09/17 22:44:19  billhorsman
 improved tests

 Revision 1.1.1.1  2002/09/13 08:14:24  billhorsman
 new

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

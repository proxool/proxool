/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Properties;

/**
 * Various tests
 *
 * @version $Revision: 1.10 $, $Date: 2002/10/29 08:54:04 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class GeneralTests extends TestCase {

    private static final Log LOG = LogFactory.getLog(GeneralTests.class);

    public GeneralTests(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        AllTests.globalSetup();
        TestHelper.setupDatabase();
    }

    protected void tearDown() throws Exception {
        TestHelper.tearDownDatabase();
        AllTests.globalTeardown();
    }

    /**
     * Can we refer to the same pool by either the complete URL or the alias?
     */
    public void testAlias() throws SQLException, ClassNotFoundException {

        String alias = "alias";

        // Register pool
        {
            String url = TestHelper.getFullUrl(alias);
            Connection c = TestHelper.getProxoolConnection(url);
            TestHelper.testConnection(c);
        }

        // Get it back by url
        {
            String url = TestHelper.getFullUrl(alias);
            Connection c = TestHelper.getProxoolConnection(url);
            TestHelper.testConnection(c);
        }

        // Get it back by name
        {
            String url = TestHelper.getSimpleUrl(alias);
            Connection c = TestHelper.getProxoolConnection(url);
            TestHelper.testConnection(c);
        }

        ConnectionPoolStatisticsIF connectionPoolStatistics = ProxoolFacade.getConnectionPoolStatistics(alias);

        // If the above calls all used the same pool then it should have served exactly 3 connections.s
        assertEquals(3L, connectionPoolStatistics.getConnectionsServedCount());

    }

    /**
     * Can we update a pool definition by passing a new Properties object?
     */
    public void testUpdate() throws SQLException, ClassNotFoundException {

        String alias = "update";

        // Register pool
        {
            String url = TestHelper.getFullUrl(alias);
            Connection c = TestHelper.getProxoolConnection(url);
            TestHelper.testConnection(c);
            c.close();
        }

        ConnectionPoolDefinitionIF cpd = ProxoolFacade.getConnectionPoolDefinition(alias);
        long mcc1 = cpd.getMaximumConnectionCount();

        {
            // Update explicitly using ProxoolFacade
            Properties info = TestHelper.buildProperties();
            info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "2");
            ProxoolFacade.updateConnectionPool(alias, info);
            cpd = ProxoolFacade.getConnectionPoolDefinition(alias);
            long mcc2 = cpd.getMaximumConnectionCount();

            assertTrue(mcc1 != mcc2);
            assertTrue(mcc2 == 2);
        }

        {
            // Update on-the-fly using the driver
            Properties info = TestHelper.buildProperties();
            info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "1");
            String url = TestHelper.getSimpleUrl(alias);
            Connection c = TestHelper.getProxoolConnection(url, info);
            TestHelper.testConnection(c);
            cpd = ProxoolFacade.getConnectionPoolDefinition(alias);
            long mcc2 = cpd.getMaximumConnectionCount();
            assertTrue(mcc1 != mcc2);
            assertTrue(mcc2 == 1);
        }

    }

    /**
     * If we ask for more simultaneous connections then we have allowed we should gracefully
     * refuse them.
     */
    public void testLoad() throws SQLException {

        String alias = "load";

        Properties info = TestHelper.buildProperties();
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "5");
        TestHelper.registerPool(alias, info);

        final int load = 6;
        final int count = 20;
        int goodHits = 0;
        final int expectedGoodHits = 17;

        Connection[] connections = new Connection[count];
        for (int i = 0; i < count; i++) {

            if (i >= load && connections[i - load] != null) {
                connections[i - load].close();
            }

            connections[i] = null;
            try {
                if (info == null) {
                    info = TestHelper.buildProperties();
                }
                String url = TestHelper.getSimpleUrl(alias);
                connections[i] = TestHelper.getProxoolConnection(url, info);

                TestHelper.testConnection(connections[i]);
                goodHits++;
            } catch (ClassNotFoundException e) {
                LOG.error("Problem finding driver?", e);
            } catch (SQLException e) {
                LOG.debug(e.getMessage(), e);
            } catch (Exception e) {
                LOG.error("Unexpected Exception", e);
            }

        }

        ConnectionPoolStatisticsIF cps = ProxoolFacade.getConnectionPoolStatistics(alias);
        LOG.info("Served: " + cps.getConnectionsServedCount());
        LOG.info("Refused: " + cps.getConnectionsRefusedCount());
        assertEquals(count, cps.getConnectionsServedCount() + cps.getConnectionsRefusedCount());
        assertEquals(goodHits, cps.getConnectionsServedCount());
        assertEquals(goodHits, expectedGoodHits);
    }

    /**
     * If we ask for more simultaneous connections then we have allowed we should gracefully
     * refuse them.
     */
    public void testInfo() throws SQLException, ClassNotFoundException {

        String alias = "info";
        String url = TestHelper.getSimpleUrl(alias);
        Properties info = TestHelper.buildProperties();
        info.setProperty("proxool.prototype-count", "0");
        info.setProperty("proxool.minimum-connection-count", "0");
        info.setProperty("proxool.maximum-connection-count", "5");
        TestHelper.registerPool(alias, info);

        Collection connectionInfos = null;
        final int arraySize = 5;

        Connection[] connections = new Connection[arraySize];
        try {

            // Open 1 connection
            connections[0] = TestHelper.getProxoolConnection(url);
            {
                connectionInfos = ProxoolFacade.getConnectionInfos(alias);
                assertEquals("Unexpected ConnectionInfo count", connectionInfos.size(), 1);
                ConnectionInfoIF[] ci = new ConnectionInfoIF[connectionInfos.size()];
                connectionInfos.toArray(ci);
                LOG.info("ConnectionInfo[0]=" + ci[0]);
            }

            // Open another
            connections[1] = TestHelper.getProxoolConnection(url);
            {
                connectionInfos = ProxoolFacade.getConnectionInfos(alias);
                assertEquals("Unexpected ConnectionInfo count", connectionInfos.size(), 2);
                ConnectionInfoIF[] ci = new ConnectionInfoIF[connectionInfos.size()];
                connectionInfos.toArray(ci);
                LOG.info("ConnectionInfo[0]=" + ci[0]);
                LOG.info("ConnectionInfo[1]=" + ci[1]);
            }

            // Close the first
            try {
                connections[0].close();
            } catch (SQLException e) {
                LOG.error("Couldn't close connection 0", e);
            }
            {
                connectionInfos = ProxoolFacade.getConnectionInfos(url);
                assertEquals("Unexpected ConnectionInfo count", connectionInfos.size(), 2);
                ConnectionInfoIF[] ci = new ConnectionInfoIF[connectionInfos.size()];
                connectionInfos.toArray(ci);
                LOG.info("ConnectionInfo[0]=" + ci[0]);
                LOG.info("ConnectionInfo[1]=" + ci[1]);
            }
        } catch (Exception e) {
            LOG.error("Problem", e);
        } finally {
            for (int i = 0; i < arraySize; i++) {
                try {
                    if (connections[i] != null) {
                        connections[i].close();
                    }
                } catch (SQLException e) {
                    LOG.error("Couldn't close connection " + i, e);
                }
            }
            {
                connectionInfos = ProxoolFacade.getConnectionInfos(url);
                assertEquals("Unexpected ConnectionInfo count", connectionInfos.size(), 2);
                ConnectionInfoIF[] ci = new ConnectionInfoIF[connectionInfos.size()];
                connectionInfos.toArray(ci);
                LOG.info("ConnectionInfo[0]=" + ci[0]);
                LOG.info("ConnectionInfo[1]=" + ci[1]);
            }
        }

    }

    /**
     * Can we have multiple pools?
     */
    public void testMultiple() throws SQLException, ClassNotFoundException {

        String alias1 = "pool#1";
        String alias2 = "pool#2";

        // #1
        {
            String url = TestHelper.getFullUrl(alias1);
            Connection c = TestHelper.getProxoolConnection(url);
            TestHelper.testConnection(c);
        }

        // #2
        {
            String url = TestHelper.getFullUrl(alias2);
            Connection c = TestHelper.getProxoolConnection(url);
            TestHelper.testConnection(c);
        }

        // #2
        {
            String url = TestHelper.getFullUrl(alias2);
            Connection c = TestHelper.getProxoolConnection(url);
            TestHelper.testConnection(c);
        }

        ConnectionPoolStatisticsIF cps1 = ProxoolFacade.getConnectionPoolStatistics(alias1);
        assertEquals(1L, cps1.getConnectionsServedCount());

        ConnectionPoolStatisticsIF cps2 = ProxoolFacade.getConnectionPoolStatistics(alias2);
        assertEquals(2L, cps2.getConnectionsServedCount());

    }

}

/*
 Revision history:
 $Log: GeneralTests.java,v $
 Revision 1.10  2002/10/29 08:54:04  billhorsman
 fixed testUpdate (wasn't closing a connection)

 Revision 1.9  2002/10/27 12:03:33  billhorsman
 clear up of tests

 Revision 1.8  2002/10/25 10:41:07  billhorsman
 draft changes to test globalSetup

 Revision 1.7  2002/10/23 21:04:54  billhorsman
 checkstyle fixes (reduced max line width and lenient naming convention

 Revision 1.6  2002/10/19 17:00:38  billhorsman
 added performance test, and created TestHelper to make it all simpler

 Revision 1.5  2002/09/19 10:34:47  billhorsman
 new testInfo test

 Revision 1.4  2002/09/19 10:06:39  billhorsman
 improved load test

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

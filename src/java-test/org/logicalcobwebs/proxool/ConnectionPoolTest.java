/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.Properties;

/**
 * Test {@link ConnectionPool}
 *
 * @version $Revision: 1.1 $, $Date: 2003/10/26 16:10:46 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class ConnectionPoolTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(ConnectionPoolTest.class);

    public ConnectionPoolTest(String name) {
        super(name);
    }

    /**
     * If we ask for more simultaneous connections then we have allowed we should gracefully
     * refuse them.
     */
    public void testMaximumConnectionCount() throws Exception {

        String testName = "maximumConnectionCount";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "2");
        info.setProperty(ProxoolConstants.VERBOSE_PROPERTY, "true");
        ProxoolFacade.registerConnectionPool(url, info);

        DriverManager.getConnection(url);
        DriverManager.getConnection(url);

        try {
            DriverManager.getConnection(url);
            fail("Didn't expect to get third connection");
        } catch (SQLException e) {
            LOG.debug("Ignoring expected exception", e);
        }

        assertEquals("activeConnectionCount", 2, ProxoolFacade.getSnapshot(alias, true).getActiveConnectionCount());

    }

    /**
     * test what happens if maximum = minimum = 1
     */
    public void testMaximumEqualsMinimumConnectionCount() throws Exception {

        String testName = "maximumEqualsMinimumConnectionCount";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "1");
        info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, "1");
        info.setProperty(ProxoolConstants.VERBOSE_PROPERTY, "true");
        ProxoolFacade.registerConnectionPool(url, info);

        // opening and closing a connection should have no effect
        Connection c1 = DriverManager.getConnection(url);
        c1.close();

        // Leave this one open for a while
        Connection c2 = DriverManager.getConnection(url);

        try {
            Connection c3 = DriverManager.getConnection(url);
            c3.close();
            fail("Didn't expect to get third connection");
        } catch (SQLException e) {
            LOG.debug("Ignoring expected exception", e);
        }

        assertEquals("activeConnectionCount", 1, ProxoolFacade.getSnapshot(alias, true).getActiveConnectionCount());
        c2.close();

        // Just check it all still works
        DriverManager.getConnection(url).close();

        assertEquals("activeConnectionCount", 0, ProxoolFacade.getSnapshot(alias, true).getActiveConnectionCount());

    }

    /**
     * Checks whether shutdown is patient enough to wait for active connections
     */
    public void testShutdownPatience() throws ProxoolException, SQLException {

        String testName = "shutdownPatience";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);        info.setProperty(ProxoolConstants.VERBOSE_PROPERTY, "true");
        ProxoolFacade.registerConnectionPool(url, info);

        // Open a connection that will close in 5 seconds
        new Thread(new Closer(DriverManager.getConnection(url), 5000)).start();

        long startTime = System.currentTimeMillis();
        ProxoolFacade.removeConnectionPool(alias, 100000);
        long shutdownTime = System.currentTimeMillis() - startTime;
        assertTrue("shutdown took too long", shutdownTime < 50000);
        assertTrue("shutdown was too quick", shutdownTime > 2000);
    }

    /**
     * Checks whether shutdown is impatient enough to shutdown even if
     * some connections are still active
     */
    public void testShutdownImpatience() throws ProxoolException, SQLException {

        String testName = "shutdownImpatience";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.VERBOSE_PROPERTY, "true");
        ProxoolFacade.registerConnectionPool(url, info);

        // Open a connection that will close in 100 seconds
        new Thread(new Closer(DriverManager.getConnection(url), 100000)).start();

        long startTime = System.currentTimeMillis();
        ProxoolFacade.removeConnectionPool(alias, 3000);
        long shutdownTime = System.currentTimeMillis() - startTime;
        assertTrue("shutdown took too long", shutdownTime < 50000);
        assertTrue("shutdown was too quick", shutdownTime > 1000);
    }

    class Closer implements Runnable {

        private Connection connection;

        private long duration;

        public Closer(Connection connection, long duration) {
            this.connection = connection;
            this.duration = duration;
        }

        public void run() {
            long startTime = System.currentTimeMillis();
            try {
                Thread.sleep(duration);
            } catch (InterruptedException e) {
                LOG.error("Awoken", e);
            }
            try {
                connection.close();
                LOG.debug("Connection closed after " + (System.currentTimeMillis() - startTime)
                        + " milliseconds.");
            } catch (SQLException e) {
                LOG.error("Problem closing connection", e);
            }
        }

    }

}

/*
 Revision history:
 $Log: ConnectionPoolTest.java,v $
 Revision 1.1  2003/10/26 16:10:46  billhorsman
 renamed to be more consistent

 Revision 1.9  2003/09/11 23:14:57  billhorsman
 New test for when maximum-connection-count = 1 and minimum-connection-count = 1

 Revision 1.8  2003/03/12 00:14:12  billhorsman
 change thresholds

 Revision 1.7  2003/03/11 01:19:48  billhorsman
 new tests

 Revision 1.6  2003/03/10 15:31:26  billhorsman
 fixes

 Revision 1.5  2003/03/04 10:24:40  billhorsman
 removed try blocks around each test

 Revision 1.4  2003/03/03 17:08:55  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.3  2003/03/03 11:12:04  billhorsman
 fixed licence

 Revision 1.2  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.1  2003/02/27 18:01:47  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.


*/

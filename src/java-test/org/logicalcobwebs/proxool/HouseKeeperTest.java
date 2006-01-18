/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.admin.SnapshotIF;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Test the house keeper in ConnectionPool
 *
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @version $Revision: 1.12 $, $Date: 2006/01/18 14:40:06 $
 * @since Proxool 0.8
 */
public class HouseKeeperTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(HouseKeeperTest.class);

    public HouseKeeperTest(String alias) {
        super(alias);
    }

    /**
     * Test that connections that remain active for longer than the configured
     * time are closed (and destroyed) automatically.
     */
    public void testMaximumActiveTime() throws Exception {

        ConnectionResetter.setTriggerResetException(true);
        String testName = "maximumActiveTime";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MAXIMUM_ACTIVE_TIME_PROPERTY, "1000");
        info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, "1");
        info.setProperty(ProxoolConstants.TRACE_PROPERTY, "true");
        info.setProperty(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY, "1000");
        ProxoolFacade.registerConnectionPool(url, info);

        assertEquals("Shouldn't be any active connections yet", 0, ProxoolFacade.getSnapshot(alias, false).getServedCount());

        final Connection connection = DriverManager.getConnection(url);
        connection.setAutoCommit(false);
        connection.createStatement().executeQuery(TestConstants.HYPERSONIC_TEST_SQL);
        long start = System.currentTimeMillis();

        assertEquals("We just opened 1 connection", 1, ProxoolFacade.getSnapshot(alias, false).getServedCount());

        new ResultMonitor() {
            public boolean check() throws Exception {
                return connection.isClosed();
            }
        }.getResult();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            LOG.debug("Awoken.");
        }

        long elapsed = System.currentTimeMillis() - start;
        assertTrue("Connection has not been closed after " + elapsed + " milliseconds as expected", connection.isClosed());
        assertEquals("Expected the connection to be inactive", 0, ProxoolFacade.getSnapshot(alias, false).getActiveConnectionCount());

        try {
            connection.createStatement().executeQuery(TestConstants.HYPERSONIC_TEST_SQL);
            fail("Calling createStatement() on a closed connection should fail");
        } catch (Exception e) {
            // s'okay. We expected this
            LOG.debug("Ignoring expected exception: " + e.getMessage());
        }

        // Now close the connection ourselves. It's already been closed by the House Keeper but nothing bad should
        // happen if we do it again now.
        connection.close();

        // Let's see if the prototyper builds another one
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            LOG.debug("Awoken.");
        }
        SnapshotIF snapshot = ProxoolFacade.getSnapshot(alias, false);
        assertEquals("activeConnectionCount", 0, snapshot.getActiveConnectionCount());
        assertEquals("availableConnectionCount", 1, snapshot.getAvailableConnectionCount());
        assertEquals("connectionCount", 1, snapshot.getConnectionCount());

    }

    /**
     * Test that connections that remain active for longer than the configured
     * time are closed (and destroyed) automatically. Also, it gets errors during
     * reset. We don't want the connectionCount to be decremented twice.
     */
    public void testMaximumActiveTimeWithResetFailure() throws Exception {

        try {
            ConnectionResetter.setTriggerResetException(true);
            String testName = "maximumActiveTime";
            String alias = testName;

            String url = TestHelper.buildProxoolUrl(alias,
                    TestConstants.HYPERSONIC_DRIVER,
                    TestConstants.HYPERSONIC_TEST_URL);
            Properties info = new Properties();
            info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
            info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
            info.setProperty(ProxoolConstants.MAXIMUM_ACTIVE_TIME_PROPERTY, "1000");
            info.setProperty(ProxoolConstants.TRACE_PROPERTY, "true");
            info.setProperty(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY, "1000");
            ProxoolFacade.registerConnectionPool(url, info);

            assertEquals("Shouldn't be any active connections yet", 0, ProxoolFacade.getSnapshot(alias, false).getServedCount());

            final Connection connection = DriverManager.getConnection(url);
            connection.setAutoCommit(false);
            connection.createStatement().executeQuery(TestConstants.HYPERSONIC_TEST_SQL);
            long start = System.currentTimeMillis();

            assertEquals("We just opened 1 connection", 1, ProxoolFacade.getSnapshot(alias, false).getServedCount());

            new ResultMonitor() {
                public boolean check() throws Exception {
                    return connection.isClosed();
                }
            }.getResult();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                LOG.debug("Awoken.");
            }

            long elapsed = System.currentTimeMillis() - start;
            assertTrue("Connection has not been closed after " + elapsed + " milliseconds as expected", connection.isClosed());
            assertEquals("Expected the connection to be inactive", 0, ProxoolFacade.getSnapshot(alias, false).getActiveConnectionCount());

            try {
                connection.createStatement().executeQuery(TestConstants.HYPERSONIC_TEST_SQL);
                fail("Calling createStatement() on a closed connection should fail");
            } catch (Exception e) {
                // s'okay. We expected this
                LOG.debug("Ignoring expected exception: " + e.getMessage());
            }

            // Now close the connection ourselves. It's already been closed by the House Keeper but nothing bad should
            // happen if we do it again now.
            connection.close();

            // Let's see if the prototyper builds another one
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                LOG.debug("Awoken.");
            }
            SnapshotIF snapshot = ProxoolFacade.getSnapshot(alias, false);
            assertEquals("activeConnectionCount", 0, snapshot.getActiveConnectionCount());
            assertEquals("availableConnectionCount", 0, snapshot.getAvailableConnectionCount());
            assertEquals("connectionCount", 0, snapshot.getConnectionCount());
        } finally {
            // Back to normal
            ConnectionResetter.setTriggerResetException(false);
        }

    }

    /**
     * Test that house keeper destroys connections that fail configured
     * the test sql
     */
    public void testHouseKeeperTestSql() throws Exception {

        String testName = "houseKeeperTestSql";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.HOUSE_KEEPING_TEST_SQL_PROPERTY, "SELECT NOW");
        info.setProperty(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY, "1000");
        ProxoolFacade.registerConnectionPool(url, info);

        DriverManager.getConnection(url).close();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            LOG.debug("Awoken.");
        }

        DriverManager.getConnection(url).close();
    }

    /**
     * Test that house keeper destroys connections that fail configured
     * the test sql
     */
    public void testInvalidBeforeUse() throws Exception {

        String testName = "invalidBeforeUse";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.HOUSE_KEEPING_TEST_SQL_PROPERTY, "Invalid test");
        info.setProperty(ProxoolConstants.TEST_BEFORE_USE_PROPERTY, Boolean.TRUE.toString());
        info.setProperty(ProxoolConstants.VERBOSE_PROPERTY, Boolean.TRUE.toString());
        info.setProperty(ProxoolConstants.TRACE_PROPERTY, Boolean.TRUE.toString());
        ProxoolFacade.registerConnectionPool(url, info);

        // This should trigger a test followed the actual executed command. Because we've
        // deliberately made the test invalid, we should get an exception when getting a
        // connection
        Connection connection = null;
        Statement s = null;
        try {
            connection = DriverManager.getConnection(url);
            s = connection.createStatement();
            s.execute(TestConstants.HYPERSONIC_TEST_SQL);
            fail("Expected to get an exception because the test failed");
        } catch (SQLException e) {
            // Log message only so we don't get a worrying stack trace
            LOG.debug("Expected exception: " + e.getMessage());
        }

    }

    /**
     * Test that house keeper destroys connections that fail configured
     * the test sql
     */
    public void testInvalidAfterUse() throws Exception {

        String testName = "invalidAfterUse";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.HOUSE_KEEPING_TEST_SQL_PROPERTY, "Invalid test");
        info.setProperty(ProxoolConstants.TEST_AFTER_USE_PROPERTY, Boolean.TRUE.toString());
        info.setProperty(ProxoolConstants.VERBOSE_PROPERTY, Boolean.TRUE.toString());
        info.setProperty(ProxoolConstants.TRACE_PROPERTY, Boolean.TRUE.toString());
        ProxoolFacade.registerConnectionPool(url, info);

        // This should trigger a test as soon as we close the connection. Because we've
        // deliberately made the test invalid then it should get thrown away
        Connection connection = null;
        Statement s = null;
        try {
            connection = DriverManager.getConnection(url);
            s = connection.createStatement();
            s.execute(TestConstants.HYPERSONIC_TEST_SQL);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        // There should be no available connections. We don't have a minimum setup and the one we
        // just created on demand got thrown away because it failed its test
        assertEquals("Available connections", 0, ProxoolFacade.getSnapshot(alias).getAvailableConnectionCount());

    }

    public void testBeforeAndAfterUse() throws Exception {

        String testName = "beforeAndAfterUse";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.HOUSE_KEEPING_TEST_SQL_PROPERTY, TestConstants.HYPERSONIC_TEST_SQL);
        info.setProperty(ProxoolConstants.TEST_BEFORE_USE_PROPERTY, Boolean.TRUE.toString());
        info.setProperty(ProxoolConstants.TEST_AFTER_USE_PROPERTY, Boolean.TRUE.toString());
        info.setProperty(ProxoolConstants.VERBOSE_PROPERTY, Boolean.TRUE.toString());
        info.setProperty(ProxoolConstants.TRACE_PROPERTY, Boolean.TRUE.toString());
        ProxoolFacade.registerConnectionPool(url, info);

        Connection connection = null;
        Statement s = null;
        try {
            connection = DriverManager.getConnection(url);
            s = connection.createStatement();
            s.execute(TestConstants.HYPERSONIC_TEST_SQL);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        // There should be one available connection.
        assertEquals("Available connections", 1, ProxoolFacade.getSnapshot(alias).getAvailableConnectionCount());

    }

}

/*
Revision history:
$Log: HouseKeeperTest.java,v $
Revision 1.12  2006/01/18 14:40:06  billhorsman
Unbundled Jakarta's Commons Logging.

Revision 1.11  2005/10/07 08:11:34  billhorsman
New test for reset failure

Revision 1.10  2005/10/02 12:30:59  billhorsman
Improved test by checking connectionCount

Revision 1.9  2004/06/02 21:05:19  billhorsman
Don't log worrying stack traces for expected exceptions.

Revision 1.8  2003/09/30 18:40:16  billhorsman
New tests for test-before-use and test-after-use

Revision 1.7  2003/09/11 23:58:05  billhorsman
New test for house-keeper-test-sql

Revision 1.6  2003/03/04 10:24:40  billhorsman
removed try blocks around each test

Revision 1.5  2003/03/03 17:08:57  billhorsman
all tests now extend AbstractProxoolTest

Revision 1.4  2003/03/03 11:12:04  billhorsman
fixed licence

Revision 1.3  2003/03/02 00:53:49  billhorsman
more robust wait

Revision 1.2  2003/03/01 15:27:24  billhorsman
checkstyle

Revision 1.1  2003/02/27 18:01:48  billhorsman
completely rethought the test structure. it's now
more obvious. no new tests yet though.

*/
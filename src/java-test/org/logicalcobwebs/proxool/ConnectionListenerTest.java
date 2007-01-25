/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.util.Properties;

/**
 * Test that registering a {@link ConnectionListenerIF} with the {@link ProxoolFacade}
 * works.
 *
 * @version $Revision: 1.17 $, $Date: 2007/01/25 23:38:24 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class ConnectionListenerTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(ConnectionListenerTest.class);

    private int onBirthCalls;
    private int onDeathCalls;
    private int onExecuteCalls;
    private int onFailCalls;

    /**
     * @see junit.framework.TestCase#TestCase
     */
    public ConnectionListenerTest(String s) {
        super(s);
    }

    /**
     * Test that multiple connection listeners can be added through ProxoolFacade,
     * and that they get the expected events.
     * @throws Exception if the test fails.
     */
    public void testAddConnectionListener() throws Exception {
        clear();
        String alias = "connectionListenerTest";
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "2");
        info.setProperty(ProxoolConstants.SIMULTANEOUS_BUILD_THROTTLE_PROPERTY, "1");
        info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, "0");
        Connection connection1 = DriverManager.getConnection(url, info);
        ProxoolFacade.addConnectionListener(alias, new TestConnectionListener());
        ProxoolFacade.addConnectionListener(alias, new TestConnectionListener());
        Connection connection2 = DriverManager.getConnection(url);
        
        // provoke execution error
        boolean errorOccured = false;
        try {
            connection1.createStatement().executeQuery("DINGO");
        } catch (SQLException e) {
            // we want this.
            errorOccured = true;
        }
        assertTrue("We failed to provoke a connection failure.", errorOccured);
        
        // following statement should be ok
        connection2.createStatement().executeQuery(TestConstants.HYPERSONIC_TEST_SQL);
        
        // close both connections
        connection1.close();
        connection2.close();
        
        // shutdown connection pool
        ProxoolFacade.removeConnectionPool(alias);
        
        // test results
        assertTrue("Expected 2 onBirth calls, but got " + this.onBirthCalls + ".", this.onBirthCalls == 2);
        assertTrue("Expected 2 onExecute calls, but got " + this.onExecuteCalls + ".", this.onExecuteCalls == 2);
        assertTrue("Expected 2 onFail calls, but got " + this.onFailCalls + ".", this.onFailCalls == 2);
        assertTrue("Expected 4 onDeath calls, but got " + this.onDeathCalls + ".", this.onDeathCalls == 4);
    }

    /**
     * See whether the command parameter passed to {@link ConnectionListenerIF#onFail(java.lang.String, java.lang.Exception)}
     * is correct. And assume it is also right for onExecute.
     * @throws Exception if the test fails.
     */
    public void testExecuteCommand() throws Exception {
        clear();
        String alias = "executeCommand";
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        Connection connection1 = DriverManager.getConnection(url, info);
        final TestConnectionListener tcl = new TestConnectionListener();
        ProxoolFacade.addConnectionListener(alias, tcl);

        Statement createStatement = connection1.createStatement();
        createStatement.execute("CREATE TABLE NOTHING (a boolean, b datetime, c integer, d decimal, e varchar)");


        // provoke execution error
        java.util.Date date = new java.util.Date();
        PreparedStatement ps = connection1.prepareStatement("select * from NOTHING where a = ? and b = ? and c = ? and d = ? and e = ?");
        ps.setBoolean(1, true);
        ps.setDate(2, new Date(date.getTime()));
        ps.setInt(3, 3);
        ps.setDouble(4, 4.0);
        ps.setString(5, "test");
        ps.execute();
        LOG.debug(tcl.getCommand());
        assertEquals("command", "select * from NOTHING where a = true and b = '" + AbstractProxyStatement.getDateAsString(date) + "' and c = 3 and d = 4.0 and e = 'test';", tcl.getCommand().trim());

        // Check that it works with no parameters
        final String s2 = "select * from NOTHING;";
        tcl.clear();
        ps = connection1.prepareStatement(s2);
        ps.execute();
        LOG.debug(tcl.getCommand());
        assertEquals("command", s2, tcl.getCommand().trim());

        tcl.clear();
        ps = connection1.prepareStatement(s2);
        ps.execute();
        LOG.debug(tcl.getCommand());
        assertEquals("command", s2, tcl.getCommand().trim());

    }

    /**
     * Test that multiple connection listeners can be added through ProxoolFacade,
     * and then removed, and that they do not receive events after they have been removed.
     * @throws Exception if the test fails.
     */
    public void testRemoveConnectionListener() throws Exception {
        clear();
        String alias = "removeConnectionListenerTest";
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "2");
        info.setProperty(ProxoolConstants.SIMULTANEOUS_BUILD_THROTTLE_PROPERTY, "1");
        info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, "0");
        Connection connection1 = DriverManager.getConnection(url, info);
        TestConnectionListener testConnectionListener1 = new TestConnectionListener();
        TestConnectionListener testConnectionListener2 = new TestConnectionListener();
        ProxoolFacade.addConnectionListener(alias, testConnectionListener1);
        ProxoolFacade.addConnectionListener(alias, testConnectionListener2);
        assertTrue("Failed to remove testConnectionListener1", ProxoolFacade.removeConnectionListener(alias, testConnectionListener1));
        assertTrue("Failed to remove testConnectionListener2", ProxoolFacade.removeConnectionListener(alias, testConnectionListener2));
        ProxoolFacade.removeConnectionListener(alias, testConnectionListener2);
        Connection connection2 = DriverManager.getConnection(url, info);
        
        // provoke execution error
        boolean errorOccured = false;
        try {
            connection1.createStatement().executeQuery("DINGO");
        } catch (SQLException e) {
            // we want this.
            errorOccured = true;
        }
        assertTrue("We failed to proovoke a connection failure.", errorOccured);
        
        // following statement should be ok
        connection2.createStatement().executeQuery(TestConstants.HYPERSONIC_TEST_SQL);
        
        // close connections
        connection1.close();
        connection2.close();
        
        // shutdown connection pool
        ProxoolFacade.removeConnectionPool(alias);
        
        // validate results
        assertTrue("Expected 0 onBirth calls, but got " + this.onBirthCalls + ".", this.onBirthCalls == 0);
        assertTrue("Expected 0 onExecute calls, but got " + this.onExecuteCalls + ".", this.onExecuteCalls == 0);
        assertTrue("Expected 0 onFail calls, but got " + this.onFailCalls + ".", this.onFailCalls == 0);
        assertTrue("Expected 0 onDeath calls, but got " + this.onDeathCalls + ".", this.onDeathCalls == 0);
    }

    private void clear() {
        this.onBirthCalls = 0;
        this.onDeathCalls = 0;
        this.onExecuteCalls = 0;
        this.onFailCalls = 0;
    }

//    /**
//     * Calls {@link AbstractProxoolTest#setUp}
//     * @see junit.framework.TestCase#setUp
//     */
//    protected void setUp() throws Exception {
//        super.setUp();
//        Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
//    }

    class TestConnectionListener implements ConnectionListenerIF {

        String command;

        public void onBirth(Connection connection) throws SQLException {
            onBirthCalls++;
        }

        public void onDeath(Connection connection, int reasonCode) throws SQLException {
            onDeathCalls++;
        }

        public void onExecute(String command, long elapsedTime) {
            onExecuteCalls++;
            this.command = command;
        }

        public void onFail(String command, Exception exception) {
            onFailCalls++;
            this.command = command;
        }

        public String getCommand() {
            return command;
        }

        public void clear() {
            command = null;
        }

        public void onAboutToDie(Connection connection, int reason) throws SQLException {
            // Ignore
        }
    }
}

/*
 Revision history:
 $Log: ConnectionListenerTest.java,v $
 Revision 1.17  2007/01/25 23:38:24  billhorsman
 Scrapped onAboutToDie and altered onDeath signature instead. Now includes reasonCode (see ConnectionListenerIF)

 Revision 1.16  2007/01/25 00:10:24  billhorsman
 New onAboutToDie event for ConnectionListenerIF that gets called if the maximum-active-time is exceeded.

 Revision 1.15  2006/03/23 11:42:20  billhorsman
 Create dummy table so test statements work. With HSQL 1.8 it is the prepareStatement() that throws the exception, not the execute() which means the listeners never gets the event.

 Revision 1.14  2006/01/18 14:40:06  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.13  2004/06/02 20:04:00  billhorsman
 Added test for onExecute command

 Revision 1.12  2004/05/26 17:19:10  brenuart
 Allow JUnit tests to be executed against another database.
 By default the test configuration will be taken from the 'testconfig-hsqldb.properties' file located in the org.logicalcobwebs.proxool package.
 This behavior can be overriden by setting the 'testConfig' environment property to another location.

 Revision 1.11  2003/03/10 23:31:04  billhorsman
 fixed deprecated properties and doc

 Revision 1.10  2003/03/04 10:58:43  billhorsman
 checkstyle

 Revision 1.9  2003/03/04 10:24:40  billhorsman
 removed try blocks around each test

 Revision 1.8  2003/03/03 17:08:55  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.7  2003/03/03 11:12:04  billhorsman
 fixed licence

 Revision 1.6  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.5  2003/02/28 10:26:38  billhorsman
 removed killAllConnections call which should be unnecessary
 and forced me to fix bug in ConnectionPool.shutdown where
 onDeath wasn't getting called. Also used constants for properties
 and used database in db directory (to clean up files)

 Revision 1.4  2003/02/19 15:14:22  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.3  2003/02/19 13:47:51  chr32
 Fixed wrong proxool parameters.

 Revision 1.2  2003/02/18 16:58:12  chr32
 Checkstyle.

 Revision 1.1  2003/02/18 16:51:20  chr32
 Added tests for ConnectionListeners.

*/

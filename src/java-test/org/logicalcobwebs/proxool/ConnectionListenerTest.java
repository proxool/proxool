/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Test that registering a {@link ConnectionListenerIF} with the {@link ProxoolFacade}
 * works.
 *
 * @version $Revision: 1.7 $, $Date: 2003/03/03 11:12:04 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class ConnectionListenerTest extends TestCase {

    private int onBirthCalls;
    private int onDeathCalls;
    private int onExecuteCalls;
    private int onFailCalls;

    /**
     * @see TestCase#TestCase
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
        info.setProperty(ProxoolConstants.MAXIMUM_NEW_CONNECTIONS_PROPERTY, "1");
        info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, "0");
        Connection connection1 = DriverManager.getConnection(url, info);
        ProxoolFacade.addConnectionListener(alias, new TestConnectionListener());
        ProxoolFacade.addConnectionListener(alias, new TestConnectionListener());
        Connection connection2 = DriverManager.getConnection(url);
        boolean errorOccured = false;
        try {
            connection1.createStatement().executeQuery("DINGO");
        } catch (SQLException e) {
            // we want this.
            errorOccured = true;
        }
        assertTrue("We failed to proovoke a connection failure.", errorOccured);
        connection2.createStatement().executeQuery("CALL 1");
        connection1.close();
        connection2.close();
        ProxoolFacade.removeConnectionPool(alias);
        assertTrue("Expected 2 onBirth calls, but got " + this.onBirthCalls + ".", this.onBirthCalls == 2);
        assertTrue("Expected 2 onExecute calls, but got " + this.onExecuteCalls + ".", this.onExecuteCalls == 2);
        assertTrue("Expected 2 onFail calls, but got " + this.onFailCalls + ".", this.onFailCalls == 2);
        assertTrue("Expected 4 onDeath calls, but got " + this.onDeathCalls + ".", this.onDeathCalls == 4);
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
        info.setProperty(ProxoolConstants.MAXIMUM_NEW_CONNECTIONS_PROPERTY, "1");
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
        boolean errorOccured = false;
        try {
            connection1.createStatement().executeQuery("DINGO");
        } catch (SQLException e) {
            // we want this.
            errorOccured = true;
        }
        assertTrue("We failed to proovoke a connection failure.", errorOccured);
        connection2.createStatement().executeQuery("CALL 1");
        connection1.close();
        connection2.close();
        ProxoolFacade.removeConnectionPool(alias);
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

    /**
     * Calls {@link GlobalTest#globalSetup}
     * @see TestCase#setUp
     */
    protected void setUp() throws Exception {
        GlobalTest.globalSetup();
        Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
    }

    /**
     * Calls {@link GlobalTest#globalTeardown}
     * @see TestCase#setUp
     */
    protected void tearDown() throws Exception {
        GlobalTest.globalTeardown();
    }

    class TestConnectionListener implements ConnectionListenerIF {
        public void onBirth (Connection connection) throws SQLException {
            onBirthCalls++;
        }

        public void onDeath (Connection connection) throws SQLException {
            onDeathCalls++;
        }

        public void onExecute (String command, long elapsedTime) {
            onExecuteCalls++;
        }

        public void onFail (String command, Exception exception) {
            onFailCalls++;
        }
    }
}

/*
 Revision history:
 $Log: ConnectionListenerTest.java,v $
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

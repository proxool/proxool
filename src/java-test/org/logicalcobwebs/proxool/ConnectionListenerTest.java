/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Test that registering a {@link ConnectionListenerIF} with the {@link ProxoolFacade}
 * works.
 *
 * @version $Revision: 1.1 $, $Date: 2003/02/18 16:51:20 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: chr32 $ (current maintainer)
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
    public void testAddConnectionListener() throws Exception{
        clear();
        Properties info = new Properties();
        info.setProperty("proxool.maximum-connection1-count", "2");
        info.setProperty("maximum-new-connections", "1");
        info.setProperty("minimum-connection1-count", "0");
        info.setProperty("user", "sa");
        info.setProperty("password", "");
        String alias = "connectionListenerTest";
        String driverClass = "org.hsqldb.jdbcDriver";
        String driverUrl = "jdbc:hsqldb:test";
        String url = "proxool." + alias + ":" + driverClass + ":" + driverUrl;
        Connection connection1 = DriverManager.getConnection(url, info);
        ProxoolFacade.addConnectionListener(alias, new testConnectionListener());
        ProxoolFacade.addConnectionListener(alias, new testConnectionListener());
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
        ProxoolFacade.killAllConnections(alias);
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
    public void testRemoveConnectionListener() throws Exception{
        clear();
        Properties info = new Properties();
        info.setProperty("proxool.maximum-connection1-count", "2");
        info.setProperty("maximum-new-connections", "1");
        info.setProperty("minimum-connection1-count", "0");
        info.setProperty("user", "sa");
        info.setProperty("password", "");
        String alias = "removeConnectionListenerTest";
        String driverClass = "org.hsqldb.jdbcDriver";
        String driverUrl = "jdbc:hsqldb:test";
        String url = "proxool." + alias + ":" + driverClass + ":" + driverUrl;
        Connection connection1 = DriverManager.getConnection(url, info);
        testConnectionListener testConnectionListener1 = new testConnectionListener();
        testConnectionListener testConnectionListener2 = new testConnectionListener();
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
        ProxoolFacade.killAllConnections(alias);
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

    class testConnectionListener implements ConnectionListenerIF {
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
 Revision 1.1  2003/02/18 16:51:20  chr32
 Added tests for ConnectionListeners.

*/

/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;

import java.util.Properties;
import java.sql.Connection;
import java.sql.SQLException;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

/**
 * Test that registering a {@link ConfigurationListenerIF} with the {@link ProxoolFacade}
 * works.
 *
 * @version $Revision: 1.2 $, $Date: 2003/02/26 16:05:50 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class StateListenerTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(StateListenerTest.class);

    private int upState = -1;

    /**
     * @see TestCase#TestCase
     */
    public StateListenerTest(String s) {
        super(s);
    }

    /**
     * Test whether we can add a state listener and that it receives
     * notification of change of state
     */
    public void testAddStateListener() {

        String testName = "addStateListener";
        ProxoolAdapter adapter = null;
        try {
            String alias = testName;
            String url = TestHelper.getFullUrl(alias);
            Properties info = TestHelper.buildProperties();
            info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "1");
            info.setProperty(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY, "5000");
            info.setProperty(ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME_PROPERTY, "5000");
            ProxoolFacade.registerConnectionPool(url, info);

            assertEquals("maximumConnectionCount", 1, ProxoolFacade.getConnectionPoolDefinition(alias).getMaximumConnectionCount());

            StateListenerIF stateListener = new TestStateListener();
            ProxoolFacade.addStateListener(alias, stateListener);

            assertEquals("maximumConnectionCount", 1, ProxoolFacade.getConnectionPoolDefinition(alias).getMaximumConnectionCount());

            Connection c1 = TestHelper.getProxoolConnection(url, null);

            // Test BUSY
            Thread.sleep(6000);
            assertEquals("upState", StateListenerIF.STATE_BUSY, upState);

            assertEquals("maximumConnectionCount", 1, ProxoolFacade.getConnectionPoolDefinition(alias).getMaximumConnectionCount());
            LOG.debug("maximumConnectionCount=" + ProxoolFacade.getConnectionPoolDefinition(alias).getMaximumConnectionCount());

            try {
                Connection c2 = TestHelper.getProxoolConnection(url, null);
                fail("Didn't expect second connection since maximumConnectionCount is 1");
            } catch (SQLException e) {
                // We expect a refusal here
                LOG.debug("Expected refusal", e);
            }

            // Test Overloaded
            Thread.sleep(6000);
            assertEquals("upState", StateListenerIF.STATE_OVERLOADED, upState);

            // Test Busy again
            Thread.sleep(5000);
            assertEquals("upState", StateListenerIF.STATE_BUSY, upState);

            // Test Quiet again
            c1.close();
            Thread.sleep(5000);
            assertEquals("upState", StateListenerIF.STATE_QUIET, upState);

            // Bogus definition -> should be down
            ProxoolFacade.updateConnectionPool("proxool." + alias + ":blah:foo", null);
            ProxoolFacade.killAllConnections(alias);
            Thread.sleep(5000);
            assertEquals("upState", StateListenerIF.STATE_DOWN, upState);

        } catch (Exception e) {
            LOG.error("Whilst performing " + testName, e);
            fail(e.getMessage());
        }

    }


    private void clear() {
        upState = -1;
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

    class TestStateListener implements StateListenerIF {

        public void upStateChanged(int newUpState) {
            LOG.debug("upState: " + upState + " -> " + newUpState);
            upState = newUpState;
        }
    }
}

/*
 Revision history:
 $Log: StateListenerTest.java,v $
 Revision 1.2  2003/02/26 16:05:50  billhorsman
 widespread changes caused by refactoring the way we
 update and redefine pool definitions.

 Revision 1.1  2003/02/19 23:07:57  billhorsman
 new test

 Revision 1.2  2003/02/19 15:14:22  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.1  2003/02/19 13:47:31  chr32
 Added configuration listener test.

 Revision 1.2  2003/02/18 16:58:12  chr32
 Checkstyle.

 Revision 1.1  2003/02/18 16:51:20  chr32
 Added tests for ConnectionListeners.

*/

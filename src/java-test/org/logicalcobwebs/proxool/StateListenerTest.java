/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Test that registering a {@link ConfigurationListenerIF} with the {@link ProxoolFacade}
 * works.
 *
 * @version $Revision: 1.12 $, $Date: 2004/06/02 21:05:19 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class StateListenerTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(StateListenerTest.class);

    /**
     * @see junit.framework.TestCase#TestCase
     */
    public StateListenerTest(String s) {
        super(s);
    }

    /**
     * Test whether we can add a state listener and that it receives
     * notification of change of state
     */
    public void testAddStateListener() throws Exception {

        String testName = "addStateListener";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "1");
        info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, "0");
        info.setProperty(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY, "1000");
        info.setProperty(ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME_PROPERTY, "6000");
        ProxoolFacade.registerConnectionPool(url, info);

        assertEquals("maximumConnectionCount", 1, ProxoolFacade.getConnectionPoolDefinition(alias).getMaximumConnectionCount());

        StateResultMonitor srm = new StateResultMonitor();
        ProxoolFacade.addStateListener(alias, srm);

        assertEquals("maximumConnectionCount", 1, ProxoolFacade.getConnectionPoolDefinition(alias).getMaximumConnectionCount());

        Connection c1 = DriverManager.getConnection(url);

        // Test BUSY
        srm.setExpectedUpState(StateListenerIF.STATE_BUSY);
        assertEquals("Timeout waiting for BUSY", ResultMonitor.SUCCESS, srm.getResult());

        assertEquals("maximumConnectionCount", 1, ProxoolFacade.getConnectionPoolDefinition(alias).getMaximumConnectionCount());

        try {
            Connection c2 = DriverManager.getConnection(url);
            fail("Didn't expect second connection since maximumConnectionCount is 1");
        } catch (SQLException e) {
            // We expect a refusal here
            // Log message only so we don't get a worrying stack trace
            LOG.debug("Ignoring expected refusal: " + e.getMessage());
        }

        // Test Overloaded
        srm.setExpectedUpState(StateListenerIF.STATE_OVERLOADED);
        assertEquals("Timeout waiting for OVERLOADED", ResultMonitor.SUCCESS, srm.getResult());

        // Test Busy again
        srm.setExpectedUpState(StateListenerIF.STATE_BUSY);
        assertEquals("Timeout waiting for BUSY", ResultMonitor.SUCCESS, srm.getResult());

        // Test Quiet again
        c1.close();
        srm.setExpectedUpState(StateListenerIF.STATE_QUIET);
        assertEquals("Timeout waiting for QUIET", ResultMonitor.SUCCESS, srm.getResult());

        // Bogus definition -> should be down
        ProxoolFacade.updateConnectionPool("proxool." + alias + ":blah:foo", null);
        srm.setExpectedUpState(StateListenerIF.STATE_DOWN);
        assertEquals("Timeout waiting for DOWN", ResultMonitor.SUCCESS, srm.getResult());

    }

    class TestStateListener implements StateListenerIF {

        private boolean somethingHappened;

        private int upState;

        public void upStateChanged(int newUpState) {
            LOG.debug("upState: " + upState + " -> " + newUpState);
            upState = newUpState;
            somethingHappened = true;
        }

        boolean isSomethingHappened() {
            return somethingHappened;
        }

        int getUpState() {
            return upState;
        }

        void reset() {
            upState = 0;
            somethingHappened = false;
        }

        void waitForSomethingToHappen(int stateToWaitFor) {

            long start = System.currentTimeMillis();
            while (!somethingHappened) {
                if (upState == stateToWaitFor) {
                    if (!somethingHappened) {
                        LOG.error("Waiting for state = " + stateToWaitFor + " but it's already at that state");
                        break;
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    LOG.error("Awoken", e);
                }
                if (System.currentTimeMillis() - start > 30000) {
                    fail("Timeout waiting for something to happen");
                }
            }

        }

        int zgetNextState(int stateToWaitFor) {
            waitForSomethingToHappen(stateToWaitFor);
            somethingHappened = false;
            return upState;
        }
    }
}

/*
 Revision history:
 $Log: StateListenerTest.java,v $
 Revision 1.12  2004/06/02 21:05:19  billhorsman
 Don't log worrying stack traces for expected exceptions.

 Revision 1.11  2003/03/04 10:58:44  billhorsman
 checkstyle

 Revision 1.10  2003/03/04 10:24:40  billhorsman
 removed try blocks around each test

 Revision 1.9  2003/03/03 17:09:06  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.8  2003/03/03 11:12:05  billhorsman
 fixed licence

 Revision 1.7  2003/03/02 00:37:23  billhorsman
 more robust

 Revision 1.6  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.5  2003/03/01 15:24:09  billhorsman
 tweaked properties

 Revision 1.4  2003/02/28 17:41:13  billhorsman
 more robust wait for state change

 Revision 1.3  2003/02/27 18:01:48  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

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

/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin;

import junit.framework.TestCase;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.logicalcobwebs.proxool.GlobalTest;
import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.TestConstants;
import org.logicalcobwebs.proxool.TestHelper;

import java.sql.DriverManager;
import java.util.Properties;

/**
 * Test {@link StatisticsListenerIF}
 *
 * @version $Revision: 1.7 $, $Date: 2003/03/01 15:49:33 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class StatisticsListenerTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(StatisticsListenerTest.class);

    /**
     * @see TestCase#TestCase
     */
    public StatisticsListenerTest(String s) {
        super(s);
    }

    /**
     * Calls {@link GlobalTest#globalSetup}
     * @see TestCase#setUp
     */
    protected void setUp() throws Exception {
        GlobalTest.globalSetup();
    }

    /**
     * Calls {@link GlobalTest#globalTeardown}
     * @see TestCase#setUp
     */
    protected void tearDown() throws Exception {
        GlobalTest.globalTeardown();
    }

    /**
     * Can we listen to statistics
     */
    public void testListener() throws Exception {

        String testName = "listener";
        String alias = testName;
        try {
            String url = TestHelper.buildProxoolUrl(alias,
                    TestConstants.HYPERSONIC_DRIVER,
                    TestConstants.HYPERSONIC_TEST_URL);
            Properties info = new Properties();
            info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
            info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
            info.setProperty(ProxoolConstants.STATISTICS_PROPERTY, "5s");

            // We don't test whether anything is logged, but this line should make something appear
            info.setProperty(ProxoolConstants.STATISTICS_LOG_LEVEL_PROPERTY, ProxoolConstants.STATISTICS_LOG_LEVEL_DEBUG);

            // Register pool
            ProxoolFacade.registerConnectionPool(url, info);

            // Add listener
            TestListener testListener = new TestListener();
            ProxoolFacade.addStatisticsListener(alias, testListener);
            long lap0 = System.currentTimeMillis();

            // Wait for next statistics so we can guarantee that next set won't
            // be produced whilst we are building connection
            testListener.getNextStatistics();
            long lap1 = System.currentTimeMillis();

            DriverManager.getConnection(url).close();
            StatisticsIF statistics = testListener.getNextStatistics();
            long lap2 = System.currentTimeMillis();

            assertEquals("servedCount (" + (lap1 - lap0) + ", " + (lap2 - lap1) + " ms)", 1L, statistics.getServedCount());

        } catch (Exception e) {
            LOG.error("Whilst performing " + testName, e);
            throw e;
        } finally {
            ProxoolFacade.removeConnectionPool(alias);
        }

    }

    class TestListener implements StatisticsListenerIF {

        private StatisticsIF statistics;

        private boolean somethingHappened;

        public void statistics(String alias, StatisticsIF statistics) {
            this.statistics = statistics;
            somethingHappened = true;
        }

        void reset() {
            statistics = null;
            somethingHappened = false;
        }

        public StatisticsIF getStatistics() {
            return statistics;
        }

        void waitForSomethingToHappen() {

            long start = System.currentTimeMillis();
            while (!somethingHappened) {
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

        StatisticsIF getNextStatistics() {
            somethingHappened = false;
            waitForSomethingToHappen();
            return statistics;
        }

    }
}

/*
 Revision history:
 $Log: StatisticsListenerTest.java,v $
 Revision 1.7  2003/03/01 15:49:33  billhorsman
 fix

 Revision 1.6  2003/03/01 15:38:38  billhorsman
 better assert msg

 Revision 1.5  2003/03/01 15:27:25  billhorsman
 checkstyle

 Revision 1.4  2003/02/27 18:01:48  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 Revision 1.3  2003/02/27 09:45:33  billhorsman
 sleep a little

 Revision 1.2  2003/02/26 16:05:51  billhorsman
 widespread changes caused by refactoring the way we
 update and redefine pool definitions.

 Revision 1.1  2003/02/20 00:33:15  billhorsman
 renamed monitor package -> admin

 Revision 1.5  2003/02/19 23:36:50  billhorsman
 renamed monitor package to admin

 Revision 1.4  2003/02/19 15:14:30  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.3  2003/02/07 17:28:23  billhorsman
 checkstyle and doc

 Revision 1.2  2003/02/07 15:11:33  billhorsman
 checkstyle

 Revision 1.1  2003/02/07 15:10:37  billhorsman
 new admin tests

 */

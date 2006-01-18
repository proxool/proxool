/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.AbstractProxoolTest;
import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.TestConstants;
import org.logicalcobwebs.proxool.TestHelper;

import java.sql.DriverManager;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Test {@link StatisticsListenerIF}
 *
 * @version $Revision: 1.13 $, $Date: 2006/01/18 14:40:05 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class StatisticsListenerTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(StatisticsListenerTest.class);

    /**
     * HH:mm:ss
     */
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("mm:ss");

    /**
     * @see junit.framework.TestCase#TestCase
     */
    public StatisticsListenerTest(String s) {
        super(s);
    }

    /**
     * Can we listen to statistics
     */
    public void testListener() throws Exception {

        String testName = "listener";
        String alias = testName;
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
        Date lap0 = new Date();

        // Wait for next statistics1 so we can guarantee that next set won't
        // be produced whilst we are building connection
        testListener.getNextStatistics();
        Date lap1 = new Date();

        DriverManager.getConnection(url).close();
        Date lap2 = new Date();
        StatisticsIF statistics1 = testListener.getNextStatistics();
        Date lap3 = new Date();
        StatisticsIF statistics2 = testListener.getNextStatistics();
        Date lap4 = new Date();

        StringBuffer detail = new StringBuffer();
        detail.append("lap0:");
        detail.append(TIME_FORMAT.format(lap0));
        detail.append(", lap1:");
        detail.append(TIME_FORMAT.format(lap1));
        detail.append(", lap2:");
        detail.append(TIME_FORMAT.format(lap2));
        detail.append(", lap3:");
        detail.append(TIME_FORMAT.format(lap3));
        detail.append("(");
        detail.append(statistics1.getServedCount());
        detail.append("), lap4:");
        detail.append(TIME_FORMAT.format(lap4));
        detail.append("(");
        detail.append(statistics2.getServedCount());
        detail.append(")");
        assertEquals("servedCount - " + detail, 1L, statistics1.getServedCount());

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
 Revision 1.13  2006/01/18 14:40:05  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.12  2003/09/05 16:27:27  billhorsman
 Better debug output.

 Revision 1.11  2003/03/04 10:58:44  billhorsman
 checkstyle

 Revision 1.10  2003/03/04 10:24:40  billhorsman
 removed try blocks around each test

 Revision 1.9  2003/03/03 17:09:08  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.8  2003/03/03 11:12:05  billhorsman
 fixed licence

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

/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.monitor;

import junit.framework.TestCase;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.logicalcobwebs.proxool.GlobalTest;
import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.TestHelper;

import java.sql.Connection;
import java.util.Properties;

/**
 * Test {@link StatisticsIF}
 *
 * @version $Revision: 1.2 $, $Date: 2003/02/19 15:14:31 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class StatisticsTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(StatisticsTest.class);

    /**
     * @see TestCase#TestCase
     */
    public StatisticsTest(String s) {
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
     * Test whether the statistics we get back are roughly right.
     */
    public void testStatistics() {

        String testName = "statistics";
        String alias = testName;
        try {
            String url = TestHelper.getFullUrl(alias);
            Properties info = TestHelper.buildProperties();
            info.setProperty(ProxoolConstants.STATISTICS_PROPERTY, "10s,15s");
            info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, "1");

            // We don't test whether anything is logged, but this line should make something appear
            info.setProperty(ProxoolConstants.STATISTICS_LOG_LEVEL_PROPERTY, ProxoolConstants.STATISTICS_LOG_LEVEL_DEBUG);

            // Register pool
            ProxoolFacade.registerConnectionPool(url, info);

            // Skip past the first set because they will probably be for only part
            // of the 10s period.
            StatisticsIF statistics = waitForNextStatistics(alias, "10s", null, 20000);


            Connection c = TestHelper.getProxoolConnection(url);
            c.close();

            statistics = waitForNextStatistics(alias, "10s", statistics, 20000);

            assertEquals("servedCount", 1L, statistics.getServedCount());
            assertEquals("servedPerSecond", 0.09, 0.11, statistics.getServedPerSecond());
            assertEquals("refusedCount", 0L, statistics.getRefusedCount());
            assertTrue("averageActiveTime > 0", statistics.getAverageActiveTime() > 0);

        } catch (Exception e) {
            LOG.error("Whilst performing " + testName, e);
            fail(e.getMessage());
        } finally {
            try {
                ProxoolFacade.removeConnectionPool(alias);
            } catch (ProxoolException e) {
                LOG.error("Couldn't shutdown pool", e);
            }
        }

    }

    private StatisticsIF waitForNextStatistics(String alias, String token, StatisticsIF oldStatistics, int timeout) throws ProxoolException {
        long startWaiting = System.currentTimeMillis();
        StatisticsIF statistics = null;
        while (statistics == null || statistics == oldStatistics) {
            if (System.currentTimeMillis() - startWaiting > timeout) {
                fail("Statistics didn't arrive within expected 20 seconds");
            }
            statistics = ProxoolFacade.getStatistics(alias, token);
        }
        return statistics;
    }

    class TestListener implements StatisticsListenerIF {

        private StatisticsIF statistics;;

        public void statistics(String alias, StatisticsIF statistics) {
            this.statistics = statistics;
        }

        public StatisticsIF getStatistics() {
            return statistics;
        }
    }
}

/*
 Revision history:
 $Log: StatisticsTest.java,v $
 Revision 1.2  2003/02/19 15:14:31  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.1  2003/02/07 17:28:36  billhorsman
 *** empty log message ***

  */

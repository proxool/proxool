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
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.TestHelper;

import java.sql.Connection;
import java.util.Properties;

/**
 * Test {@link StatisticsListenerIF}
 *
 * @version $Revision: 1.5 $, $Date: 2003/02/19 23:36:50 $
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
    public void testListener() {

        String testName = "listener";
        String alias = testName;
        try {
            String url = TestHelper.getFullUrl(alias);
            Properties info = TestHelper.buildProperties();
            info.setProperty(ProxoolConstants.STATISTICS_PROPERTY, "10s");

            // We don't test whether anything is logged, but this line should make something appear
            info.setProperty(ProxoolConstants.STATISTICS_LOG_LEVEL_PROPERTY, ProxoolConstants.STATISTICS_LOG_LEVEL_DEBUG);

            // Register pool
            ProxoolFacade.registerConnectionPool(url, info);

            // Add listener
            TestListener tl = new TestListener();
            ProxoolFacade.addStatisticsListener(alias, tl);

            Connection c = TestHelper.getProxoolConnection(url);
            c.close();

            long startWaiting = System.currentTimeMillis();
            while (tl.getStatistics() == null) {
                if (System.currentTimeMillis() - startWaiting > 20000) {
                    fail("Statistics didn't arrive within expected 20 seconds");
                }
            }

            assertEquals("servedCount", 1L, tl.getStatistics().getServedCount());
            LOG.debug("statistics().getServedCount()=" + tl.getStatistics().getServedCount());

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
 $Log: StatisticsListenerTest.java,v $
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

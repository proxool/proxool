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
import org.logicalcobwebs.proxool.ConnectionInfoIF;

import java.sql.Connection;
import java.util.Properties;

/**
 * Test {@link SnapshotIF}
 *
 * @version $Revision: 1.3 $, $Date: 2003/02/19 23:36:50 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class SnapshotTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(SnapshotTest.class);

    /**
     * @see TestCase#TestCase
     */
    public SnapshotTest(String s) {
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

            {
                Connection c = TestHelper.getProxoolConnection(url);
                c.close();

                SnapshotIF snapshot = ProxoolFacade.getSnapshot(alias, true);

                assertEquals("servedCount", 1L, snapshot.getServedCount());
                assertEquals("refusedCount", 0L, snapshot.getRefusedCount());
                assertEquals("availableConnectionCount", 1, snapshot.getAvailableConnectionCount());
                assertEquals("activeConnectionCount", 0, snapshot.getActiveConnectionCount());

                ConnectionInfoIF[] connectionInfos = snapshot.getConnectionInfos();
                assertEquals("connectionInfos.length", 1, connectionInfos.length);
                assertEquals("connectionInfos[0].getStatus()", ConnectionInfoIF.STATUS_AVAILABLE, connectionInfos[0].getStatus());
            }

            {
                Connection c = TestHelper.getProxoolConnection(url);

                SnapshotIF snapshot = ProxoolFacade.getSnapshot(alias, true);

                assertEquals("servedCount", 2L, snapshot.getServedCount());
                assertEquals("refusedCount", 0L, snapshot.getRefusedCount());
                assertEquals("availableConnectionCount", 0, snapshot.getAvailableConnectionCount());
                assertEquals("activeConnectionCount", 1, snapshot.getActiveConnectionCount());

                ConnectionInfoIF[] connectionInfos = snapshot.getConnectionInfos();
                assertEquals("connectionInfos.length", 1, connectionInfos.length);
                assertEquals("connectionInfos[0].getStatus()", ConnectionInfoIF.STATUS_ACTIVE, connectionInfos[0].getStatus());

                c.close();
            }

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

}

/*
 Revision history:
 $Log: SnapshotTest.java,v $
 Revision 1.3  2003/02/19 23:36:50  billhorsman
 renamed monitor package to admin

 Revision 1.2  2003/02/19 15:14:29  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.1  2003/02/07 17:28:36  billhorsman
 *** empty log message ***

 */

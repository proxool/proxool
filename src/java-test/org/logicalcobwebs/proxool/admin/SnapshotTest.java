/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin;

import junit.framework.TestCase;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.logicalcobwebs.proxool.ConnectionInfoIF;
import org.logicalcobwebs.proxool.GlobalTest;
import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.TestConstants;
import org.logicalcobwebs.proxool.TestHelper;
import org.logicalcobwebs.proxool.AbstractProxoolTest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Test {@link SnapshotIF}
 *
 * @version $Revision: 1.6 $, $Date: 2003/03/03 17:09:08 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class SnapshotTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(SnapshotTest.class);

    /**
     * @see TestCase#TestCase
     */
    public SnapshotTest(String s) {
        super(s);
    }

    /**
     * Test whether the statistics we get back are roughly right.
     */
    public void testStatistics() throws Exception {

        String testName = "statistics";
        String alias = testName;
        try {
            String url = TestHelper.buildProxoolUrl(alias,
                    TestConstants.HYPERSONIC_DRIVER,
                    TestConstants.HYPERSONIC_TEST_URL);
            Properties info = new Properties();
            info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
            info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
            info.setProperty(ProxoolConstants.STATISTICS_PROPERTY, "10s,15s");
            info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, "1");

            // We don't test whether anything is logged, but this line should make something appear
            info.setProperty(ProxoolConstants.STATISTICS_LOG_LEVEL_PROPERTY, ProxoolConstants.STATISTICS_LOG_LEVEL_DEBUG);

            // Register pool
            ProxoolFacade.registerConnectionPool(url, info);

            // Wait for prototyper to build connections
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                LOG.debug("Awoken", e);
            }

            {
                Connection c = DriverManager.getConnection(url);
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
                Connection c = DriverManager.getConnection(url);

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
            throw e;
        } finally {
            ProxoolFacade.removeConnectionPool(alias);
        }

    }

}

/*
 Revision history:
 $Log: SnapshotTest.java,v $
 Revision 1.6  2003/03/03 17:09:08  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.5  2003/03/03 11:12:05  billhorsman
 fixed licence

 Revision 1.4  2003/03/01 15:27:25  billhorsman
 checkstyle

 Revision 1.3  2003/02/27 18:01:48  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 Revision 1.2  2003/02/26 16:05:51  billhorsman
 widespread changes caused by refactoring the way we
 update and redefine pool definitions.

 Revision 1.1  2003/02/20 00:33:15  billhorsman
 renamed monitor package -> admin

 Revision 1.3  2003/02/19 23:36:50  billhorsman
 renamed monitor package to admin

 Revision 1.2  2003/02/19 15:14:29  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.1  2003/02/07 17:28:36  billhorsman
 *** empty log message ***

 */

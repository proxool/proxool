/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.logicalcobwebs.proxool.admin.SnapshotIF;
import org.logicalcobwebs.proxool.admin.SnapshotResultMonitor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Test the prototyper in ConnectionPool
 *
 * @version $Revision: 1.8 $, $Date: 2003/03/06 10:11:24 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class PrototyperTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(PrototyperTest.class);

    public PrototyperTest(String alias) {
        super(alias);
    }

    /**
     * Test that spare connections are made as we run out of them
     */
    public void testPrototypeCount() throws Exception {

        final String testName = "prototypeCount";
        final String alias = testName;

        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.VERBOSE_PROPERTY, Boolean.TRUE.toString());
        info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, "0");
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "5");
        info.setProperty(ProxoolConstants.PROTOTYPE_COUNT_PROPERTY, "2");
        info.setProperty(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY, "1000");
        String url = ProxoolConstants.PROXOOL
                + ProxoolConstants.ALIAS_DELIMITER
                + alias
                + ProxoolConstants.URL_DELIMITER
                + TestConstants.HYPERSONIC_DRIVER
                + ProxoolConstants.URL_DELIMITER
                + TestConstants.HYPERSONIC_TEST_URL;
        ProxoolFacade.registerConnectionPool(url, info);

        Connection[] connections = new Connection[6];

        SnapshotResultMonitor srm = new SnapshotResultMonitor(alias) {
            public boolean check(SnapshotIF snapshot) throws Exception {
                SnapshotIF s = ProxoolFacade.getSnapshot(alias);
                return (s.getActiveConnectionCount() == 0
                        && s.getAvailableConnectionCount() == 2);
            }
        };
        assertEquals("Timeout", ResultMonitor.SUCCESS, srm.getResult());
        assertEquals("activeConnectionCount", 0, srm.getSnapshot().getActiveConnectionCount());
        assertEquals("availableConnectionCount", 2, srm.getSnapshot().getAvailableConnectionCount());

        connections[0] = DriverManager.getConnection(url);

        srm = new SnapshotResultMonitor(alias) {
            public boolean check(SnapshotIF snapshot) throws Exception {
                SnapshotIF s = ProxoolFacade.getSnapshot(alias);
                return (s.getActiveConnectionCount() == 1
                        && s.getAvailableConnectionCount() == 2);
            }
        };
        assertEquals("Timeout", ResultMonitor.SUCCESS, srm.getResult());
        assertEquals("activeConnectionCount", 1, srm.getSnapshot().getActiveConnectionCount());
        assertEquals("availableConnectionCount", 2, srm.getSnapshot().getAvailableConnectionCount());

        connections[1] = DriverManager.getConnection(url);
        connections[2] = DriverManager.getConnection(url);
        connections[3] = DriverManager.getConnection(url);

        srm = new SnapshotResultMonitor(alias) {
            public boolean check(SnapshotIF snapshot) throws Exception {
                SnapshotIF s = ProxoolFacade.getSnapshot(alias);
                return (s.getActiveConnectionCount() == 4
                        && s.getAvailableConnectionCount() == 1);
            }
        };
        assertEquals("Timeout", ResultMonitor.SUCCESS, srm.getResult());
        assertEquals("activeConnectionCount", 4, srm.getSnapshot().getActiveConnectionCount());
        assertEquals("availableConnectionCount", 1, srm.getSnapshot().getAvailableConnectionCount());

    }

    /**
     * Test that the minimum number of connections is maintained
     */
    public void testMinimumConnectionCount() throws Exception {

        String testName = "miniumumConnectionCount";
        final String alias = testName;

        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.VERBOSE_PROPERTY, Boolean.TRUE.toString());
        info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, "2");
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "5");
        info.setProperty(ProxoolConstants.PROTOTYPE_COUNT_PROPERTY, "0");
        info.setProperty(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY, "1000");
        String url = TestHelper.buildProxoolUrl(alias, TestConstants.HYPERSONIC_DRIVER, TestConstants.HYPERSONIC_TEST_URL);
        ProxoolFacade.registerConnectionPool(url, info);

        ResultMonitor srm = new SnapshotResultMonitor(alias) {
            public boolean check(SnapshotIF snapshot) throws Exception {
                SnapshotIF s = ProxoolFacade.getSnapshot(alias);
                return (s.getAvailableConnectionCount() == 2);
            }
        };
        assertEquals("Timeout", ResultMonitor.SUCCESS, srm.getResult());

        assertEquals("availableConnectionCount", 2, ProxoolFacade.getSnapshot(alias, false).getAvailableConnectionCount());

    }

}


/*
 Revision history:
 $Log: PrototyperTest.java,v $
 Revision 1.8  2003/03/06 10:11:24  billhorsman
 trap timeouts better

 Revision 1.7  2003/03/05 18:45:17  billhorsman
 better threading

 Revision 1.6  2003/03/04 10:24:40  billhorsman
 removed try blocks around each test

 Revision 1.5  2003/03/03 17:08:57  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.4  2003/03/03 11:12:04  billhorsman
 fixed licence

 Revision 1.3  2003/03/01 15:14:15  billhorsman
 new ResultMonitor to help cope with test threads

 Revision 1.2  2003/03/01 00:39:23  billhorsman
 made more robust

 Revision 1.1  2003/02/27 18:01:48  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 */
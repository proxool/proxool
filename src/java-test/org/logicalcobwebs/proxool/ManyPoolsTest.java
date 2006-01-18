/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.admin.SnapshotResultMonitor;
import org.logicalcobwebs.proxool.admin.SnapshotIF;

import java.util.Properties;

/**
 * Test whether Proxool is happy to run lots of pools. Is it scalable?
 *
 * @version $Revision: 1.4 $, $Date: 2006/01/18 14:40:06 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class ManyPoolsTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(ManyPoolsTest.class);

    public ManyPoolsTest(String alias) {
        super(alias);
    }

    public void testManyPools() throws ProxoolException {

        final String testName = "manyPools";

        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.VERBOSE_PROPERTY, Boolean.TRUE.toString());
        info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, "0");
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "5");
        info.setProperty(ProxoolConstants.PROTOTYPE_COUNT_PROPERTY, "2");
        info.setProperty(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY, "30000");
        info.setProperty(ProxoolConstants.HOUSE_KEEPING_TEST_SQL_PROPERTY, TestConstants.HYPERSONIC_TEST_SQL);
        
        final int poolCount = 100;
        String alias[] = new String[poolCount];
        for (int i = 0; i < poolCount; i++) {
            alias[i] = testName + "_" + i;
            String url = ProxoolConstants.PROXOOL
                    + ProxoolConstants.ALIAS_DELIMITER
                    + alias[i]
                    + ProxoolConstants.URL_DELIMITER
                    + TestConstants.HYPERSONIC_DRIVER
                    + ProxoolConstants.URL_DELIMITER
                    + TestConstants.HYPERSONIC_URL_PREFIX + i;
            ProxoolFacade.registerConnectionPool(url, info);
        }

        SnapshotResultMonitor srm = new SnapshotResultMonitor(alias[poolCount - 1]) {
            public boolean check(SnapshotIF snapshot) throws Exception {
                LOG.debug("Checking availableConnectionCount: " + snapshot.getAvailableConnectionCount());
                return (snapshot.getAvailableConnectionCount() == 2);
            }
        };
        srm.setDelay(2000);
        srm.setTimeout(300000);
        assertEquals("Timeout", ResultMonitor.SUCCESS, srm.getResult());
        assertEquals("activeConnectionCount", 0, srm.getSnapshot().getActiveConnectionCount());

    }
}


/*
 Revision history:
 $Log: ManyPoolsTest.java,v $
 Revision 1.4  2006/01/18 14:40:06  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.3  2004/07/13 20:36:57  billhorsman
 Open a different database for each pool. Otherwise Hypersonic has some threading issues.

 Revision 1.2  2004/05/26 17:19:09  brenuart
 Allow JUnit tests to be executed against another database.
 By default the test configuration will be taken from the 'testconfig-hsqldb.properties' file located in the org.logicalcobwebs.proxool package.
 This behavior can be overriden by setting the 'testConfig' environment property to another location.

 Revision 1.1  2003/03/05 18:49:27  billhorsman
 moved test to right tree

 Revision 1.1  2003/03/05 18:42:33  billhorsman
 big refactor of prototyping and house keeping to
 drastically reduce the number of threads when using
 many pools

 */
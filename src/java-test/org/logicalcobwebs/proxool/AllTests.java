/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Run all tests
 *
 * @version $Revision: 1.26 $, $Date: 2004/03/18 17:10:01 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: chr32 $ (current maintainer)
 * @since Proxool 0.5
 */
public class AllTests {

    /**
     * Run all tests
     *
     * @return a composite test of all Proxool tests.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(ConfigurationListenerTest.class);
        suite.addTestSuite(ConnectionInfoTest.class);
        suite.addTestSuite(ConnectionListenerTest.class);
        suite.addTestSuite(ConnectionResetterTest.class);
        suite.addTestSuite(ProxoolDataSourceTest.class);
        suite.addTestSuite(DriverTest.class);
        suite.addTestSuite(ConnectionPoolTest.class);
        suite.addTestSuite(FatalSqlExceptionTest.class);
        suite.addTestSuite(HibernateTest.class);
        suite.addTestSuite(HouseKeeperTest.class);
        suite.addTestSuite(HypersonicTest.class);
        suite.addTestSuite(ManyPoolsTest.class);
        suite.addTestSuite(PropertyTest.class);
        suite.addTestSuite(PrototyperTest.class);
        suite.addTestSuite(ProxyConnectionTest.class);
        suite.addTestSuite(ProxyDatabaseMetaDataTest.class);
        suite.addTestSuite(ProxyStatementTest.class);
        suite.addTestSuite(RegistrationTest.class);
        suite.addTestSuite(StateListenerTest.class);
        suite.addTestSuite(UpdateDefinitionTest.class);
        // TODO Need more investigation into why this fails sometimes.
        // suite.addTestSuite(PerformanceTest.class);

        // create a wrapper for global initialization code.
        TestSetup wrapper = new TestSetup(suite) {
            public void setUp() throws Exception {
                GlobalTest.globalSetup();
            }
        };

        return wrapper;
    }

}

/*
 Revision history:
 $Log: AllTests.java,v $
 Revision 1.26  2004/03/18 17:10:01  chr32
 Renamed DataSourceTest -> ProxoolDataSourceTest. Added test for factory-configured mode.

 Revision 1.25  2004/03/15 02:46:09  chr32
 Added initial ProxoolDataSourceTest.

 Revision 1.24  2003/11/04 13:23:18  billhorsman
 Added PropetyTest

 Revision 1.23  2003/10/26 16:23:20  billhorsman
 Fixed up test suites

 Revision 1.22  2003/09/28 09:38:39  billhorsman
 New unit test for Hibernate.

 Revision 1.21  2003/04/27 22:11:34  billhorsman
 temporary removal of PerformanceTest

 Revision 1.20  2003/04/27 15:46:11  billhorsman
 moved ProxoolDataSourceTest to sandbox

 Revision 1.19  2003/04/19 13:01:01  billhorsman
 improve tests

 Revision 1.18  2003/03/11 14:58:30  billhorsman
 put PerformanceTest back in the global test

 Revision 1.17  2003/03/03 11:12:03  billhorsman
 fixed licence

 Revision 1.16  2003/02/27 18:01:46  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 Revision 1.15  2003/02/19 23:25:28  billhorsman
 new StateListenerTest

 Revision 1.14  2003/02/19 15:14:22  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.13  2003/02/19 13:47:32  chr32
 Added configuration listener test.

 Revision 1.12  2003/02/18 16:51:19  chr32
 Added tests for ConnectionListeners.

 Revision 1.11  2003/02/06 17:41:02  billhorsman
 now uses imported logging

 Revision 1.10  2003/01/23 11:13:57  billhorsman
 remove PerformanceTest from suite

 Revision 1.9  2002/12/16 17:35:42  chr32
 Removed redundant imports.

 Revision 1.8  2002/12/16 17:06:10  billhorsman
 new test structure

 Revision 1.7  2002/12/15 19:16:58  chr32
 Added JAXPConfigurator test.

 Revision 1.6  2002/11/07 18:53:41  billhorsman
 slight improvement to setup

 Revision 1.5  2002/11/02 11:37:48  billhorsman
 New tests

 Revision 1.4  2002/10/28 21:37:54  billhorsman
 now allows for non-existent log4jPath

 Revision 1.3  2002/10/27 13:05:02  billhorsman
 checkstyle

 Revision 1.2  2002/10/27 12:03:33  billhorsman
 clear up of tests

 Revision 1.1  2002/10/25 10:41:07  billhorsman
 draft changes to test globalSetup

*/

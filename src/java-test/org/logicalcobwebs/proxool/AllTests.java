/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Run all tests
 *
 * @version $Revision: 1.14 $, $Date: 2003/02/19 15:14:22 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
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
        suite.addTestSuite(HypersonicTest.class);
        suite.addTestSuite(GeneralTests.class);
        suite.addTestSuite(ConnectionResetterTest.class);
        suite.addTestSuite(ConnectionListenerTest.class);
        suite.addTestSuite(ConfigurationListenerTest.class);

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

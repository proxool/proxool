/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.configuration;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.logicalcobwebs.proxool.GlobalTest;

/**
 * Run all tests
 *
 * @version $Revision: 1.7 $, $Date: 2003/03/03 11:12:06 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public class AllTests {

    /**
     * Create a composite test of all Proxool configuration tests.
     *
     * @return a composite test of all Proxool configuration tests.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(ConfiguratorTest.class);
        suite.addTestSuite(PropertyConfiguratorTest.class);
        suite.addTestSuite(JAXPConfiguratorTest.class);
        suite.addTestSuite(AvalonConfiguratorTest.class);

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
 Revision 1.7  2003/03/03 11:12:06  billhorsman
 fixed licence

 Revision 1.6  2003/02/19 15:14:26  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.5  2002/12/26 11:35:32  billhorsman
 Added PropertyConfiguratorTest to test suite.

 Revision 1.4  2002/12/23 02:40:21  chr32
 Doc.

 Revision 1.3  2002/12/18 03:13:51  chr32
 Doc.

 Revision 1.2  2002/12/16 17:35:43  chr32
 Removed redundant imports.

 Revision 1.1  2002/12/16 17:06:26  billhorsman
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

/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.logicalcobwebs.proxool.GlobalTest;

/**
 * Run all in admin package tests
 *
 * @version $Revision: 1.3 $, $Date: 2003/02/19 23:36:50 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class AllTests {

    /**
     * Create a composite test of all admin package tests
     * @return test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(StatisticsListenerTest.class);

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
 Revision 1.3  2003/02/19 23:36:50  billhorsman
 renamed monitor package to admin

 Revision 1.2  2003/02/19 15:14:28  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.1  2003/02/07 15:10:36  billhorsman
 new admin tests


*/

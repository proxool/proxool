/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.proxool.monitor;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.logicalcobwebs.proxool.GlobalTest;

/**
 * Run all in monitor package tests
 *
 * @version $Revision: 1.1 $, $Date: 2003/02/07 15:10:36 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class AllTests {

    /**
     * Create a composite test of all monitor package tests
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
 Revision 1.1  2003/02/07 15:10:36  billhorsman
 new monitor tests


*/

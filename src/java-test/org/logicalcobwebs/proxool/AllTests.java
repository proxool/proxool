/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.proxool;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Run all tests
 *
 * @version $Revision: 1.3 $, $Date: 2002/10/27 13:05:02 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public class AllTests {

    private static boolean initialised;

    /**
     * Run all tests
     *
     * @return a composite test of all Proxool tests.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(Configurators.class);
        suite.addTestSuite(Performance.class);
        suite.addTestSuite(GeneralTests.class);

        // create a wrapper for global initialization code.
        TestSetup wrapper = new TestSetup(suite) {
            public void setUp() throws Exception {
                AllTests.globalSetup();
            }
        };

        return wrapper;
    }

    public static synchronized void globalSetup() {
        if (!initialised) {
            String log4jPath = System.getProperty("log4jPath");
            System.out.println(log4jPath);
            DOMConfigurator.configure(log4jPath);
            initialised = true;
        }
    }

    public static synchronized void globalTeardown() {
    }

}

/*
 Revision history:
 $Log: AllTests.java,v $
 Revision 1.3  2002/10/27 13:05:02  billhorsman
 checkstyle

 Revision 1.2  2002/10/27 12:03:33  billhorsman
 clear up of tests

 Revision 1.1  2002/10/25 10:41:07  billhorsman
 draft changes to test globalSetup

*/

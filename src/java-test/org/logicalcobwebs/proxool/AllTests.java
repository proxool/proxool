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
 * @version $Revision: 1.1 $, $Date: 2002/10/25 10:41:07 $
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

        // create a wrapper for global initialization code.
        TestSetup wrapper = new TestSetup(suite) {
            public void setUp() throws Exception {
                AllTests.setup();
            }

            protected void tearDown() throws Exception {
                AllTests.teardown();
            }
        };

        return wrapper;
    }

    public synchronized static void setup() {
        if (!initialised) {
            DOMConfigurator.configure("src/java-test/org/logicalcobwebs/proxool/log4j.xml");
            initialised = true;
        }
    }

    public static void teardown() {

    }

}

/*
 Revision history:
 $Log: AllTests.java,v $
 Revision 1.1  2002/10/25 10:41:07  billhorsman
 draft changes to test setup

*/

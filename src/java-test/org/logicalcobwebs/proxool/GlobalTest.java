/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.log4j.xml.DOMConfigurator;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.TestSetup;

/**
 * Provides a suite of all tests. And some utility methods for setting
 * up the logging.
 *
 * @version $Revision: 1.5 $, $Date: 2003/02/06 17:41:03 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public class GlobalTest {

    private static final Log LOG = LogFactory.getLog(GlobalTest.class);

    private static boolean initialised;

    public static synchronized void globalSetup() {
        if (!initialised) {
            String log4jPath = System.getProperty("log4jPath");
            if (log4jPath != null && log4jPath.length() > 0) {
                try {
                    DOMConfigurator.configureAndWatch(log4jPath);
                } catch (Exception e) {
                    LOG.debug("Can't configure logging using " + log4jPath);
                }
            }

            /* uncomment this if you want to turn on debug loggin to the console
            org.apache.log4j.BasicConfigurator.resetConfiguration();
            org.apache.log4j.BasicConfigurator.configure();
            */

            initialised = true;
        }
    }

    public static synchronized void globalTeardown() {
    }

    /**
     * Run all tests
     *
     * @return a composite test of all Proxool tests.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(org.logicalcobwebs.proxool.AllTests.suite());
        suite.addTest(org.logicalcobwebs.proxool.configuration.AllTests.suite());

        // create a wrapper for global initialization code.
        TestSetup wrapper = new TestSetup(suite) {
            public void setUp() throws Exception {
                GlobalTest.globalSetup();
            }

            protected void tearDown() throws Exception {
                GlobalTest.globalTeardown();
            }
        };
        return wrapper;
    }
}


/*
 Revision history:
 $Log: GlobalTest.java,v $
 Revision 1.5  2003/02/06 17:41:03  billhorsman
 now uses imported logging

 Revision 1.4  2003/01/31 16:38:05  billhorsman
 doc

 Revision 1.3  2002/12/18 03:15:03  chr32
 Added commented-out code that will make logging level DEBUG.

 Revision 1.2  2002/12/16 17:15:12  billhorsman
 fixes

 Revision 1.1  2002/12/16 17:05:25  billhorsman
 new test structure

 */
/**
 * Clever Little Trader
 *
 * Jubilee Group and Logical Cobwebs, 2002
 */
package org.logicalcobwebs.proxool;

import org.apache.log4j.xml.DOMConfigurator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.TestSetup;

/**
 * TODO
 * @version $Revision: 1.2 $, $Date: 2002/12/16 17:15:12 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since TODO
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
        };

        return wrapper;
    }
}


/*
 Revision history:
 $Log: GlobalTest.java,v $
 Revision 1.2  2002/12/16 17:15:12  billhorsman
 fixes

 Revision 1.1  2002/12/16 17:05:25  billhorsman
 new test structure

 */
/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin.jmx;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.logicalcobwebs.proxool.GlobalTest;

/**
 * Run all tests in the jmx package.
 *
 * @version $Revision: 1.5 $, $Date: 2003/10/20 07:40:44 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: chr32 $ (current maintainer)
 * @since Proxool 0.8
 */
public class AllTests {

    /**
     * Create a composite test of all the tests in the jmx package.
     * @return test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(ConnectionPoolMBeanTest.class);
        suite.addTestSuite(MultipleAgentsConnectionPoolMBeanTest.class);

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
 Revision 1.5  2003/10/20 07:40:44  chr32
 Improved tests.

 Revision 1.4  2003/05/06 23:17:12  chr32
 Moving JMX tests back in from sandbox.

 Revision 1.1  2003/03/07 16:35:17  billhorsman
 moved jmx stuff into sandbox until it is tested

 Revision 1.2  2003/03/03 11:12:06  billhorsman
 fixed licence

 Revision 1.1  2003/02/26 19:03:43  chr32
 Init rev.

*/

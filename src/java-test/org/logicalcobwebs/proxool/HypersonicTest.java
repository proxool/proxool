/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

/**
 * Very basic test to see if Hypersonic test database is working
 *
 * @version $Revision: 1.11 $, $Date: 2003/03/03 17:08:57 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public class HypersonicTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(HypersonicTest.class);

    private static final String TEST_TABLE = "test";

    public HypersonicTest(String s) {
        super(s);
    }

    public void testHypersonic() throws Exception {

        String testName = "hypersonic";
        try {
            TestHelper.getDirectConnection().close();
        } catch (Exception e) {
            LOG.error("Whilst performing " + testName, e);
            throw e;
        }

    }

}

/*
 Revision history:
 $Log: HypersonicTest.java,v $
 Revision 1.11  2003/03/03 17:08:57  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.10  2003/03/03 11:12:04  billhorsman
 fixed licence

 Revision 1.9  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.8  2003/02/27 18:01:48  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 Revision 1.7  2003/02/19 15:14:23  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.6  2003/02/06 17:41:03  billhorsman
 now uses imported logging

 Revision 1.5  2002/12/16 17:04:55  billhorsman
 new test structure

 Revision 1.4  2002/11/09 16:01:38  billhorsman
 fix doc

 Revision 1.3  2002/11/02 14:22:16  billhorsman
 Documentation

 Revision 1.2  2002/11/02 13:57:34  billhorsman
 checkstyle

 Revision 1.1  2002/10/29 23:18:30  billhorsman
 Tests that hypersonic db works ok

*/

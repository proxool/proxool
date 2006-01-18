/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.DriverManager;
import java.util.Properties;

/**
 * Tests {@link ProxoolDriver}
 *
 * @version $Revision: 1.6 $, $Date: 2006/01/18 14:40:06 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class DriverTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(DriverTest.class);

    public DriverTest(String alias) {
        super(alias);
    }

    /**
     * Can we refer to the same pool by either the complete URL or the alias?
     */
    public void testAlias() throws Exception {

        String testName = "alias";
        String alias = testName;

        // Register pool
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        DriverManager.getConnection(url, info).close();
        assertEquals("servedCount", 1, ProxoolFacade.getSnapshot(alias).getServedCount());

        // Get it back by url
        url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        DriverManager.getConnection(url).close();
        assertEquals("servedCount", 2, ProxoolFacade.getSnapshot(alias).getServedCount());

        // Get it back by name
        url = TestHelper.buildProxoolUrl(alias);
        DriverManager.getConnection(ProxoolConstants.PROXOOL + "." + alias).close();
        assertEquals("servedCount", 3, ProxoolFacade.getSnapshot(alias).getServedCount());

    }


}


/*
 Revision history:
 $Log: DriverTest.java,v $
 Revision 1.6  2006/01/18 14:40:06  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.5  2003/04/29 12:04:18  billhorsman
 fix test

 Revision 1.4  2003/03/04 10:24:40  billhorsman
 removed try blocks around each test

 Revision 1.3  2003/03/03 17:08:57  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.2  2003/03/03 11:12:04  billhorsman
 fixed licence

 Revision 1.1  2003/02/27 18:01:48  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 */
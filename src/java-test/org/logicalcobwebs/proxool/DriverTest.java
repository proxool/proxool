/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.DriverManager;
import java.util.Properties;

/**
 * Tests {@link ProxoolDriver}
 *
 * @version $Revision: 1.2 $, $Date: 2003/03/03 11:12:04 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class DriverTest extends TestCase {

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
        try {

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
            DriverManager.getConnection(url).close();
            assertEquals("servedCount", 3, ProxoolFacade.getSnapshot(alias).getServedCount());

        } catch (Exception e) {
            LOG.error("Whilst performing " + testName, e);
            throw e;
        } finally {
            ProxoolFacade.removeConnectionPool(alias);
        }
    }


}


/*
 Revision history:
 $Log: DriverTest.java,v $
 Revision 1.2  2003/03/03 11:12:04  billhorsman
 fixed licence

 Revision 1.1  2003/02/27 18:01:48  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 */
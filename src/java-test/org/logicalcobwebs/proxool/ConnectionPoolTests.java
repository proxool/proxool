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
import java.sql.SQLException;
import java.util.Properties;

/**
 * Test {@link ConnectionPool}
 *
 * @version $Revision: 1.3 $, $Date: 2003/03/03 11:12:04 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class ConnectionPoolTests extends TestCase {

    private static final Log LOG = LogFactory.getLog(ConnectionPoolTests.class);

    public ConnectionPoolTests(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        GlobalTest.globalSetup();
    }

    protected void tearDown() throws Exception {
        GlobalTest.globalTeardown();
    }


    /**
     * If we ask for more simultaneous connections then we have allowed we should gracefully
     * refuse them.
     */
    public void testMaximumConnectionCount() throws Exception {

        String testName = "maximumConnectionCount";
        String alias = testName;
        try {
            String url = TestHelper.buildProxoolUrl(alias,
                    TestConstants.HYPERSONIC_DRIVER,
                    TestConstants.HYPERSONIC_TEST_URL);
            Properties info = new Properties();
            info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
            info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
            info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "2");
            ProxoolFacade.registerConnectionPool(url, info);

            DriverManager.getConnection(url);
            DriverManager.getConnection(url);

            try {
                DriverManager.getConnection(url);
                fail("Didn't expect to get third connection");
            } catch (SQLException e) {
                LOG.debug("Ignoring expected exception", e);
            }

            assertEquals("activeConnectionCount", 2, ProxoolFacade.getSnapshot(alias, false).getActiveConnectionCount());

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
 $Log: ConnectionPoolTests.java,v $
 Revision 1.3  2003/03/03 11:12:04  billhorsman
 fixed licence

 Revision 1.2  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.1  2003/02/27 18:01:47  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.


*/

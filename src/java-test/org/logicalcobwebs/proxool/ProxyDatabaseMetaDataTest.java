/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;

import java.util.Properties;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

/**
 * Test whether ProxyDatabaseMetaData works
 *
 * @version $Revision: 1.1 $, $Date: 2003/02/27 18:01:48 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class ProxyDatabaseMetaDataTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(ProxyDatabaseMetaDataTest.class);

    public ProxyDatabaseMetaDataTest(String alias) {
        super(alias);
    }

    /**
     * Calls {@link GlobalTest#globalSetup}
     * @see TestCase#setUp
     */
    protected void setUp() throws Exception {
        GlobalTest.globalSetup();
    }

    /**
     * Calls {@link GlobalTest#globalTeardown}
     * @see TestCase#setUp
     */
    protected void tearDown() throws Exception {
        GlobalTest.globalTeardown();
    }

    /**
     * Test whether we can get the Proxool connection back from the
     * DatabaseMetaData object (rather than the delegate connection)
     */
    public void testGetConnection() throws Exception {

        String testName = "getConnection";
        String alias = testName;

        try {

            // Register pool
            String url = TestHelper.buildProxoolUrl(alias,
                    TestConstants.HYPERSONIC_DRIVER,
                    TestConstants.HYPERSONIC_TEST_URL);
            Properties info = new Properties();
            info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
            info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
            ProxoolFacade.registerConnectionPool(url, info);

            Connection connection = DriverManager.getConnection(url);
            DatabaseMetaData dmd = connection.getMetaData();
            Connection retrievedConnection = dmd.getConnection();

            assertEquals("Retrieved connection not the same", connection, retrievedConnection);
            assertEquals("Retrieved connection not the same", connection.getClass(), retrievedConnection.getClass());

            connection.close();

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
 $Log: ProxyDatabaseMetaDataTest.java,v $
 Revision 1.1  2003/02/27 18:01:48  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 */
/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Test whether ProxyDatabaseMetaData works
 *
 * @version $Revision: 1.5 $, $Date: 2003/03/04 10:24:40 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class ProxyDatabaseMetaDataTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(ProxyDatabaseMetaDataTest.class);

    public ProxyDatabaseMetaDataTest(String alias) {
        super(alias);
    }

    /**
     * Test whether we can get the Proxool connection back from the
     * DatabaseMetaData object (rather than the delegate connection)
     */
    public void testGetConnection() throws Exception {

        String testName = "getConnection";
        String alias = testName;

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

    }

}


/*
 Revision history:
 $Log: ProxyDatabaseMetaDataTest.java,v $
 Revision 1.5  2003/03/04 10:24:40  billhorsman
 removed try blocks around each test

 Revision 1.4  2003/03/03 17:09:05  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.3  2003/03/03 11:12:05  billhorsman
 fixed licence

 Revision 1.2  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.1  2003/02/27 18:01:48  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 */
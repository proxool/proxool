/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Tests whether we have access to the delegate connection
 * @version $Revision: 1.1 $, $Date: 2004/03/23 21:14:24 $
 * @author <a href="mailto:bill@logicalcobwebs.co.uk">Bill Horsman</a>
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.9
 */
public class DelegateConnectionTest extends AbstractProxoolTest {

    /**
     * @see AbstractProxoolTest
     */
    public DelegateConnectionTest(String alias) {
        super(alias);
    }

    /**
     * Get a connection and cast it into the appropriate interface
     */
    public void testDelegateConnection() throws Exception {
        String alias = "delegateConnection";
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        Connection c1 = DriverManager.getConnection(url, info);
        // TODO cast the connection into the appropriate *interface* and see if it works.
        c1.close();
    }

}
/*
 Revision history:
 $Log: DelegateConnectionTest.java,v $
 Revision 1.1  2004/03/23 21:14:24  billhorsman
 new tests

*/
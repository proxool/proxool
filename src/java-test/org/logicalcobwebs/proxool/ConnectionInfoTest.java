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
import java.util.Iterator;
import java.util.Properties;

/**
 * Tests {@link ProxoolFacade#getConnectionInfos}
 *
 * @version $Revision: 1.3 $, $Date: 2003/03/03 11:12:04 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class ConnectionInfoTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(ConnectionInfoTest.class);

    public ConnectionInfoTest(String alias) {
        super(alias);
    }

    /**
     * If we ask for more simultaneous connections then we have allowed we should gracefully
     * refuse them.
     */
    public void testConnectionInfo() throws Exception {

        String testName = "connectionInfo";
        String alias = testName;
        try {
            String url = TestHelper.buildProxoolUrl(alias,
                    TestConstants.HYPERSONIC_DRIVER,
                    TestConstants.HYPERSONIC_TEST_URL);
            Properties info = new Properties();
            info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
            info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
            ProxoolFacade.registerConnectionPool(url, info);

            DriverManager.getConnection(url);
            assertEquals("connectionInfo count", 1, ProxoolFacade.getConnectionInfos(alias).size());

            DriverManager.getConnection(url);
            assertEquals("connectionInfo count", 2, ProxoolFacade.getConnectionInfos(alias).size());

            DriverManager.getConnection(url).close();
            assertEquals("connectionInfo count", 3, ProxoolFacade.getConnectionInfos(alias).size());

            Iterator i = ProxoolFacade.getConnectionInfos(alias).iterator();
            ConnectionInfoIF ci1 = (ConnectionInfoIF) i.next();
            ConnectionInfoIF ci2 = (ConnectionInfoIF) i.next();
            ConnectionInfoIF ci3 = (ConnectionInfoIF) i.next();

            assertEquals("#1 status", ConnectionInfoIF.STATUS_ACTIVE, ci1.getStatus());
            assertEquals("#2 status", ConnectionInfoIF.STATUS_ACTIVE, ci2.getStatus());
            assertEquals("#3 status", ConnectionInfoIF.STATUS_AVAILABLE, ci3.getStatus());

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
 $Log: ConnectionInfoTest.java,v $
 Revision 1.3  2003/03/03 11:12:04  billhorsman
 fixed licence

 Revision 1.2  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.1  2003/02/27 18:01:47  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 */
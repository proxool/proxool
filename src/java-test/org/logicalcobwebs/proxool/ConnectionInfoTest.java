/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.DriverManager;
import java.util.Properties;

/**
 * Tests {@link ProxoolFacade#getConnectionInfos}
 *
 * @version $Revision: 1.7 $, $Date: 2003/04/28 20:02:43 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class ConnectionInfoTest extends AbstractProxoolTest {

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

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        ProxoolFacade.registerConnectionPool(url, info);

        DriverManager.getConnection(url);
        assertEquals("connectionInfo count", 1, ProxoolFacade.getSnapshot(alias, true).getConnectionInfos().length);

        DriverManager.getConnection(url);
        assertEquals("connectionInfo count", 2, ProxoolFacade.getSnapshot(alias, true).getConnectionInfos().length);

        DriverManager.getConnection(url).close();
        assertEquals("connectionInfo count", 3, ProxoolFacade.getSnapshot(alias, true).getConnectionInfos().length);

        ConnectionInfoIF[] connectionInfos = ProxoolFacade.getSnapshot(alias, true).getConnectionInfos();
        assertEquals("activeCount", 2, getCount(connectionInfos, ConnectionInfoIF.STATUS_ACTIVE));
        assertEquals("availableCount", 1, getCount(connectionInfos, ConnectionInfoIF.STATUS_AVAILABLE));

    }

    private int getCount(ConnectionInfoIF[] connectionInfos, int status) {
        int count = 0;
        for (int i = 0; i < connectionInfos.length; i++) {
            ConnectionInfoIF connectionInfo = connectionInfos[i];
            if (connectionInfo.getStatus() == status) {
                count++;
            }
        }
        return count;
    }


}


/*
 Revision history:
 $Log: ConnectionInfoTest.java,v $
 Revision 1.7  2003/04/28 20:02:43  billhorsman
 changed from deprecated getConnectionInfos to Snapshot

 Revision 1.6  2003/03/11 00:38:41  billhorsman
 allowed for connections in different order

 Revision 1.5  2003/03/04 10:24:40  billhorsman
 removed try blocks around each test

 Revision 1.4  2003/03/03 17:08:55  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.3  2003/03/03 11:12:04  billhorsman
 fixed licence

 Revision 1.2  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.1  2003/02/27 18:01:47  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 */
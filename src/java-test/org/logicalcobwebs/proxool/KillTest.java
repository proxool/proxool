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
 * Tests whether we can kill connections when we want to
 * @version $Revision: 1.1 $, $Date: 2004/03/23 21:14:24 $
 * @author <a href="mailto:bill@logicalcobwebs.co.uk">Bill Horsman</a>
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.9
 */
public class KillTest extends AbstractProxoolTest {
    private static final boolean MERCIFUL = true;
    private static final boolean MERCILESS = false;
    private static final boolean DETAILED = true;

    /**
     * @see AbstractProxoolTest
     */
    public KillTest(String alias) {
        super(alias);
    }

    /**
     * @see ProxoolFacade#killAllConnections(java.lang.String, java.lang.String, boolean)
     */
    public void testKillAllMercifully() throws Exception {
        String alias = "killAllMercifully";
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "2");
        Connection c1 = DriverManager.getConnection(url, info);
        Connection c2 = DriverManager.getConnection(url, info);
        c1.close();
        c2.close();
        // There should now be two connections built
        assertEquals("Connection count", 2, ProxoolFacade.getSnapshot(alias, DETAILED).getConnectionInfos().length);
        ProxoolFacade.killAllConnections(alias, "testing", MERCIFUL);
        // None left
        assertEquals("Connection count", 0, ProxoolFacade.getSnapshot(alias, DETAILED).getConnectionInfos().length);
    }

    /**
     * @see ProxoolFacade#killAllConnections(java.lang.String, java.lang.String, boolean)
     */
    public void testKillAllMercilessly() throws Exception {
        String alias = "killAllMercilessly";
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "2");
        Connection c1 = DriverManager.getConnection(url, info);
        Connection c2 = DriverManager.getConnection(url, info);
        // There should now be two connections built
        assertEquals("Connection count", 2, ProxoolFacade.getSnapshot(alias, DETAILED).getConnectionInfos().length);
        ProxoolFacade.killAllConnections(alias, "testing", MERCILESS);
        // None left
        assertEquals("Connection count", 0, ProxoolFacade.getSnapshot(alias, DETAILED).getConnectionInfos().length);
        c1.close();
        c2.close();
    }

    /**
     * @see ProxoolFacade#killConnecton(java.lang.String, long, boolean)
     */
    public void testKillOneById() throws Exception {
        String alias = "killOneById";
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "2");
        Connection c1 = DriverManager.getConnection(url, info);
        Connection c2 = DriverManager.getConnection(url, info);
        c1.close();
        c2.close();
        // There should now be two connections built
        assertEquals("Connection count", 2, ProxoolFacade.getSnapshot(alias, DETAILED).getConnectionInfos().length);
        long id1 = ProxoolFacade.getId(c1);
        long id2 = ProxoolFacade.getId(c2);
        ProxoolFacade.killConnecton(alias, id1, MERCIFUL);
        // One left
        assertEquals("Connection count", 1, ProxoolFacade.getSnapshot(alias, DETAILED).getConnectionInfos().length);
        Connection c3 = DriverManager.getConnection(url, info);
        long id3 = ProxoolFacade.getId(c3);
        Connection c4 = DriverManager.getConnection(url, info);
        long id4 = ProxoolFacade.getId(c4);
        c3.close();
        c4.close();
        assertTrue("Killed ID served", id3 != id1);
        assertTrue("Killed ID served", id4 != id1);
    }
    /**
     * @see ProxoolFacade#killConnecton(java.lang.String, long, boolean)
     */
    public void testKillOneByConnection() throws Exception {
        String alias = "killOneByConnection";
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "2");
        Connection c1 = DriverManager.getConnection(url, info);
        Connection c2 = DriverManager.getConnection(url, info);
        c1.close();
        c2.close();
        // There should now be two connections built
        assertEquals("Connection count", 2, ProxoolFacade.getSnapshot(alias, DETAILED).getConnectionInfos().length);
        long id1 = ProxoolFacade.getId(c1);
        long id2 = ProxoolFacade.getId(c2);
        ProxoolFacade.killConnecton(c1, MERCIFUL);
        // One left
        assertEquals("Connection count", 1, ProxoolFacade.getSnapshot(alias, DETAILED).getConnectionInfos().length);
        Connection c3 = DriverManager.getConnection(url, info);
        long id3 = ProxoolFacade.getId(c3);
        Connection c4 = DriverManager.getConnection(url, info);
        long id4 = ProxoolFacade.getId(c4);
        c3.close();
        c4.close();
        assertTrue("Killed ID served", id3 != id1);
        assertTrue("Killed ID served", id4 != id1);
    }

}
/*
 Revision history:
 $Log: KillTest.java,v $
 Revision 1.1  2004/03/23 21:14:24  billhorsman
 new tests

*/
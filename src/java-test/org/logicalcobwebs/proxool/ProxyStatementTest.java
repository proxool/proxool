/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Test whether ProxyStatement works
 *
 * @version $Revision: 1.6 $, $Date: 2003/08/27 18:03:20 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class ProxyStatementTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(ProxyStatementTest.class);

    public ProxyStatementTest(String alias) {
        super(alias);
    }

    /**
     * That we can get the delegate driver's Statement from the one given by Proxool
     */
    public void testDelegateStatement() throws Exception {

        String testName = "delegateStatement";
        String alias = testName;
        Connection c = null;

        // Register pool
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        ProxoolFacade.registerConnectionPool(url, info);

        c = DriverManager.getConnection(url);
        Statement s = c.createStatement();
        Statement delegateStatement = ProxoolFacade.getDelegateStatement(s);
        LOG.debug("Statement " + s.getClass() + " is delegating to " + delegateStatement.getClass());
        assertTrue("Delegate statement isn't a Hypersonic one as expected.", delegateStatement instanceof org.hsqldb.jdbcStatement);

    }

    /**
     * That we can get the delegate driver's Connection from the one given by Proxool
     */
    public void testDelegateConnection() throws Exception {

        String testName = "delegateConnection";
        String alias = testName;
        Connection c = null;

        // Register pool
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        ProxoolFacade.registerConnectionPool(url, info);

        c = DriverManager.getConnection(url);
        Connection delegateConnection = ProxoolFacade.getDelegateConnection(c);
        LOG.debug("Conneciton " + c.getClass() + " is delegating to " + delegateConnection.getClass());
        assertTrue("Connection isn't a Hypersonic one as expected.", delegateConnection instanceof org.hsqldb.jdbcConnection);

    }

    public void testFatalSqlException() throws Exception {

        String testName = "fatalSqlException";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY, "not found");
        ProxoolFacade.registerConnectionPool(url, info);

        Connection c = DriverManager.getConnection(url);
        ;
        Statement s = c.createStatement();
        try {
            s.execute("drop table foo");
        } catch (SQLException e) {
            // Expected exception (foo doesn't exist)
            LOG.debug("Excepted exception", e);
        }

        c.close();

        assertEquals("availableConnectionCount", 0L, ProxoolFacade.getSnapshot(alias, false).getAvailableConnectionCount());

    }

}


/*
 Revision history:
 $Log: ProxyStatementTest.java,v $
 Revision 1.6  2003/08/27 18:03:20  billhorsman
 added new getDelegateConnection() method

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
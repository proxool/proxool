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
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.util.Properties;

/**
 * Test whether ProxyStatement works
 *
 * @version $Revision: 1.9 $, $Date: 2004/03/23 21:17:23 $
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
     * Test what interfaces are supported when getting a PreparedStatement
     */
    public void testPreparedStatement() throws Exception {

        String testName = "preparedStatement";
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
        PreparedStatement s = c.prepareStatement(TestConstants.HYPERSONIC_TEST_SQL);
        Statement delegateStatement = ProxoolFacade.getDelegateStatement(s);
        LOG.debug("Statement " + s.getClass() + " is delegating to " + delegateStatement.getClass());
        assertTrue("Delegate statement isn't a Hypersonic one as expected.", delegateStatement instanceof org.hsqldb.jdbcPreparedStatement);

    }

    /**
     * Test what interfaces are supported when getting a CallableStatement
     */
    public void testCallableStatement() throws Exception {

        String testName = "callableStatement";
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
        CallableStatement s = c.prepareCall(TestConstants.HYPERSONIC_TEST_SQL);
        s = c.prepareCall(TestConstants.HYPERSONIC_TEST_SQL);
        Statement delegateStatement = ProxoolFacade.getDelegateStatement(s);
        LOG.debug("Statement " + s.getClass() + " is delegating to " + delegateStatement.getClass());
        assertTrue("Delegate statement isn't a Hypersonic one as expected.", delegateStatement instanceof org.hsqldb.jdbcPreparedStatement);

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

}


/*
 Revision history:
 $Log: ProxyStatementTest.java,v $
 Revision 1.9  2004/03/23 21:17:23  billhorsman
 added preparedStatement and callableStatement tests

 Revision 1.8  2003/12/09 18:52:19  billhorsman
 checkstyle

 Revision 1.7  2003/11/05 00:00:52  billhorsman
 Remove redundant test (already in FatalSqlExceptionTest)

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
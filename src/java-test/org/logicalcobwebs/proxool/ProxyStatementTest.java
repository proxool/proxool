/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Test whether ProxyStatement works
 *
 * @version $Revision: 1.12 $, $Date: 2006/01/18 14:40:06 $
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
    public void testDelegateStatement() throws Exception 
	{
        String testName = "delegateStatement";

        // Register pool
        String url = registerPool(testName);

        // get a connection from the pool and create a statement with it
        Connection c = DriverManager.getConnection(url);
        Statement  s = c.createStatement();
        Statement  delegateStatement = ProxoolFacade.getDelegateStatement(s);
        Class 	   delegateStatementClass = delegateStatement.getClass();
        s.close();
        
        LOG.debug("Statement " + s.getClass() + " is delegating to " + delegateStatementClass);
        
        // get a *real* connection directly from the native driver (bypassing the pool)
        Connection realConnection = TestHelper.getDirectConnection();
        Statement  realStatement  = realConnection.createStatement();
        Class	   realStatementClass = realStatement.getClass();
        
        realStatement.close();
        realConnection.close();

	    // are they of the same type ?
        assertEquals("Delegate statement isn't of the expected type.", realStatementClass, delegateStatementClass);
    }

    
    /**
     * Test what interfaces are supported when getting a PreparedStatement
     */
    public void testPreparedStatement() throws Exception {

        String testName = "preparedStatement";

        // Register pool
        String url = registerPool(testName);

        // get a connection from the pool and create a prepare statement with it
        Connection c = DriverManager.getConnection(url);
        PreparedStatement s = c.prepareStatement(TestConstants.HYPERSONIC_TEST_SQL);
        Statement delegateStatement = ProxoolFacade.getDelegateStatement(s);
        Class delegateStatementClass = delegateStatement.getClass();
        s.close();
        c.close();

        LOG.debug("Statement " + s.getClass() + " is delegating to " + delegateStatementClass);
        
        // get a *real* connection directly from the native driver (bypassing the pool)
        Connection realConnection = TestHelper.getDirectConnection();
        Statement realStatement  = realConnection.prepareStatement(TestConstants.HYPERSONIC_TEST_SQL);
        Class realStatementClass = realStatement.getClass();
        
        realStatement.close();
        realConnection.close();
        
        // are they of the same type ?
        assertEquals("Delegate statement isn't of the expected type.", realStatementClass, delegateStatementClass);
    }

    
    /**
     * Test what interfaces are supported when getting a CallableStatement
     */
    public void testCallableStatement() throws Exception {

        String testName = "callableStatement";

        // Register pool
        String url = registerPool(testName);

        // get a connection from the pool and create a callable statement with it
        Connection c = DriverManager.getConnection(url);
        CallableStatement s = c.prepareCall(TestConstants.HYPERSONIC_TEST_SQL);
        Statement delegateStatement = ProxoolFacade.getDelegateStatement(s);
        Class delegateStatementClass = delegateStatement.getClass();
        s.close();
        
        LOG.debug("Statement " + s.getClass() + " is delegating to " + delegateStatementClass);
        
        // get a *real* connection directly from the native driver (bypassing the pool)
        Connection realConnection = TestHelper.getDirectConnection();
        Statement  realStatement  = realConnection.prepareCall(TestConstants.HYPERSONIC_TEST_SQL);
        Class	   realStatementClass = realStatement.getClass();
        
        realStatement.close();
        realConnection.close();
        
        // are they of the same type ?
        assertEquals("Delegate statement isn't of the expected type.", realStatementClass, delegateStatementClass );
    }


    /**
     * That we can get the delegate driver's Connection from the one given by Proxool
     */
    public void testDelegateConnection() throws Exception {

        String testName = "delegateConnection";

        // Register pool
        String url = registerPool(testName);

        // get a connection from the pool
        Connection c = DriverManager.getConnection(url);
        Connection delegateConnection = ProxoolFacade.getDelegateConnection(c);
        Class delegateConnectionClass = delegateConnection.getClass();
        
        LOG.debug("Connection " + c + " is delegating to " + delegateConnectionClass);
        c.close();
        
        // get a *real* connection directly from the native driver (bypassing the pool)
        Connection realConnection = TestHelper.getDirectConnection();
        Class	   realConnectionClass = realConnection.getClass();
        realConnection.close();

        assertEquals("Connection isn't of the expected.", realConnectionClass, delegateConnectionClass);
    }
    
    
    /**
     * 
     * @param alias
     * @throws Exception
     */
    private String registerPool(String alias) throws Exception
	{
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        ProxoolFacade.registerConnectionPool(url, info);
        
        return url;
	}
}


/*
 Revision history:
 $Log: ProxyStatementTest.java,v $
 Revision 1.12  2006/01/18 14:40:06  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.11  2004/07/13 21:32:41  billhorsman
 Close the first connection first before opening the real connection (directly) otherwise you get a "database already in use"error on Windows.

 Revision 1.10  2004/05/26 17:19:09  brenuart
 Allow JUnit tests to be executed against another database.
 By default the test configuration will be taken from the 'testconfig-hsqldb.properties' file located in the org.logicalcobwebs.proxool package.
 This behavior can be overriden by setting the 'testConfig' environment property to another location.

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
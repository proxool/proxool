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
 * @version $Revision: 1.6 $, $Date: 2004/03/23 21:16:05 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class FatalSqlExceptionTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(FatalSqlExceptionTest.class);

    public FatalSqlExceptionTest(String alias) {
        super(alias);
    }


    public void testFatalSqlException() throws Exception {

        String testName = "fatalSqlException";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY, "Table not found");
        info.setProperty(ProxoolConstants.VERBOSE_PROPERTY, String.valueOf(Boolean.TRUE));
        info.setProperty(ProxoolConstants.TRACE_PROPERTY, String.valueOf(Boolean.TRUE));
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        ProxoolFacade.registerConnectionPool(url, info);

        Connection c1 = null;
        long id1 = 0;
        try {
            c1 = DriverManager.getConnection(url);
            id1 = ProxoolFacade.getId(c1);
        } finally {
            if (c1 != null) {
                c1.close();
            }
        }

        Connection c2 = null;
        long id2 = 0;
        try {
            c2 = DriverManager.getConnection(url);
            id2 = ProxoolFacade.getId(c2);
            assertTrue("Expected same connection back", id1 == id2);
            Statement s = c2.createStatement();
            // Doing it twice will guarantee a failure. Even if it exists
            s.execute("drop table Z");
            s.execute("drop table Z");
        } catch (SQLException e) {
            assertTrue("Didn't expect a " + FatalSQLException.class.getName(), !(e instanceof FatalSQLException));
            // Expected exception (foo doesn't exist)
            LOG.debug("Expected exception (safe to ignore)", e);
        } finally {
            if (c2 != null) {
                c2.close();
            }
        }

        Connection c3 = null;
        long id3 = 0;
        try {
            c3 = DriverManager.getConnection(url);
            id3 = ProxoolFacade.getId(c3);
            assertTrue("Expected a different connection", id1 != id3);
        } finally {
            if (c3 != null) {
                c3.close();
            }
        }

    }

    public void testWrappedFatalSqlException() throws Exception {

        String testName = "wrappedFatalSqlException";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY, "Table not found");
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_WRAPPER_CLASS_PROPERTY, FatalSQLException.class.getName());
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        ProxoolFacade.registerConnectionPool(url, info);

        Connection c = null;
        try {
            c = DriverManager.getConnection(url);
            Statement s = c.createStatement();
            s.execute("drop table Z");
        } catch (SQLException e) {
            assertTrue("Expected a " + FatalSQLException.class.getName() + " but got a " + e.getClass().getName() + " instead", e instanceof FatalSQLException);
            // Expected exception (foo doesn't exist)
            LOG.debug("Expected exception (safe to ignore)", e);
        }

        try {
            if (c != null) {
                c.close();
            }
        } catch (SQLException e) {
            LOG.debug("Couldn't close connection", e);
        }

        Thread.sleep(1000);

        // Proxool should automatically throw away that connection that caused a fatal sql exception
        assertEquals("availableConnectionCount", 0L, ProxoolFacade.getSnapshot(alias, false).getAvailableConnectionCount());

    }

    public void testWrappedFatalRuntimeException() throws Exception {

        String testName = "wrappedFatalRuntimeException";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY, "Table not found");
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_WRAPPER_CLASS_PROPERTY, FatalRuntimeException.class.getName());
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        ProxoolFacade.registerConnectionPool(url, info);

        Connection c = null;
        try {
            c = DriverManager.getConnection(url);
            Statement s = c.createStatement();
            s.execute("drop table Z");
        } catch (RuntimeException e) {
            assertTrue("Expected a " + FatalRuntimeException.class.getName() + " but got a " + e.getClass().getName() + " instead", e instanceof FatalRuntimeException);
            // Expected exception (foo doesn't exist)
            LOG.debug("Expected exception (safe to ignore)", e);
        }

        try {
            if (c != null) {
                c.close();
            }
        } catch (SQLException e) {
            LOG.debug("Couldn't close connection", e);
        }

        Thread.sleep(1000);

        // Proxool should automatically throw away that connection that caused a fatal sql exception
        assertEquals("availableConnectionCount", 0L, ProxoolFacade.getSnapshot(alias, false).getAvailableConnectionCount());

    }

    public void testFatalSqlExceptionWrapperNotFound() throws Exception {

        String testName = "fatalSqlExceptionWrapperNotFound";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY, "Table not found");
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_WRAPPER_CLASS_PROPERTY, "org.does.not.Exist");
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        try {
            ProxoolFacade.registerConnectionPool(url, info);
            fail("Registration was expected to have failed");
        } catch (ProxoolException e) {
            LOG.debug("Expected exception", e);
            // That's OK. We're expecting one of these
        }

    }

    public void testFatalSqlExceptionWrapperInvalid() throws Exception {

        String testName = "fatalSqlExceptionWrapperInvalid";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY, "Table not found");
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        // ProxoolException isn't a RuntimeException or an SQLException
        info.setProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_WRAPPER_CLASS_PROPERTY, ProxoolException.class.getName());
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        try {
            ProxoolFacade.registerConnectionPool(url, info);
            fail("Registration was expected to have failed");
        } catch (ProxoolException e) {
            LOG.debug("Expected exception", e);
            // That's OK. We're expecting one of these
        }

    }
}


/*
 Revision history:
 $Log: FatalSqlExceptionTest.java,v $
 Revision 1.6  2004/03/23 21:16:05  billhorsman
 make use of new getId() to compare connections

 Revision 1.5  2003/11/04 23:58:48  billhorsman
 Made more robust (against existing database state)

 Revision 1.4  2003/09/29 17:50:45  billhorsman
 Tests for new wrapper.

 Revision 1.3  2003/09/05 16:59:20  billhorsman
 Tests for wrapped exceptions.

 Revision 1.2  2003/08/27 18:58:11  billhorsman
 Fixed up test

 Revision 1.1  2003/07/23 06:54:48  billhorsman
 draft JNDI changes (shouldn't effect normal operation)

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
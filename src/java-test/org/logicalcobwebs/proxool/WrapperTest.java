/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Tests whether we are wrapping up connections correctly. There disposable
 * wrappers stop the user doing nasty things to the connection after it has
 * been closed.
 * @version $Revision: 1.1 $, $Date: 2004/03/23 21:14:24 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.9
 */
public class WrapperTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(WrapperTest.class);

    /**
     * @see AbstractProxoolTest
     */
    public WrapperTest(String alias) {
        super(alias);
    }

    /**
     * Check that closing a connection twice can't close the same connection for the
     * next user
     */
    public void testDoubleConnection() throws Exception {

        String testName = "alias";
        String alias = testName;

        // Register pool
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT, "1");

        Connection c1 = DriverManager.getConnection(url, info);
        Connection dc1 = ProxoolFacade.getDelegateConnection(c1);
        c1.close();
        assertEquals("servedCount", 1, ProxoolFacade.getSnapshot(alias).getServedCount());
        LOG.debug("c1 = " + c1.toString() + ", dc1 = " + dc1);

        Connection c2 = DriverManager.getConnection(url, info);
        Connection dc2 = ProxoolFacade.getDelegateConnection(c2);
        LOG.debug("c2 = " + c2 + ", dc2 = " + dc2);

        assertEquals("Expected the delegate connection to be the same", dc1, dc2);
        // Closing the first connection should not fail, but it shouldn't do anything either.
        c1.close();
        // Check that the second connection hasn't been effected
        assertTrue("Connection was closed unexpectedly", !c2.isClosed());

        // Trying to use the first connection should fail
        Statement s = null;
        try {
            s = c1.createStatement();
            s.execute(TestConstants.HYPERSONIC_TEST_SQL);
            fail("Expected to get an exception because the test failed");
        } catch (SQLException e) {
            LOG.debug("Expected exception.", e);
        }

        // The second connection should work okay though
        try {
            s = c2.createStatement();
            s.execute(TestConstants.HYPERSONIC_TEST_SQL);
        } catch (SQLException e) {
            fail("Connection failed, but it should have worked");
        }

        c2.close();
        assertTrue("Connection was not closed", c2.isClosed());

    }

    /**
     * Check that nothing bad happens if you close a connection twice
     * @throws SQLException if anything goes wrong
     * @throws ProxoolException if anything goes wrong
     */
    public void testDoubleClose() throws SQLException, ProxoolException {

        String testName = "alias";
        String alias = testName;

        // Register pool
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT, "1");

        Connection c1 = DriverManager.getConnection(url, info);
        c1.close();
        c1.close();

    }

    /**
     * Check that calling createStatement after a connection has been closed
     * throws an exception
     * @throws SQLException if anything goes wrong
     * @throws ProxoolException if anything goes wrong
     */
    public void testCreateStatementAfterClose() throws SQLException, ProxoolException {

        String testName = "alias";
        String alias = testName;

        // Register pool
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT, "1");

        Connection c1 = DriverManager.getConnection(url, info);
        c1.close();
        try {
            c1.createStatement();
            fail("Expected createStatement() to fail after connection was closed");
        } catch (SQLException e) {
            // Ignore (we expected it)
        }
    }

    /**
     * Check that isClosed() returns true after we have closed a connection
     * @throws SQLException if anything goes wrong
     * @throws ProxoolException if anything goes wrong
     */
    public void testIsClosedAfterClose() throws SQLException, ProxoolException {

        String testName = "alias";
        String alias = testName;

        // Register pool
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT, "1");

        Connection c1 = DriverManager.getConnection(url, info);
        c1.close();
        assertTrue("isClosed()", c1.isClosed());
    }

    /**
     * Check that isClosed() returns true after we have closed a connection
     * @throws SQLException if anything goes wrong
     * @throws ProxoolException if anything goes wrong
     */
    public void testHashCode() throws SQLException, ProxoolException {

        String testName = "alias";
        String alias = testName;

        // Register pool
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT, "1");

        Connection c1 = DriverManager.getConnection(url, info);
        LOG.debug(c1 + " = " + c1.hashCode());
        c1.close();
        LOG.debug(c1 + " = " + c1.hashCode());

        Connection c2 = DriverManager.getConnection(url, info);
        LOG.debug(c2 + " = " + c2.hashCode());
        c2.close();
        LOG.debug(c2 + " = " + c2.hashCode());

    }

    /**
     * Check tha equals() works right
     * @throws SQLException if anything goes wrong
     * @throws ProxoolException if anything goes wrong
     */
    public void testEquals() throws SQLException, ProxoolException {

        String testName = "alias";
        String alias = testName;

        // Register pool
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT, "1");

        Connection c1 = DriverManager.getConnection(url, info);
        assertTrue("c1 == c1", c1.equals(c1));
        c1.close();
        assertTrue("c1 == c1", c1.equals(c1));

        Connection c2 = DriverManager.getConnection(url, info);
        assertTrue("c1 != c2", !c1.equals(c2));
        c2.close();
        assertTrue("c1 != c2", !c1.equals(c2));

    }

    /**
     * Check whether {@link ProxoolFacade#getId(java.sql.Connection)} returns
     * sensible values
     * @throws ProxoolException if anything goes wrong
     * @throws SQLException if anything goes wrong
     */
    public void testId() throws ProxoolException, SQLException {
        String testName = "alias";
        String alias = testName;

        // Register pool
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT, "2");

        Connection c1 = DriverManager.getConnection(url, info);
        assertEquals("c1.getId()", 1, ProxoolFacade.getId(c1));

        Connection c2 = DriverManager.getConnection(url, info);
        assertEquals("c2.getId()", 2, ProxoolFacade.getId(c2));
        c1.close();
        assertEquals("c1.getId()", 1, ProxoolFacade.getId(c1));
        c2.close();
        assertEquals("c2.getId()", 2, ProxoolFacade.getId(c2));

    }
}


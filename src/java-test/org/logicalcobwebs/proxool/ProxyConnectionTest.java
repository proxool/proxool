/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Test whether ProxyConnection works
 *
 * @version $Revision: 1.4 $, $Date: 2003/03/03 17:09:04 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class ProxyConnectionTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(ProxyConnectionTest.class);

    public ProxyConnectionTest(String alias) {
        super(alias);
    }

    /**
     * Tests whether a statement gets closed automatically by the
     * Connection. I can't think of a way of asserting this but you should
     * see a line in the log saying it was closed.
     */
    public void testCloseStatement() throws Exception {

        String testName = "closeStatement";
        String alias = testName;
        try {
            String url = TestHelper.buildProxoolUrl(alias,
                    TestConstants.HYPERSONIC_DRIVER,
                    TestConstants.HYPERSONIC_TEST_URL);
            Properties info = new Properties();
            info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
            info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
            ProxoolFacade.registerConnectionPool(url, info);

            Connection c = DriverManager.getConnection(url);
            Statement s = c.createStatement();
            try {
                s.execute("drop table foo");
            } catch (SQLException e) {
                // Expected exception (foo doesn't exist)
                LOG.debug("Ignoring excepted exception", e);
            } finally {
                // this should trigger an automatic close of the statement.
                // Unfortunately, I can't find a way of asserting that this
                // really happens. Hypersonic seems to let me continue
                // to use all the methods on the Statement despite it being
                // closed.
                c.close();
            }

            c = DriverManager.getConnection(url);
            Statement s2 = c.createStatement();
            try {
                s2.execute("drop table foo");
            } catch (SQLException e) {
                // Expected exception (foo doesn't exist)
                LOG.debug("Excepted exception", e);
            } finally {
                if (s2 != null) {
                    s2.close();
                }
                // this should NOT trigger an automatic close of the statement
                // because it's been closed explicitly above
                c.close();
            }

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
 $Log: ProxyConnectionTest.java,v $
 Revision 1.4  2003/03/03 17:09:04  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.3  2003/03/03 11:12:05  billhorsman
 fixed licence

 Revision 1.2  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.1  2003/02/27 18:01:48  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 */
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
 * @version $Revision: 1.1 $, $Date: 2003/07/23 06:54:48 $
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
                "com.mysql.jdbc.Driver",
                "jdbc:mysql://localhost/test");
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY, "link failure");
        ProxoolFacade.registerConnectionPool(url, info);

        for (int i = 0; i < 100; i++) {
            Connection c = null;
            try {
                c = DriverManager.getConnection(url);
                Statement s = c.createStatement();
                s.execute("create table Z (a  int)");
                s.execute("drop table Z");
            } catch (SQLException e) {
                // Expected exception (foo doesn't exist)
                LOG.debug("Expected exception", e);
            }

            try {
                if (c != null) {
                    c.close();
                }
            } catch (SQLException e) {
                LOG.debug("Couldn't close connection", e);
            }

            Thread.sleep(1000);
        }

        assertEquals("availableConnectionCount", 0L, ProxoolFacade.getSnapshot(alias, false).getAvailableConnectionCount());

    }

}


/*
 Revision history:
 $Log: FatalSqlExceptionTest.java,v $
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
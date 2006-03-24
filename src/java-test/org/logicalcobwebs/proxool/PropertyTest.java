/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Tests whether {@link ConnectionPoolDefinition} recognises properties
 * properly
 * @version $Revision: 1.3 $, $Date: 2006/03/24 00:18:46 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8.2
 */
public class PropertyTest extends AbstractProxoolTest {

    public PropertyTest(String alias) {
        super(alias);
    }

    /**
     * Test whether we are successfully passing properties onto the delegate driver. This
     * relies on a feature of Hypersonic 1.7.1 where ResultSetMetaData.isWritable() is
     * unsupported. The default behaviour, however, is just to return a value that maybe
     * incorrect but without throwing an exception. If you set the property jdbc.strict_md = true
     * then Hypersonic does throw an exception. This might change in future versions of Hypersonic
     * so we should keep an eye on this.
     * See <a href="http://hsqldb.sourceforge.net/doc/src/org/hsqldb/jdbcResultSet.html">http://hsqldb.sourceforge.net/doc/src/org/hsqldb/jdbcResultSet.html</a>
     */
    public void testDelegateProperty() throws Exception {

        String testName = "delegateProperty";
        String alias = testName;

        // Register pool
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.HOUSE_KEEPING_TEST_SQL_PROPERTY, TestConstants.HYPERSONIC_TEST_SQL);

        Connection c = null;
        try {
            c = DriverManager.getConnection(url, info);
            Statement s = c.createStatement();
            try {
                s.execute("drop table z");
            } catch (SQLException e) {
                // Probably because it doesn't exist.
            }
            s.execute("create table z (a int)");

            s.execute("select * from z");
            ResultSet rs = s.getResultSet();
            ResultSetMetaData rsmd = rs.getMetaData();
            // by default, this should work without error (even if the value returned is rubbish)
            rsmd.isWritable(1);
        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }
/*
     This doesn't work with HSQLDB 1.8.0. They don't seem to mention the strict_md propertry in their
     documentation anymore.

        // And now test with the strict meta data
        info.setProperty("jdbc.strict_md", "true");
        try {
            c = DriverManager.getConnection(url, info);
            Statement s = c.createStatement();
            s.execute("select * from z");
            ResultSet rs = s.getResultSet();
            ResultSetMetaData rsmd = rs.getMetaData();
            try {
                rsmd.isWritable(1);
                fail("Expected isWritable() to throw unsupported exception");
            } catch (SQLException e) {
                // Expected an exception
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
*/

    }
}
/*
 Revision history:
 $Log: PropertyTest.java,v $
 Revision 1.3  2006/03/24 00:18:46  billhorsman
 Changes for HSQL 1.8

 Revision 1.2  2004/05/26 17:19:09  brenuart
 Allow JUnit tests to be executed against another database.
 By default the test configuration will be taken from the 'testconfig-hsqldb.properties' file located in the org.logicalcobwebs.proxool package.
 This behavior can be overriden by setting the 'testConfig' environment property to another location.

 Revision 1.1  2003/11/04 13:22:43  billhorsman
 New test for delegate properties

  */
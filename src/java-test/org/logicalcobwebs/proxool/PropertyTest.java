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
 * @version $Revision: 1.1 $, $Date: 2003/11/04 13:22:43 $
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
        Connection c = null;
        try {
            c = DriverManager.getConnection(url, info);
            Statement s = c.createStatement();
            try {
                s.execute("drop table z");
            } catch (SQLException e) {
                // Probably because it doesn't exist.
            }
            s.execute("create table z (a int(4))");

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
    }
}
/*
 Revision history:
 $Log: PropertyTest.java,v $
 Revision 1.1  2003/11/04 13:22:43  billhorsman
 New test for delegate properties

  */
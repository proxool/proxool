/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.examples;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The simplest example of all. Just gets a Connection.
 *
 * @version $Revision: 1.7 $, $Date: 2006/01/18 14:40:03 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class Simple {

    private static final Log LOG = LogFactory.getLog(Simple.class);

    private static void withoutProxool() {

        Connection connection = null;
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            try {
                connection = DriverManager.getConnection("jdbc:hsqldb:test");
            } catch (SQLException e) {
                LOG.error("Problem getting connection", e);
            }

            if (connection != null) {
                LOG.info("Got connection :)");
            } else {
                LOG.error("Didn't get connection, which probably means that no Driver accepted the URL");
            }

        } catch (ClassNotFoundException e) {
            LOG.error("Couldn't find driver", e);
        } finally {
            try {
                // Check to see we actually got a connection before we
                // attempt to close it.
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.error("Problem closing connection", e);
            }
        }
    }

    private static void withProxool() {

        Connection connection = null;
        try {
            // NOTE THIS LINE
            Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
            try {
                // NOTE THIS LINE
                connection = DriverManager.getConnection("proxool.example:org.hsqldb.jdbcDriver:jdbc:hsqldb:test");
            } catch (SQLException e) {
                LOG.error("Problem getting connection", e);
            }

            if (connection != null) {
                LOG.info("Got connection :)");
            } else {
                LOG.error("Didn't get connection, which probably means that no Driver accepted the URL");
            }

        } catch (ClassNotFoundException e) {
            LOG.error("Couldn't find driver", e);
        } finally {
            try {
                // Check to see we actually got a connection before we
                // attempt to close it.
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.error("Problem closing connection", e);
            }
        }
    }

    /**
     * Tests getting a connection with and without Proxool
     */
    public static void main(String[] args) {
        withoutProxool();
        withProxool();
    }

}

/*
 Revision history:
 $Log: Simple.java,v $
 Revision 1.7  2006/01/18 14:40:03  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.6  2003/03/03 11:12:02  billhorsman
 fixed licence

 Revision 1.5  2003/02/06 17:41:03  billhorsman
 now uses imported logging

 Revision 1.4  2003/02/06 15:42:48  billhorsman
 updated (overdue!)

 Revision 1.3  2002/12/03 10:54:04  billhorsman
 use hypersonic driver

 Revision 1.2  2002/09/19 10:01:37  billhorsman
 improved error handling and logging

 Revision 1.1  2002/09/19 08:51:09  billhorsman
 created new examples package

 Revision 1.1.1.1  2002/09/13 08:14:27  billhorsman
 new

 Revision 1.4  2002/07/10 16:14:47  billhorsman
 widespread layout changes and move constants into ProxoolConstants

 Revision 1.3  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.2  2002/06/28 11:19:47  billhorsman
 improved doc

*/

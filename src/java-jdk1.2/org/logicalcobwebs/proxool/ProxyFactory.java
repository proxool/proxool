/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;


import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A central place to build proxy objects ({@link org.logicalcobwebs.proxool.ProxyConnection connections}
 * and {@link org.logicalcobwebs.proxool.ProxyStatement statements}).
 *
 * @version $Revision: 1.8 $, $Date: 2003/08/30 15:37:45 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
class ProxyFactory {

    private static final Log LOG = LogFactory.getLog(ProxyFactory.class);

    protected static ProxyConnection buildProxyConnection(long id, ConnectionPool connectionPool, int status) throws SQLException {
        Connection realConnection = null;
        final String url = connectionPool.getDefinition().getUrl();
        realConnection = DriverManager.getConnection(
                url,
                connectionPool.getDefinition().getDelegateProperties());

        return new ProxyConnection(realConnection, id, url, connectionPool, status);
    }

    /**
     * Get a Connection from the ProxyConnection
     *
     * @param proxyConnection where to find the connection
     * @return
     */
    protected static Connection getConnection(ProxyConnectionIF proxyConnection) {
        return (ProxyConnection) proxyConnection;
    }

    /**
     * Gets the real Connection that we got from the delegate driver
     * @param connection proxy connection
     * @return deletgate connection
     */
    protected static Connection getDelegateConnection(Connection connection) {
        try {
            return ((ProxyConnection) connection).getConnection();
        } catch (ClassCastException e) {
            LOG.error("Expected a ProxyConnection but got a " + connection.getClass() + " instead.");
            throw e;
        }
    }

    /**
     * Gets the real Statement that we got from the delegate driver
     * @param statement proxy statement
     * @return delegate statement
     */
    protected static Statement getDelegateStatement(Statement statement) {
        try {
            return ((ProxyStatement) statement).getDelegateStatement();
        } catch (ClassCastException e) {
            LOG.error("Expected a ProxyStatement but got a " + statement.getClass() + " instead.");
            throw e;
        }
    }

    protected static Statement createProxyStatement(Statement delegate, ConnectionPool connectionPool, ProxyConnectionIF proxyConnection, String sqlStatement) {
        return new ProxyStatement(delegate, connectionPool, proxyConnection, sqlStatement);
    }

    /**
     * Create a new DatabaseMetaData from a connection
     * @param connection the proxy connection we are using
     * @return databaseMetaData
     * @throws SQLException if the delegfate connection couldn't get the metaData
     */
    protected static DatabaseMetaData getDatabaseMetaData(Connection connection, ProxyConnectionIF proxyConnection) throws SQLException {
        return new ProxyDatabaseMetaData(connection, proxyConnection);
    }


}

/*
 Revision history:
 $Log: ProxyFactory.java,v $
 Revision 1.8  2003/08/30 15:37:45  billhorsman
 Implement getDelegateConnection.

 Revision 1.7  2003/03/11 14:58:56  billhorsman
 brought inline with main tree

 Revision 1.6  2003/02/19 23:47:23  billhorsman
 fixed copyright

 Revision 1.5  2003/02/12 12:46:30  billhorsman
 brought up to date with recent changes in main source

 Revision 1.4  2003/02/06 23:48:10  billhorsman
 Use imported logging

 Revision 1.3  2003/01/31 16:53:26  billhorsman
 checkstyle

 Revision 1.2  2003/01/31 14:33:19  billhorsman
 fix for DatabaseMetaData

 Revision 1.1  2003/01/28 11:55:04  billhorsman
 new JDK 1.2 patches (functioning but not complete)

 Revision 1.11  2003/01/27 18:26:39  billhorsman
 refactoring of ProxyConnection and ProxyStatement to
 make it easier to write JDK 1.2 patch

 Revision 1.10  2002/12/16 11:15:19  billhorsman
 fixed getDelegateStatement

 Revision 1.9  2002/12/16 10:57:47  billhorsman
 add getDelegateStatement to allow access to the
 delegate JDBC driver's Statement

 Revision 1.8  2002/12/12 10:48:25  billhorsman
 checkstyle

 Revision 1.7  2002/12/08 22:17:35  billhorsman
 debug for proxying statement interfaces

 Revision 1.6  2002/12/06 15:57:08  billhorsman
 fix for proxied statement where Statement interface is not directly
 implemented.

 Revision 1.5  2002/12/03 12:24:00  billhorsman
 fixed fatal sql exception

 Revision 1.4  2002/11/09 15:56:52  billhorsman
 fix doc

 Revision 1.3  2002/11/02 14:22:15  billhorsman
 Documentation

 Revision 1.2  2002/10/30 21:25:08  billhorsman
 move createStatement into ProxyFactory

 Revision 1.1  2002/10/30 21:19:16  billhorsman
 make use of ProxyFactory

*/

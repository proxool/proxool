/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DatabaseMetaData;

/**
 * A central place to build proxy objects ({@link ProxyConnection connections}
 * and {@link ProxyStatement statements}).
 *
 * @version $Revision: 1.16 $, $Date: 2003/03/03 11:11:58 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
class ProxyFactory {

    private static final Log LOG = LogFactory.getLog(ProxyFactory.class);

    protected static ProxyConnection buildProxyConnection(long id, ConnectionPool connectionPool) throws SQLException {
        Connection realConnection = null;
        final String url = connectionPool.getDefinition().getUrl();
        realConnection = DriverManager.getConnection(
                url,
                connectionPool.getDefinition().getProperties());

        Object delegate = Proxy.newProxyInstance(
                realConnection.getClass().getClassLoader(),
                realConnection.getClass().getInterfaces(),
                new ProxyConnection(realConnection, id, url, connectionPool));

        return (ProxyConnection) Proxy.getInvocationHandler(delegate);
    }

    /**
     * Get a Connection from the ProxyConnection
     *
     * @param proxyConnection where to find the connection
     * @return
     */
    protected static Connection getConnection(ProxyConnectionIF proxyConnection) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (InvocationHandler) proxyConnection);
    }

    /**
     * Gets the real Statement that we got from the delegate driver
     * @return delegate statement
     */
    protected static Statement getDelegateStatement(Statement statement) {
        Statement ds = statement;
        ProxyStatement ps = (ProxyStatement) Proxy.getInvocationHandler(statement);
        ds = ps.getDelegateStatement();
        return ds;
    }

    protected static Statement createProxyStatement(Statement delegate, ConnectionPool connectionPool, ProxyConnectionIF proxyConnection,  String sqlStatement) {
        // We can't use Class#getInterfaces since that doesn't take
        // into account superclass interfaces. We could, laboriously,
        // work our way up the hierarchy but it doesn't seem worth while -
        // we only actually expect three options:
        Class[] interfaces = new Class[1];
        if (delegate instanceof CallableStatement) {
            interfaces[0] = CallableStatement.class;
        } else if (delegate instanceof PreparedStatement) {
            interfaces[0] = PreparedStatement.class;
        } else {
            interfaces[0] = Statement.class;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(delegate.getClass().getName() + " is being proxied using the " + interfaces[0]);
        }
        return (Statement) Proxy.newProxyInstance(delegate.getClass().getClassLoader(), interfaces, new ProxyStatement(delegate, connectionPool, proxyConnection, sqlStatement));
    }

    /**
     * Create a new DatabaseMetaData from a connection
     * @param connection the proxy connection we are using
     * @return databaseMetaData
     * @throws SQLException if the delegfate connection couldn't get the metaData
     */
    protected static DatabaseMetaData getDatabaseMetaData(Connection connection, ProxyConnectionIF proxyConnection) throws SQLException {
        return (DatabaseMetaData) Proxy.newProxyInstance(
          DatabaseMetaData.class.getClassLoader(),
                new Class[]{DatabaseMetaData.class},
                new ProxyDatabaseMetaData(connection, proxyConnection)
        );
    }


}

/*
 Revision history:
 $Log: ProxyFactory.java,v $
 Revision 1.16  2003/03/03 11:11:58  billhorsman
 fixed licence

 Revision 1.15  2003/02/19 15:14:32  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.14  2003/02/12 12:28:27  billhorsman
 added url, proxyHashcode and delegateHashcode to
 ConnectionInfoIF

 Revision 1.13  2003/02/06 17:41:04  billhorsman
 now uses imported logging

 Revision 1.12  2003/01/31 14:33:18  billhorsman
 fix for DatabaseMetaData

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

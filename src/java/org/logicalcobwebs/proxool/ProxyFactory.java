/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.cglib.proxy.Proxy;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Modifier;

/**
 * A central place to build proxy objects. It will also provide the original
 * object given a proxy.
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @version $Revision: 1.26 $, $Date: 2004/03/23 21:19:45 $
 * @since Proxool 0.5
 */
class ProxyFactory {

    private static final Log LOG = LogFactory.getLog(ProxyFactory.class);

    private static Map interfaceMap = new HashMap();

    /**
     * Wraps up a proxyConnection inside a {@link WrappedConnection} and then proxies it as a
     * simple {@link Connection}. You should call this immediately before the connection is served
     * to the user. The WrappedConnection is disposable (it is thrown away when the connection
     * is returned to the pool).
     * @param proxyConnection the pooled connection we are wrapping up
     * @return the Connection for use
     */
    protected static Connection getWrappedConnection(ProxyConnection proxyConnection) {
        final WrappedConnection wrappedConnection = new WrappedConnection(proxyConnection);
        Object delegate = Proxy.newProxyInstance(
                proxyConnection.getConnection().getClass().getClassLoader(),
                getInterfaces(proxyConnection.getConnection().getClass()),
                wrappedConnection);
        return (Connection) delegate;
    }

    /**
     * Gets the real Statement that we got from the delegate driver. This is no longer
     * necessary and only provided for backwards compatability.
     * @param statement proxy statement
     * @return delegate statement
     * @see ProxoolFacade#getDelegateStatement(java.sql.Statement)
     */
    protected static Statement getDelegateStatement(Statement statement) {
        Statement ds = statement;
        ProxyStatement ps = (ProxyStatement) Proxy.getInvocationHandler(statement);
        ds = ps.getDelegateStatement();
        return ds;
    }

    /**
     * Gets the real Connection that we got from the delegate driver. This is no longer
     * necessary and only provided for backwards compatability.
     * @param connection proxy connection
     * @return deletgate connection
     * @see ProxoolFacade#getDelegateConnection(java.sql.Connection)
     */
    protected static Connection getDelegateConnection(Connection connection) {
        WrappedConnection wc = (WrappedConnection) Proxy.getInvocationHandler(connection);
        return wc.getProxyConnection().getConnection();
    }

    protected static Statement getStatement(Statement delegate, ConnectionPool connectionPool, ProxyConnectionIF proxyConnection, String sqlStatement) {
        return (Statement) Proxy.newProxyInstance(
                delegate.getClass().getClassLoader(),
                getInterfaces(delegate.getClass()),
                new ProxyStatement(delegate, connectionPool, proxyConnection, sqlStatement));
    }

    /**
     * Get all the interfaces that a class implements. Drills down into super interfaces too
     * and super classes too.
     * The results are cached so it's very fast second time round.
     * @param clazz the class to examine.
     * @return an array of classes (all interfaces) that this class implements.
     */
    private static Class[] getInterfaces(Class clazz) {
        Class[] interfaceArray = (Class[]) interfaceMap.get(clazz);
        if (interfaceArray == null) {
            Set interfaces = new HashSet();
            traverseInterfacesRecursively(interfaces, clazz);
            interfaceArray = (Class[]) interfaces.toArray(new Class[interfaces.size()]);
/*
            if (LOG.isDebugEnabled()) {
                for (int i = 0; i < interfaceArray.length; i++) {
                    Class aClass = interfaceArray[i];
                    LOG.debug("Implementing " + aClass);
                }
            }
*/
            interfaceMap.put(clazz, interfaceArray);
/*
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Reusing " + interfaceArray.length + " interfaces already looked up for " + clazz);
            }
*/
        }
        return interfaceArray;
    }

    /**
     * Recursively looks at all interfaces for a class. Also looks at interfaces implemented
     * by the super class (and its super class, etc.) Quite a lot of processing involved
     * so you shouldn't call it too often.
     * @param interfaces this set is populated with all interfaceMap it finds
     * @param clazz the base class to analyze
     */
    private static void traverseInterfacesRecursively(Set interfaces, Class clazz) {
        // Check for circular reference (avoid endless recursion)
        if (interfaces.contains(clazz)) {
            // Skip it, we've already been here.
/*
            if (LOG.isDebugEnabled()) {
                LOG.debug("Skipping " + clazz + " because we've already traversed it");
            }
*/
        } else {
/*
            if (LOG.isDebugEnabled()) {
                LOG.debug("Analyzing " + clazz);
            }
*/
            Class[] interfaceArray = clazz.getInterfaces();
            for (int i = 0; i < interfaceArray.length; i++) {
/*
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Adding " + interfaceArray[i]);
                }
*/
                traverseInterfacesRecursively(interfaces, interfaceArray[i]);
                // We're only interested in public interfaces. In fact, including
                // non-public interfaces will give IllegalAccessExceptions.
                if (Modifier.isPublic(interfaceArray[i].getModifiers())) {
                    interfaces.add(interfaceArray[i]);
                }
            }
            Class superClazz = clazz.getSuperclass();
            if (superClazz != null) {
                traverseInterfacesRecursively(interfaces, superClazz);
            }
/*
            if (LOG.isDebugEnabled()) {
                LOG.debug("Found " + interfaceArray.length + " interfaceMap for " + clazz);
            }
*/
        }
    }

    /**
     * Create a new DatabaseMetaData from a connection
     * @param connection the proxy connection we are using
     * @return databaseMetaData
     * @throws SQLException if the delegfate connection couldn't get the metaData
     */
    protected static DatabaseMetaData getDatabaseMetaData(Connection connection, Connection wrappedConnection) throws SQLException {
        return (DatabaseMetaData) Proxy.newProxyInstance(
                DatabaseMetaData.class.getClassLoader(),
                new Class[]{DatabaseMetaData.class},
                new ProxyDatabaseMetaData(connection, wrappedConnection)
        );
    }

    /**
     * Get the WrappedConnection behind this proxy connection.
     * @param connection the connection that was served
     * @return the wrapped connection or null if it couldn't be found
     */
    public static WrappedConnection getWrappedConnection(Connection connection) {
        return (WrappedConnection) Proxy.getInvocationHandler(connection);
    }

}

/*
 Revision history:
 $Log: ProxyFactory.java,v $
 Revision 1.26  2004/03/23 21:19:45  billhorsman
 Added disposable wrapper to proxied connection. And made proxied objects implement delegate interfaces too.

 Revision 1.25  2003/12/12 19:29:47  billhorsman
 Now uses Cglib 2.0

 Revision 1.24  2003/09/30 18:39:08  billhorsman
 New test-before-use, test-after-use and fatal-sql-exception-wrapper-class properties.

 Revision 1.23  2003/09/10 22:21:04  chr32
 Removing > jdk 1.2 dependencies.

 Revision 1.22  2003/09/07 22:11:31  billhorsman
 Remove very persistent debug message

 Revision 1.21  2003/08/27 18:03:20  billhorsman
 added new getDelegateConnection() method

 Revision 1.20  2003/03/11 14:51:54  billhorsman
 more concurrency fixes relating to snapshots

 Revision 1.19  2003/03/10 23:43:13  billhorsman
 reapplied checkstyle that i'd inadvertently let
 IntelliJ change...

 Revision 1.18  2003/03/10 15:26:49  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.17  2003/03/05 18:42:33  billhorsman
 big refactor of prototyping and house keeping to
 drastically reduce the number of threads when using
 many pools

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

/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.cglib.proxy.InvocationHandler;
import org.logicalcobwebs.cglib.proxy.MethodInterceptor;
import org.logicalcobwebs.cglib.proxy.MethodProxy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.proxy.InvokerFacade;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Connection;

/**
 * Wraps up a {@link ProxyConnection}. It is proxied as a {@link java.sql.Connection}
 * @version $Revision: 1.6 $, $Date: 2006/01/18 14:40:02 $
 * @author <a href="mailto:bill@logicalcobwebs.co.uk">Bill Horsman</a>
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.9
 */
public class WrappedConnection implements MethodInterceptor {

    private static final Log LOG = LogFactory.getLog(WrappedConnection.class);

    private static final String CLOSE_METHOD = "close";

    private static final String IS_CLOSED_METHOD = "isClosed";

    private static final String EQUALS_METHOD = "equals";

    private static final String GET_META_DATA_METHOD = "getMetaData";

    private static final String FINALIZE_METHOD = "finalize";

    private static final String HASH_CODE_METHOD = "hashCode";

    private static final String TO_STRING_METHOD = "toString";

    /**
     * The wrapped object. We should protect this and not expose it. We have to make sure that
     * if we pass the proxyConnection to another WrappedConnection then this one can no longer
     * manipulate it.
     */
    private ProxyConnection proxyConnection;

    private long id;

    private String alias;

    /**
     * This gets set if the close() method is explicitly called. The {@link #getProxyConnection() proxyConnection}
     * could still be {@link org.logicalcobwebs.proxool.ProxyConnectionIF#isReallyClosed() really closed} without
     * this wrapper knowing about it yet.
     */
    private boolean manuallyClosed;

    /**
     * Construct this wrapper around the proxy connection
     * @param proxyConnection to wrap
     */
    public WrappedConnection(ProxyConnection proxyConnection) {
        this.proxyConnection = proxyConnection;
        this.id = proxyConnection.getId();
        this.alias= proxyConnection.getDefinition().getAlias();
    }

    /**
     * Get the encapsulated proxy connection
     * @return the proxy connection
     */
    public ProxyConnection getProxyConnection() {
        return proxyConnection;
    }

    /**
     * Delegates to {@link #invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[]) invoke}
     * @see MethodInterceptor#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], org.logicalcobwebs.cglib.proxy.MethodProxy)
     */
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        return invoke(proxy, method, args);
    }

    /**
     * Delegates all operations to the encapsulated {@link ProxyConnection} except for:
     * <ul>
     * <li>close()</li>
     * <li>equals()</li>
     * <li>hashCode()</li>
     * <li>isClosed()</li>
     * <li>getMetaData()</li>
     * <li>finalize()</li>
     * </ul>
     * It also spots mutators and remembers that the property has been changed so that it can
     * be {@link ConnectionResetter reset}. And any statements that are returned are remembered
     * so that we can track whether all statements have been closed properly when the connection
     * is returned to the pool.
     * @see InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        int argCount = args != null ? args.length : 0;
        Method concreteMethod = method;
        if (proxyConnection != null && proxyConnection.getConnection() != null) {
            concreteMethod = InvokerFacade.getConcreteMethod(proxyConnection.getConnection().getClass(), method);
        }
        try {
            if (proxyConnection != null && proxyConnection.isReallyClosed()) {
                // The user is trying to do something to this connection and it's been closed.
                if (concreteMethod.getName().equals(IS_CLOSED_METHOD)) {
                    // That's cool. No problem checking as many times as you like.
                } else if (concreteMethod.getName().equals(CLOSE_METHOD)) {
                    // That's cool. You can call close as often as you like.
                } else if (manuallyClosed) {
                    // We've already manually closed this connection yet we trying to do something
                    // to it that isn't another close(). That is bad client coding :)
                    throw new SQLException("You can't perform any operations on a connection after you've called close()");
                } else {
                    // The connection has been closed automatically. The client probably wasn't expecting
                    // that. Still, throw an exception so that they know it's all gone pear shaped.
                    throw new SQLException("You can't perform any operations on this connection. It has been automatically closed by Proxool for some reason (see logs).");
                }
            }
            if (concreteMethod.getName().equals(CLOSE_METHOD)) {
                // It's okay to close a connection twice. Only we ignore the
                // second time.
                if (proxyConnection != null && !proxyConnection.isReallyClosed()) {
                    proxyConnection.close();
                    // Set it to null so that we can't do anything else to it.
                    proxyConnection = null;
                    manuallyClosed = true;
                }
            } else if (concreteMethod.getName().equals(EQUALS_METHOD) && argCount == 1) {
                result = equals(args[0]) ? Boolean.TRUE : Boolean.FALSE;
            } else if (concreteMethod.getName().equals(HASH_CODE_METHOD) && argCount == 0) {
                result = new Integer(hashCode());
            } else if (concreteMethod.getName().equals(IS_CLOSED_METHOD) && argCount == 0) {
                result = (proxyConnection == null || proxyConnection.isClosed()) ? Boolean.TRUE : Boolean.FALSE;
            } else if (concreteMethod.getName().equals(GET_META_DATA_METHOD) && argCount == 0) {
                if (proxyConnection != null) {
                    Connection connection = ProxyFactory.getWrappedConnection(proxyConnection);
                    result = ProxyFactory.getDatabaseMetaData(proxyConnection.getConnection().getMetaData(), connection);
                } else {
                    throw new SQLException("You can't perform a " + concreteMethod.getName() + " operation after the connection has been closed");
                }
            } else if (concreteMethod.getName().equals(FINALIZE_METHOD)) {
                super.finalize();
            } else if (concreteMethod.getName().equals(TO_STRING_METHOD)) {
                result = toString();
            } else {
                if (proxyConnection != null) {
                    if (concreteMethod.getName().startsWith(ConnectionResetter.MUTATOR_PREFIX)) {
                        proxyConnection.setNeedToReset(true);
                    }
                    try {
                        result = concreteMethod.invoke(proxyConnection.getConnection(), args);
                    } catch (IllegalAccessException e) {
                        // This is probably because we are trying to access a non-public concrete class. But don't worry,
                        // we can always use the proxy supplied method. This will only fail if we try to use an injectable
                        // method on a method in a class that isn't public and for a method that isn't declared in an interface -
                        // but if that is the case then that method is inaccessible by any means (even by bypassing Proxool and
                        // using the vendor's driver directly).
                        LOG.debug("Ignoring IllegalAccessException whilst invoking the " + concreteMethod + " concrete method and trying the " + method + " method directly.");
                        // By overriding the method cached in the InvokerFacade we ensure that we only log this message once, and
                        // we speed up subsequent usages by not calling the method that fails first.
                        InvokerFacade.overrideConcreteMethod(proxyConnection.getConnection().getClass(), method, method);
                        result = method.invoke(proxyConnection.getConnection(), args);
                    }
                } else {
                    throw new SQLException("You can't perform a " + concreteMethod.getName() + " operation after the connection has been closed");
                }
            }

            // If we have just made some sort of Statement then we should rather return
            // a proxy instead.
            if (result instanceof Statement) {
                // Work out whether we were passed the sql statement during the
                // call to get the statement object. Sometimes you do, sometimes
                // you don't:
                // connection.prepareCall(sql);
                // connection.createProxyStatement();
                String sqlStatement = null;
                if (argCount > 0 && args[0] instanceof String) {
                    sqlStatement = (String) args[0];
                }

                // We keep a track of all open statements
                proxyConnection.addOpenStatement((Statement) result);

                result = ProxyFactory.getStatement((Statement) result, proxyConnection.getConnectionPool(), proxyConnection, sqlStatement);

            }

        } catch (InvocationTargetException e) {
            // We might get a fatal exception here. Let's test for it.
            if (FatalSqlExceptionHelper.testException(proxyConnection.getDefinition(), e.getTargetException())) {
                FatalSqlExceptionHelper.throwFatalSQLException(proxyConnection.getDefinition().getFatalSqlExceptionWrapper(), e.getTargetException());
            }
            throw e.getTargetException();
        } catch (SQLException e) {
            throw new SQLException("Couldn't perform the operation " + concreteMethod.getName() + ": " + e.getMessage());
        } catch (Exception e) {
            LOG.error("Unexpected invocation exception", e);
            if (FatalSqlExceptionHelper.testException(proxyConnection.getDefinition(), e)) {
                FatalSqlExceptionHelper.throwFatalSQLException(proxyConnection.getDefinition().getFatalSqlExceptionWrapper(), e);
            }
            throw new RuntimeException("Unexpected invocation exception: "
                    + e.getMessage());
        }
        return result;
    }

    /**
     * The ID for the encapsulated {@link ProxyConnection}. This will still
     * return the correct value after the connection is closed.
     * @return the ID
     */
    public long getId() {
        return id;
    }

    /**
     * Get the alias of the connection pool this connection belongs to
     * @return {@link ConnectionPoolDefinitionIF#getAlias() alias}
     */
    public String getAlias() {
        return alias;
    }

    /**
     * If the object passed to this method is actually a proxied version of this
     * class then compare the real class with this one.
     * @param obj the object to compare
     * @return true if the object is a proxy of "this"
     */
    public boolean equals(Object obj) {
        if (obj instanceof Connection) {
            final WrappedConnection wc = ProxyFactory.getWrappedConnection((Connection) obj);
            if (wc != null && wc.getId() > 0 && getId() > 0) {
                return wc.getId() == getId();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        if (proxyConnection != null) {
            return hashCode() + "(" + proxyConnection.getConnection().toString() + ")";
        } else {
            return hashCode() + "(out of scope)";
        }
    }
}
/*
 Revision history:
 $Log: WrappedConnection.java,v $
 Revision 1.6  2006/01/18 14:40:02  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.5  2005/10/02 12:32:58  billhorsman
 Improve the trapping of operations after a wrapped connection is closed.

 Revision 1.4  2005/05/04 16:31:41  billhorsman
 Use the definition referenced by the proxy connection rather than the pool instead.

 Revision 1.3  2004/07/13 21:06:21  billhorsman
 Fix problem using injectable interfaces on methods that are declared in non-public classes.

 Revision 1.2  2004/06/02 20:50:47  billhorsman
 Dropped obsolete InvocationHandler reference and injectable interface stuff.

 Revision 1.1  2004/03/23 21:19:45  billhorsman
 Added disposable wrapper to proxied connection. And made proxied objects implement delegate interfaces too.

*/
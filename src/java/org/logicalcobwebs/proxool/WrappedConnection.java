/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.cglib.proxy.InvocationHandler;
import org.logicalcobwebs.cglib.proxy.MethodInterceptor;
import org.logicalcobwebs.cglib.proxy.MethodProxy;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Connection;

/**
 * Wraps up a {@link ProxyConnection}. It is proxied as a {@link java.sql.Connection}
 * @version $Revision: 1.1 $, $Date: 2004/03/23 21:19:45 $
 * @author <a href="mailto:bill@logicalcobwebs.co.uk">Bill Horsman</a>
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.9
 */
public class WrappedConnection implements InvocationHandler, MethodInterceptor {

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
     * Construct this wrapper around the proxy connection
     * @param proxyConnection to wrap
     */
    public WrappedConnection(ProxyConnection proxyConnection) {
        this.proxyConnection = proxyConnection;
        this.id = proxyConnection.getId();
        this.alias= proxyConnection.getConnectionPool().getDefinition().getAlias();
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
        try {
            if (method.getName().equals(CLOSE_METHOD)) {
                // It's okay to close a connection twice. Only we ignore the
                // second time.
                if (proxyConnection != null) {
                    proxyConnection.close();
                    // Set it to null so that we can't do anything else to it.
                    proxyConnection = null;
                }
            } else if (method.getName().equals(EQUALS_METHOD) && argCount == 1) {
                result = equals(args[0]) ? Boolean.TRUE : Boolean.FALSE;
            } else if (method.getName().equals(HASH_CODE_METHOD) && argCount == 0) {
                result = new Integer(hashCode());
            } else if (method.getName().equals(IS_CLOSED_METHOD) && argCount == 0) {
                result = (proxyConnection == null || proxyConnection.isClosed()) ? Boolean.TRUE : Boolean.FALSE;
            } else if (method.getName().equals(GET_META_DATA_METHOD) && argCount == 0) {
                if (proxyConnection != null) {
                    result = ProxyFactory.getDatabaseMetaData(proxyConnection.getConnection(),(Connection) proxy);
                } else {
                    throw new SQLException("You can't perform a " + method.getName() + " operation after the connection has been closed");
                }
            } else if (method.getName().equals(FINALIZE_METHOD)) {
                super.finalize();
            } else if (method.getName().equals(TO_STRING_METHOD)) {
                result = toString();
            } else {
                if (proxyConnection != null) {
                    if (method.getName().startsWith(ConnectionResetter.MUTATOR_PREFIX)) {
                        proxyConnection.setNeedToReset(true);
                    }
                    result = method.invoke(proxyConnection.getConnection(), args);
                } else {
                    throw new SQLException("You can't perform a " + method.getName() + " operation after the connection has been closed");
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
            if (FatalSqlExceptionHelper.testException(proxyConnection.getConnectionPool().getDefinition(), e.getTargetException())) {
                FatalSqlExceptionHelper.throwFatalSQLException(proxyConnection.getConnectionPool().getDefinition().getFatalSqlExceptionWrapper(), e.getTargetException());
            }
            throw e.getTargetException();
        } catch (SQLException e) {
            throw new SQLException("Couldn't perform the operation " + method.getName());
        } catch (Exception e) {
            LOG.error("Unexpected invocation exception", e);
            if (FatalSqlExceptionHelper.testException(proxyConnection.getConnectionPool().getDefinition(), e)) {
                FatalSqlExceptionHelper.throwFatalSQLException(proxyConnection.getConnectionPool().getDefinition().getFatalSqlExceptionWrapper(), e);
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
            if (wc != null) {
                return wc.hashCode() == hashCode();
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
 Revision 1.1  2004/03/23 21:19:45  billhorsman
 Added disposable wrapper to proxied connection. And made proxied objects implement delegate interfaces too.

*/
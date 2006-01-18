/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * Will wrap up exceptions in another exception which can be defined at runtime.
 * @version $Revision: 1.5 $, $Date: 2006/01/18 14:40:01 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
class FatalSqlExceptionHelper {

    private static final Log LOG = LogFactory.getLog(FatalSqlExceptionHelper.class);

    /**
     * Throws a wrapped SQLException if a wrapper is defined
     * @param className the classname of the wrapping exception (must be either a RuntimeException or
     * an SQLException). If null, then the original exception is rethrown.
     * @param originalException the orginal exception
     * @throws ProxoolException if there is an unexpected error with wrapping the exception
     * @throws SQLException either the original exception, or a wrapped version of it
     * @throws RuntimeException a wrapped up version of the orginal
     */
    protected static void throwFatalSQLException(String className, Throwable originalException) throws ProxoolException, SQLException, RuntimeException {
        if (className != null && className.trim().length() > 0) {
            Class clazz = null;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new ProxoolException("Couldn't find class " + className);
            }
            if (SQLException.class.isAssignableFrom(clazz)) {
                // That's OK
            } else if (RuntimeException.class.isAssignableFrom(clazz)) {
                // That's OK
            } else {
                throw new ProxoolException("Couldn't wrap up using " + clazz.getName() + " because it isn't either a RuntimeException or an SQLException");
            }
            Constructor toUse = null;
            Object[] args = null;
            String argDescription = "";
            Constructor[] constructors = clazz.getConstructors();
            for (int i = 0; i < constructors.length; i++) {
                Constructor constructor = constructors[i];
                Class[] parameterTypes = constructor.getParameterTypes();
                if (toUse == null && parameterTypes.length == 0) {
                    toUse = constructor;
                }
                if (parameterTypes.length == 1 && Exception.class.isAssignableFrom(parameterTypes[0])) {
                    toUse = constructor;
                    args = new Object[]{originalException};
                    argDescription = "Exception";
                    break;
                }
            }
            try {
                Object exceptionToThrow = toUse.newInstance(args);
                if (exceptionToThrow instanceof RuntimeException) {
                    LOG.debug("Wrapping up a fatal exception: " + originalException.getMessage(), originalException);
                    throw (RuntimeException) exceptionToThrow;
                } else if (exceptionToThrow instanceof SQLException) {
                    throw (SQLException) exceptionToThrow;
                } else {
                    throw new ProxoolException("Couldn't throw " + clazz.getName() + " because it isn't either a RuntimeException or an SQLException");
                }
            } catch (InstantiationException e) {
                throw new ProxoolException("Couldn't create " + clazz.getName() + "(" + argDescription + ")", e);
            } catch (IllegalAccessException e) {
                throw new ProxoolException("Couldn't create " + clazz.getName() + "(" + argDescription + ")", e);
            } catch (InvocationTargetException e) {
                throw new ProxoolException("Couldn't create " + clazz.getName() + "(" + argDescription + ")", e);
            }
        } else {
            if (originalException instanceof SQLException) {
                throw (SQLException) originalException;
            } else if (originalException instanceof RuntimeException) {
                throw (RuntimeException) originalException;
            } else {
                throw new RuntimeException("Unexpected exception:" + originalException.getMessage());
            }
        }
    }

    /**
     * Test to see if an exception is a fatal one
     * @param cpd the definition so we can find out what a fatal exception looks like
     * @param t the exception to test
     * @return true if it is fatal
     */
    protected static boolean testException(ConnectionPoolDefinitionIF cpd, Throwable t) {
        return testException(cpd, t, 0);
    }

    /**
     * Test to see if an exception is a fatal one
     * @param cpd the definition so we can find out what a fatal exception looks like
     * @param t the exception to test
     * @param level the recursion level (max 20)
     * @return true if it is fatal
     */
    protected static boolean testException(ConnectionPoolDefinitionIF cpd, Throwable t, int level) {
        boolean fatalSqlExceptionDetected = false;
        Iterator i = cpd.getFatalSqlExceptions().iterator();
        while (i.hasNext()) {
            if (t.getMessage() != null && t.getMessage().indexOf((String) i.next()) > -1) {
                // This SQL exception indicates a fatal problem with this connection.
                fatalSqlExceptionDetected = true;
            }
        }

        // If it isn't fatal, then try testing the contained exception
        if (!fatalSqlExceptionDetected && level < 20) {
            Throwable cause = getCause(t);
            if (cause != null) {
                fatalSqlExceptionDetected = testException(cpd, cause, level + 1);
            }
        }

        return fatalSqlExceptionDetected;
    }

    /**
     * Tries to drill down into an exception to find its cause. Only goes one level deep.
     * Uses reflection to look at getCause(), getTargetException(), getRootCause() and
     * finally getOriginalException() methods to see if it can find one. Doesn't throw
     * an error - it will just log a warning and return a null if nothing was found.
     * @param t the exception to look inside
     * @return the original exception or null if none was found.
     */
    protected static Throwable getCause(Throwable t) {
        Throwable cause = null;
        Method causeMethod = null;

        try {
            // Try a series of likely accessor methods
            if (causeMethod == null) {
                causeMethod = getMethod(t, "getCause");
            }
            if (causeMethod == null) {
                causeMethod = getMethod(t, "getTargetException");
            }
            if (causeMethod == null) {
                causeMethod = getMethod(t, "getRootCause");
            }
            if (causeMethod == null) {
                causeMethod = getMethod(t, "getOriginalException");
            }

            // If one was found, invoke it.
            if (causeMethod != null) {
                try {
                    cause = (Throwable) causeMethod.invoke(t, null);
                } catch (IllegalAccessException e) {
                    LOG.warn("Problem invoking " + t.getClass().getName() + "." + causeMethod.getName() + ". Ignoring.", e);
                } catch (IllegalArgumentException e) {
                    LOG.warn("Problem invoking " + t.getClass().getName() + "." + causeMethod.getName() + ". Ignoring.", e);
                } catch (InvocationTargetException e) {
                    LOG.warn("Problem invoking " + t.getClass().getName() + "." + causeMethod.getName() + ". Ignoring.", e);
                }
            }
        } catch (Exception e) {
            LOG.warn("Unexpected exception drilling into exception. Ignoring.", e);
        }
        return cause;
    }

    private static Method getMethod(Object o, String methodName) {
        Method m = null;
        try {
            m = o.getClass().getMethod(methodName, null);
            // Reject any method that doesn't return a throwable.
            if (!Throwable.class.isAssignableFrom(m.getReturnType())) {
                m = null;
            }
        } catch (NoSuchMethodException e) {
            // That's OK
        } catch (SecurityException e) {
            LOG.warn("Problem finding method " + methodName, e);
        }
        return m;
    }

}

/*
 Revision history:
 $Log: FatalSqlExceptionHelper.java,v $
 Revision 1.5  2006/01/18 14:40:01  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.4  2005/07/01 08:02:50  billhorsman
 Check for exception message being null

 Revision 1.3  2003/09/30 18:39:08  billhorsman
 New test-before-use, test-after-use and fatal-sql-exception-wrapper-class properties.

 Revision 1.2  2003/09/29 18:12:33  billhorsman
 Doc

*/
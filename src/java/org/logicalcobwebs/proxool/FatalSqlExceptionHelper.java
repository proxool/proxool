package org.logicalcobwebs.proxool;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/

class FatalSqlExceptionHelper {

    /**
     * Throws a wrapped SQLException if a wrapper is defined
     * @param className the classname of the wrapping exception (must be either a RuntimeException or
     * an SQLException). If null, then the original exception is rethrown.
     * @param originalException the orginal exception
     * @throws ProxoolException if there is an unexpected error with wrapping the exception
     * @throws SQLException either the original exception, or a wrapped version of it
     * @throws RuntimeException a wrapped up version of the orginal
     */
    protected static void throwFatalSQLException(String className, SQLException originalException) throws ProxoolException, SQLException, RuntimeException {
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
            throw originalException;
        }
    }
}

/*
 * $Header: /cvsroot/proxool/proxool/src/java/org/logicalcobwebs/logging/impl/Attic/Jdk14Logger.java,v 1.5 2003/09/10 23:05:40 billhorsman Exp $
 * $Revision: 1.5 $
 * $Date: 2003/09/10 23:05:40 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.logicalcobwebs.logging.impl;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.logicalcobwebs.logging.Log;

/**
 * <p>Implementation of the <code>org.logicalcobwebs.logging.Log</code>
 * interfaces that wraps the standard JDK logging mechanisms that were
 * introduced in the Merlin release (JDK 1.4).</p>
 *
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @author <a href="mailto:donaldp@apache.org">Peter Donald</a>
 * @version $Revision: 1.5 $ $Date: 2003/09/10 23:05:40 $
 */

public final class Jdk14Logger implements Log {

    // ----------------------------------------------------------- Constructors


    /**
     * Construct a named instance of this Logger.
     *
     * @param name Name of the logger to be constructed
     */
    public Jdk14Logger (String name) {
        try {
            final Class loggerClass = Class.forName("java.util.logging.Logger");
            final Method getLoggerMethod = loggerClass.getMethod("getLogger", new Class [] {String.class});
            logger = getLoggerMethod.invoke(null, new Object[]{name});
            logpMethod = logger.getClass().getMethod("logp", new Class[] {Class.forName("java.util.logging.Logger"),
                String.class, String.class, String.class});
            logpExMethod = logger.getClass().getMethod("logp", new Class[] {Class.forName("java.util.logging.Logger"),
                String.class, String.class, String.class, Throwable.class});
            final Class levelClass = Class.forName("java.util.logging.Level");
            isLoggableMethod = loggerClass.getMethod("isLoggable", new Class[] {levelClass});
            getStackTraceMethod = Throwable.class.getMethod("getStackTrace", null);
            final Class stackTraceClass = Class.forName("java.lang.StackTraceElement");
            getClassNameMethod = stackTraceClass.getMethod("getClassName", null);
            getMethodNameMethod = stackTraceClass.getMethod("getMethodName", null);
            levelFINEST = levelClass.getField("FINEST").get(null);
            levelFINE = levelClass.getField("FINE").get(null);
            levelINFO = levelClass.getField("INFO").get(null);
            levelWARNING = levelClass.getField("WARNING").get(null);
            levelSEVERE = levelClass.getField("SEVERE").get(null);
        } catch (Exception e) {
            System.err.println("Could not create Jdk14Logger.");
            e.printStackTrace();
        }
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The underlying Logger implementation we are using.
     */
    private Object logger = null;

    private Method logpMethod = null;
    private Method logpExMethod = null;
    private Method isLoggableMethod = null;
    private Method getStackTraceMethod = null;
    private Method getClassNameMethod = null;
    private Method getMethodNameMethod = null;

    private Object levelFINEST = null;
    private Object levelFINE = null;
    private Object levelINFO = null;
    private Object levelWARNING = null;
    private Object levelSEVERE = null;







    // --------------------------------------------------------- Public Methods

    private void log (Object level, String msg, Throwable ex) {
        // Hack (?) to get the stack trace.
        Throwable dummyException = new Throwable ();
        String cname = "unknown";
        String method = "unknown";
        // Use reflection instead of JDK1.4 code.
        try {
            Object locations[] = (Object[]) getStackTraceMethod.invoke(dummyException, null);
            // Caller will be the third element
            if (locations != null && locations.length > 2) {
                cname = (String) getClassNameMethod.invoke(locations[2], null);
                method = (String) getMethodNameMethod.invoke(locations[2], null);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        try {
            if (ex == null) {
                logpMethod.invoke(logger, new Object[]{level, cname, method, msg});
            } else {
                logpExMethod.invoke(logger, new Object[]{level, cname, method, msg, ex});
            }
        } catch (Exception e) {
            throw new RuntimeException(
                "Logging of message '" + msg + "' failed" + (ex != null ? ":" + ex.getMessage() : "."));
        }
    }

    /**
     * Log a message with debug log level.
     */
    public void debug (Object message) {
        log (levelFINE, String.valueOf (message), null);
    }

    /**
     * Log a message and exception with debug log level.
     */
    public void debug (Object message, Throwable exception) {
        log (levelFINE, String.valueOf (message), exception);
    }

    /**
     * Log a message with error log level.
     */
    public void error (Object message) {
        log (levelSEVERE, String.valueOf (message), null);
    }

    /**
     * Log a message and exception with error log level.
     */
    public void error (Object message, Throwable exception) {
        log (levelSEVERE, String.valueOf (message), exception);
    }

    /**
     * Log a message with fatal log level.
     */
    public void fatal (Object message) {
        log (levelSEVERE, String.valueOf (message), null);
    }

    /**
     * Log a message and exception with fatal log level.
     */
    public void fatal (Object message, Throwable exception) {
        log (levelSEVERE, String.valueOf (message), exception);
    }

    /**
     * Log a message with info log level.
     */
    public void info (Object message) {
        log (levelINFO, String.valueOf (message), null);
    }

    /**
     * Log a message and exception with info log level.
     */
    public void info (Object message, Throwable exception) {
        log (levelINFO, String.valueOf (message), exception);
    }

    /**
     * Is debug logging currently enabled?
     */
    public boolean isDebugEnabled () {
        return (isLoggable (levelFINE));
    }

    /**
     * Is error logging currently enabled?
     */
    public boolean isErrorEnabled () {
        return (isLoggable (levelSEVERE));
    }

    /**
     * Is fatal logging currently enabled?
     */
    public boolean isFatalEnabled () {
        return (isLoggable (levelSEVERE));
    }

    /**
     * Is info logging currently enabled?
     */
    public boolean isInfoEnabled () {
        return (isLoggable (levelINFO));
    }

    /**
     * Is tace logging currently enabled?
     */
    public boolean isTraceEnabled () {
        return (isLoggable (levelFINEST));
    }

    /**
     * Is warning logging currently enabled?
     */
    public boolean isWarnEnabled () {
        return (isLoggable (levelWARNING));
    }

    /**
     * Log a message with trace log level.
     */
    public void trace (Object message) {
        log (levelFINEST, String.valueOf (message), null);
    }

    /**
     * Log a message and exception with trace log level.
     */
    public void trace (Object message, Throwable exception) {
        log (levelFINEST, String.valueOf (message), exception);
    }

    /**
     * Log a message with warn log level.
     */
    public void warn (Object message) {
        log (levelWARNING, String.valueOf (message), null);
    }

    /**
     * Log a message and exception with warn log level.
     */
    public void warn (Object message, Throwable exception) {
        log (levelWARNING, String.valueOf (message), exception);
    }

    private boolean isLoggable(Object level) {
        try {
            return ((Boolean) isLoggableMethod.invoke(logger, new Object[] {level})).booleanValue();
        } catch (Exception e) {
            throw new RuntimeException("isLoggable call failed: " + e.getMessage());
        }
    }
}


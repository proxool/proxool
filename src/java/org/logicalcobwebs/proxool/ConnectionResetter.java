/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Responsible for resetting a Connection to its default state when it is
 * returned to the pool. It must be initialised by the first Connection that
 * is made (for each pool) so that we don't make any assumptions about
 * what the default values are.
 *
 * @version $Revision: 1.5 $, $Date: 2002/11/12 20:24:12 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public class ConnectionResetter {

    private Log log;

    /**
     * @see #initialise
     */
    private boolean initialised;

    /**
     * @see #addReset
     * @see #reset
     */
    private Map accessorMutatorMap = new HashMap();

    /**
     * @see #addReset
     * @see #reset
     */
    private Map defaultValues = new HashMap();

    /**
     * We use this to guess if we are changing a property that will need resetting
     */
    protected static final String MUTATOR_PREFIX = "set";

    private String driverName;

    /**
     * Pass in the log to use
     * @param log debug information sent here
     */
    protected ConnectionResetter(Log log, String driverName) {
        this.log = log;
        this.driverName = driverName;

        // Map all the reset methods
        addReset("getCatalog", "setCatalog");
        addReset("isReadOnly", "setReadOnly");
        addReset("getTransactionIsolation", "setTransactionIsolation");
        addReset("getTypeMap", "setTypeMap");
        addReset("getHoldability", "setHoldability");
    }

    /**
     * Add a pair of methods that need resetting each time a connection is
     * put back in the pool
     * @param accessorName the name of the "getter" method (e.g. getAutoCommit)
     * @param mutatorName teh name of the "setter" method (e.g. setAutoCommit)
     */
    private void addReset(String accessorName, String mutatorName) {

        try {

            Method accessor = null;
            Method mutator = null;

            Method[] methods = Connection.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getName().equals(accessorName)) {
                    if (accessor == null) {
                        accessor = method;
                    } else {
                        log.info("Skipping ambiguous reset method " + accessorName);
                        return;
                    }
                }
                if (method.getName().equals(mutatorName)) {
                    if (mutator == null) {
                        mutator = method;
                    } else {
                        log.info("Skipping ambiguous reset method " + mutatorName);
                        return;
                    }
                }
            }

            if (accessor == null) {
                log.debug("Ignoring attempt to map reset method " + accessorName + " (probably because it isn't implemented in this JDK)");
            } else if (mutator == null) {
                log.debug("Ignoring attempt to map reset method " + mutatorName + " (probably because it isn't implemented in this JDK)");
            } else if (accessorMutatorMap.containsKey(accessor)) {
                log.warn("Ignoring attempt to map duplicate reset method " + accessorName);
            } else if (accessorMutatorMap.containsValue(mutator)) {
                log.warn("Ignoring attempt to map duplicate reset method " + mutatorName);
            } else {

                if (mutatorName.indexOf(MUTATOR_PREFIX) != 0) {
                    log.warn("Resetter mutator " + mutatorName + " does not start with " + MUTATOR_PREFIX
                            + " as expected. Proxool maynot recognise that a reset is necessary.");
                }

                if (accessor.getParameterTypes().length > 0) {
                    log.info("Ignoring attempt to map accessor method " + accessorName + ". It must have no arguments.");
                } else if (mutator.getParameterTypes().length != 1) {
                    log.info("Ignoring attempt to map mutator method " + mutatorName
                            + ". It must have exactly one argument, not " + mutator.getParameterTypes().length);
                } else {
                    accessorMutatorMap.put(accessor, mutator);
                }
            }
        } catch (Exception e) {
            log.error("Problem mapping " + accessorName + " and " + mutatorName, e);
        }

    }

    /**
     * This gets called every time we make a Connection. Not that often
     * really, so it's ok to synchronize a bit.
     * @param connection this will be used to get all the default values
     */
    protected void initialise(Connection connection) {
        if (!initialised) {
            synchronized (this) {
                if (!initialised) {

                    Set accessorsToRemove = new HashSet();
                    Iterator i = accessorMutatorMap.keySet().iterator();
                    while (i.hasNext()) {
                        Method accessor = (Method) i.next();
                        Method mutator = (Method) accessorMutatorMap.get(accessor);
                        Object value = null;
                        try {
                            value = accessor.invoke(connection, null);
                            // It's perfectly ok for the default value to be null, we just
                            // don't want to add it to the map.
                            if (value != null) {
                                defaultValues.put(mutator, value);
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("Remembering default value: " + accessor.getName() + "() = " + value);
                            }

                        } catch (Throwable t) {
                            log.debug(driverName + " does not support " + accessor.getName() + ". Proxool doesn't mind.");
                            // We will remove this later (to avoid ConcurrentModifcation)
                            accessorsToRemove.add(accessor);
                        }

                        // Just test that the mutator works too. Otherwise it's going to fall over
                        // everytime we close a connection
                        try {
                            Object[] args = {value};
                            mutator.invoke(connection, args);
                        } catch (Throwable t) {
                            log.debug(driverName + " does not support " + mutator.getName() + ". Proxool doesn't mind.");
                            // We will remove this later (to avoid ConcurrentModifcation)
                            accessorsToRemove.add(accessor);
                        }

                    }

                    // Remove all the reset methods that we had trouble configuring
                    Iterator j = accessorsToRemove.iterator();
                    while (j.hasNext()) {
                        Method accessor = (Method) j.next();
                        Method mutator = (Method) accessorMutatorMap.get(accessor);
                        accessorMutatorMap.remove(accessor);
                        defaultValues.remove(mutator);
                    }

                    initialised = true;
                }
            }
        }
    }

    /**
     * Reset this connection to its default values. If anything goes wrong, it is logged
     * as a warning or info but it silently continues.
     * @param connection to be reset
     * @param id used in log messages
     * @return true if the reset was error free, or false if it encountered errors. (in which case it should probably not be reused)
     */
    protected boolean reset(Connection connection, String id) {
        boolean errorsEncountered = false;

        // Now let's reset each property in turn
        Iterator i = accessorMutatorMap.values().iterator();
        while (i.hasNext()) {
            Method mutator = (Method) i.next();
            Object[] args = {defaultValues.get(mutator)};
            try {
                mutator.invoke(connection, args);
                if (log.isDebugEnabled()) {
                    log.debug(id + " - Reset: " + mutator.getName() + "(" + args[0] + ")");
                }
            } catch (Throwable t) {
                errorsEncountered = true;
                if (log.isDebugEnabled()) {
                    log.debug(id + " - Problem resetting: " + mutator.getName() + "(" + args[0] + ").", t);
                }
            }
        }

        // Let's see the state of autoCommit. It will help us give better advice in the log messages
        boolean autoCommit = true;
        try {
            autoCommit = connection.getAutoCommit();
        } catch (Throwable t) {
            errorsEncountered = true;
            log.warn(id + " - Problem calling connection.getAutoCommit()", t);
        }

        if (errorsEncountered) {

            log.warn(id + " - There were some problems resetting the connection. It will not be used again (just in case). "
                    + "The thread that is responsible is named '" + Thread.currentThread().getName() + "'");
            if (!autoCommit) {
                log.warn(id + " - The connection was closed with autoCommit=false. That is fine, but it might indicate that "
                        + "the problems that happened whilst trying to reset it were because a transaction is still in progress.");
            }
        }

        // Finally. reset autoCommit.
        if (!autoCommit) {
            try {
                connection.setAutoCommit(true);
                log.debug(id + " - autoCommit reset back to true");
            } catch (Throwable t) {
                errorsEncountered = true;
                log.warn(id + " - Problem calling connection.setAutoCommit(true)", t);
            }
        }

        return !errorsEncountered;
    }

}

/*
 Revision history:
 $Log: ConnectionResetter.java,v $
 Revision 1.5  2002/11/12 20:24:12  billhorsman
 checkstyle

 Revision 1.4  2002/11/12 20:18:23  billhorsman
 Made connection resetter a bit more friendly. Now, if it encounters any problems during
 reset then that connection is thrown away. This is going to cause you problems if you
 always close connections in an unstable state (e.g. with transactions open. But then
 again, it's better to know about that as soon as possible, right?

 Revision 1.3  2002/11/07 18:55:40  billhorsman
 demoted log message from info to debug

 Revision 1.2  2002/11/07 12:38:04  billhorsman
 performance improvement - only reset when it might be necessary

 Revision 1.1  2002/11/06 20:25:08  billhorsman
 New class responsible for resetting connections when
 they are returned to the pool.

*/

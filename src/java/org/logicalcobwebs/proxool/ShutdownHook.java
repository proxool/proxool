/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * This is instantiated statically by ProxoolFacade. It will automatically
 * close down all the connections when the JVM stops.
 * @version $Revision: 1.13 $, $Date: 2006/11/02 10:00:34 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
class ShutdownHook implements Runnable {

    private static final Log LOG = LogFactory.getLog(ShutdownHook.class);

    private static boolean registered;

    protected static void init() {
        if (!registered) {
            registered = true;
            new ShutdownHook();
        }
    }

    protected static void remove(Thread t) {
        Runtime runtime = Runtime.getRuntime();
        try {
            final Method removeShutdownHookMethod = Runtime.class.getMethod("removeShutdownHook", new Class[] {Thread.class});
            removeShutdownHookMethod.invoke(runtime, new Object[] {t});
            if (LOG.isDebugEnabled()) {
                LOG.debug("Removed shutdownHook");
            }
        } catch (NoSuchMethodException e) {
            LOG.warn("Proxool will have to be shutdown manually with ProxoolFacade.shutdown() because this version of the JDK does not support Runtime.getRuntime().addShutdownHook()");
        } catch (SecurityException e) {
            LOG.error("Problem removing shutdownHook", e);
        } catch (IllegalAccessException e) {
            LOG.error("Problem removing shutdownHook", e);
        } catch (InvocationTargetException e) {
            // Use getTargetException() because getCause() is only supported in JDK 1.4 and later
            Throwable cause = ((InvocationTargetException) e).getTargetException();
            if (cause instanceof IllegalStateException) {
                // This is probably because a shutdown is in progress. We can
                // safely ignore that.
            } else {
                LOG.error("Problem removing shutdownHook", e);
            }
        }
    }

    /**
     * Registers this ShutdownHook with Runtime
     */
    private ShutdownHook() {
        Thread t = new Thread(this);
        t.setName("ShutdownHook");
        Runtime runtime = Runtime.getRuntime();
        try {
            Method addShutdownHookMethod = Runtime.class.getMethod("addShutdownHook", new Class[] {Thread.class});
            addShutdownHookMethod.invoke(runtime, new Object[] {t});
            ProxoolFacade.setShutdownHook(t);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Registered shutdownHook");
            }
        } catch (NoSuchMethodException e) {
            LOG.warn("Proxool will have to be shutdown manually with ProxoolFacade.shutdown() because this version of the JDK does not support Runtime.getRuntime().addShutdownHook()");
        } catch (SecurityException e) {
            LOG.error("Problem registering shutdownHook", e);
        } catch (IllegalAccessException e) {
            LOG.error("Problem registering shutdownHook", e);
        } catch (InvocationTargetException e) {
            LOG.error("Problem registering shutdownHook", e);
        }
    }

    /**
     * Remove all connection pools without delay. Only runs if the
     * shutdown hook is {@link org.logicalcobwebs.proxool.ProxoolFacade#isShutdownHookEnabled() enabled}.
     * @see ProxoolFacade#removeAllConnectionPools
     */
    public void run() {
        if (ProxoolFacade.isShutdownHookEnabled()) {
            LOG.debug("Running ShutdownHook");
            Thread.currentThread().setName("Shutdown Hook");
            ProxoolFacade.shutdown(0);
        } else {
            LOG.debug("Skipping ShutdownHook because it's been disabled");
        }
    }

}


/*
 Revision history:
 $Log: ShutdownHook.java,v $
 Revision 1.13  2006/11/02 10:00:34  billhorsman
 Added ProxoolFacade.disableShutdownHook.

 Revision 1.12  2006/01/18 14:40:02  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.11  2003/12/16 09:09:32  billhorsman
 Switched from getCause() to getTargetException() so that we can trap the IllegalStateException in all JDKs.

 Revision 1.10  2003/11/16 18:19:14  chr32
 Started calling to Exception.getCause() via refletion to maintain compilability with < jdk 1.4 compilers.

 Revision 1.9  2003/10/27 12:32:06  billhorsman
 Fixed typos and silently ignore IllegalStateException during shutdownHook removal (it's probably because
 the JVM is shutting down).

 Revision 1.8  2003/09/07 22:05:15  billhorsman
 Now uses reflection to add ShutdownHook to Runtime so that it is JDK independent. Using JDK1.2
 will disable the shutdownHook and simply log a warning message that Proxool must be shutdown
 explicitly.

 Revision 1.7  2003/03/03 17:07:58  billhorsman
 name thread

 Revision 1.6  2003/03/03 11:11:58  billhorsman
 fixed licence

 Revision 1.5  2003/02/26 11:20:59  billhorsman
 removed debug

 Revision 1.4  2003/02/10 15:13:57  billhorsman
 fixed deprecated call

 Revision 1.3  2003/02/06 17:41:05  billhorsman
 now uses imported logging

 Revision 1.2  2003/02/04 17:19:11  billhorsman
 ShutdownHook now initialises

 Revision 1.1  2003/02/04 15:04:17  billhorsman
 New ShutdownHook

 */
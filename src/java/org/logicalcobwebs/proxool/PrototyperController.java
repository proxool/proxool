/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;

/**
 * Controls the {@link Prototyper prototypers}
 * @version $Revision: 1.10 $, $Date: 2006/01/18 14:40:01 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class PrototyperController {

    private static final Log LOG = LogFactory.getLog(PrototyperController.class);

    private static PrototyperThread prototyperThread;

    private static boolean keepSweeping;

    private static final String LOCK = "LOCK";

    private static void startPrototyper() {
        if (prototyperThread == null) {
            synchronized(LOCK) {
                if (prototyperThread == null) {
                    prototyperThread = new PrototyperThread("Prototyper");
                    prototyperThread.start();
                }
            }
        }
    }
    /**
     * Trigger prototyping immediately. Runs inside a new Thread so
     * control returns as quick as possible. You should call this whenever
     * you suspect that building more connections might be a good idea.
     * @param alias
     */
    protected static void triggerSweep(String alias) {
        try {
            // Ensure that we're not in the process of shutting down the pool
            ConnectionPool cp = ConnectionPoolManager.getInstance().getConnectionPool(alias);
            try {
                cp.acquirePrimaryReadLock();
                cp.getPrototyper().triggerSweep();
            } catch (InterruptedException e) {
                LOG.error("Couldn't acquire primary read lock", e);
            } finally {
                cp.releasePrimaryReadLock();
            }
        } catch (ProxoolException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Couldn't trigger prototyper triggerSweep for '" + alias + "'  - maybe it's just been shutdown");
            }
        }
        startPrototyper();
        try {
            // If we are currently sweeping this will cause it to loop through
            // once more
            keepSweeping = true;
            // If we aren't already started then this will start a new sweep
            if (prototyperThread != null) {
                prototyperThread.doNotify();
            }
        } catch (IllegalMonitorStateException e) {
            LOG.debug("Hmm", e);
            if (Thread.activeCount() > 10 && LOG.isInfoEnabled()) {
                LOG.info("Suspicious thread count of " + Thread.activeCount());
            }
        } catch (IllegalThreadStateException e) {
            // Totally expected. Should happen all the time. Just means that
            // we are already sweeping.
            if (LOG.isDebugEnabled()) {
                LOG.debug("Ignoring attempt to prototype whilst already prototyping");
            }
        }
    }

    public static boolean isKeepSweeping() {
        return keepSweeping;
    }

    public static void sweepStarted() {
        keepSweeping = false;
    }

    /**
     * Stop all house keeper threads
     */
    protected static void shutdown() {
        synchronized(LOCK) {
            if (prototyperThread != null) {
                LOG.info("Stopping " + prototyperThread.getName() + " thread");
                prototyperThread.cancel();
                prototyperThread = null;
            }
        }
    }
}


/*
 Revision history:
 $Log: PrototyperController.java,v $
 Revision 1.10  2006/01/18 14:40:01  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.9  2004/04/05 22:54:57  billhorsman
 Check if notify thread has been shutdown before triggering it.

 Revision 1.8  2004/03/26 15:58:56  billhorsman
 Fixes to ensure that house keeper and prototyper threads finish after shutdown.

 Revision 1.7  2004/03/25 22:02:15  brenuart
 First step towards pluggable ConnectionBuilderIF & ConnectionValidatorIF.
 Include some minor refactoring that lead to deprecation of some PrototyperController methods.

 Revision 1.5  2003/03/10 23:43:11  billhorsman
 reapplied checkstyle that i'd inadvertently let
 IntelliJ change...

 Revision 1.4  2003/03/10 16:28:02  billhorsman
 removed debug trace

 Revision 1.3  2003/03/10 15:26:47  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.2  2003/03/06 12:43:32  billhorsman
 removed paranoid debug

 Revision 1.1  2003/03/05 18:42:33  billhorsman
 big refactor of prototyping and house keeping to
 drastically reduce the number of threads when using
 many pools

 */
/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.SQLException;

/**
 * Controls the {@link Prototyper prototypers}
 * @version $Revision: 1.5 $, $Date: 2003/03/10 23:43:11 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class PrototyperController {

    private static final Log LOG = LogFactory.getLog(PrototyperController.class);

    private static PrototyperThread prototyperThread = new PrototyperThread("Prototyper");

    static {
        prototyperThread.start();
    }

    private static boolean keepSweeping;

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
        try {
            // If we are currently sweeping this will cause it to loop through
            // once more
            keepSweeping = true;
            boolean alive = prototyperThread.isAlive();
            // If we aren't already started then this will start a new sweep
            prototyperThread.doNotify();
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

    /**
     * Build a new connection
     * @param alias identifies the pool
     * @param state the initial state it will be created as (this allows us
     * to create it as {@link ConnectionInfoIF#STATUS_ACTIVE ACTIVE} and avoid
     * another thread grabbing it before we can)
     * @param creator for log audit
     * @return the new connection
     * @throws SQLException if there was a problem building the connection
     * @throws ProxoolException if the alias doesn't exist
     */
    protected static ProxyConnectionIF buildConnection(String alias, int state, String creator) throws SQLException, ProxoolException {
        return getConnectionPool(alias).getPrototyper().buildConnection(state, creator);
    }

    private static ConnectionPool getConnectionPool(String alias) throws ProxoolException {
        return ConnectionPoolManager.getInstance().getConnectionPool(alias);
    }

    /**
     * Checks whether we are currently already building too many connections
     * @param alias identifies the pool
     * @throws SQLException if the throttle has been reached
     */
    protected static void checkSimultaneousBuildThrottle(String alias) throws SQLException, ProxoolException {
        getConnectionPool(alias).getPrototyper().checkSimultaneousBuildThrottle();
    }

    /**
     * Cancel this prototyper and stop all prototyping immediately.
     * @param alias identifies the pool
     */
    public static void cancel(String alias)  {
        try {
            getConnectionPool(alias).getPrototyper().cancel();
        } catch (ProxoolException e) {
            LOG.error("Couldn't cancel prototyper", e);
        }
    }

    protected static void connectionRemoved(String alias) {
        try {
            getConnectionPool(alias).getPrototyper().connectionRemoved();
        } catch (ProxoolException e) {
            LOG.debug("Ignoring connection removed from cancelled prototyper");
        }
    }

    public static boolean isKeepSweeping() {
        return keepSweeping;
    }

    public static void sweepStarted() {
        keepSweeping = false;
    }

}


/*
 Revision history:
 $Log: PrototyperController.java,v $
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
/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;

/**
 * Controls the {@link Prototyper prototypers}
 * @version $Revision: 1.2 $, $Date: 2003/03/06 12:43:32 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class PrototyperController {

    private static final Log LOG = LogFactory.getLog(PrototyperController.class);

    private static Map prototypers = new HashMap();

    private static PrototyperThread prototyperThread = new PrototyperThread("Prototyper");

    private static boolean keepSweeping;

    /**
     * Trigger prototyping immediately. Runs inside a new Thread so
     * control returns as quick as possible. You should call this whenever
     * you suspect that building more connections might be a good idea.
     * @param alias
     */
    protected static void triggerSweep(String alias) {
        try {
            getPrototyper(alias).triggerSweep();
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
            if (LOG.isDebugEnabled()) {
                LOG.debug("Triggering " + prototyperThread.getName() + " sweep (threadCount=" + Thread.activeCount() + ", alive=" + alive + ")");
            }
        } catch (IllegalMonitorStateException e) {
            LOG.debug("Hmm", e);
        } catch (IllegalThreadStateException e) {
            // Totally expected. Should happen all the time. Just means that
            // we are already sweeping.
            if (LOG.isDebugEnabled()) {
                LOG.debug("Ignoring attempt to prototype whilst already prototyping");
            }
        }
    }

    protected synchronized static void register(ConnectionPool connectionPool) {
        String alias = connectionPool.getDefinition().getAlias();
        LOG.debug("Registering '" + alias + "' prototyper");
        prototypers.put(alias, new Prototyper(connectionPool));

        if (!prototyperThread.isAlive()) {
            prototyperThread.start();
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
     * @throws ProxoolException if the prototyper isn't {@link #register registered}
     */
    protected static ProxyConnectionIF buildConnection(String alias, int state, String creator) throws SQLException, ProxoolException {
        return getPrototyper(alias).buildConnection(state, creator);
    }

    private static Prototyper getPrototyper(String alias) throws ProxoolException {
        Prototyper p = (Prototyper) prototypers.get(alias);
        if (p == null) {
            throw new ProxoolException("Tried to use an unregistered prototyper '"+ alias + "'");
        }
        return p;
    }

    /**
     * Checks whether we are currently already building too many connections
     * @param alias identifies the pool
     * @throws SQLException if the throttle has been reached
     */
    protected static void checkSimultaneousBuildThrottle(String alias) throws SQLException, ProxoolException {
        getPrototyper(alias).checkSimultaneousBuildThrottle();
    }

    /**
     * Cancel this prototyper and stop all prototyping immediately. To
     * {@link #buildConnection use} it again you will have to
     * {@link #register register}
     * @param alias identifies the pool
     */
    public static void cancel(String alias)  {
        try {
            getPrototyper(alias).cancel();
        } catch (ProxoolException e) {
            LOG.error("Couldn't cancel prototyper", e);
        }
        prototypers.remove(alias);
    }

    protected static void connectionRemoved(String alias) {
        try {
            getPrototyper(alias).connectionRemoved();
        } catch (ProxoolException e) {
            LOG.debug("Ignoring connection removed from cancelled prototyper");
        }
    }

    protected static synchronized Prototyper[] getPrototypers() {
        return (Prototyper[]) prototypers.values().toArray(new Prototyper[prototypers.size()]);
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
 Revision 1.2  2003/03/06 12:43:32  billhorsman
 removed paranoid debug

 Revision 1.1  2003/03/05 18:42:33  billhorsman
 big refactor of prototyping and house keeping to
 drastically reduce the number of threads when using
 many pools

 */
/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.logicalcobwebs.proxool.util.FastArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Schedules when to run the house keeper
 * @version $Revision: 1.3 $, $Date: 2003/03/10 23:43:10 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class HouseKeeperController {

    private static final Log LOG = LogFactory.getLog(HouseKeeperController.class);

    private static Map houseKeepers = new HashMap();

    private static List houseKeeperList = new FastArrayList();

    private static int houseKeeperIndex = 0;

    private static List houseKeeperThreads = new FastArrayList();

    private static final Object LOCK = new Integer(1);

    private static HouseKeeper getHouseKeeper(String alias) throws ProxoolException {
        final HouseKeeper houseKeeper = (HouseKeeper) houseKeepers.get(alias);
        if (houseKeeper == null) {
            throw new ProxoolException("Tried to use an unregistered house keeper '" + alias + "'");
        }
        return houseKeeper;
    }

    /**
     * Get the next house keeper that needs to be run
     * @return the house keeper to run, or null if there is nothing to do.
     */
    protected static HouseKeeper getHouseKeeperToRun() {
        HouseKeeper houseKeeper = null;
        synchronized (LOCK) {
            for (int i = 0; i < houseKeeperList.size(); i++) {
                if (houseKeeperIndex > houseKeeperList.size() - 1) {
                    houseKeeperIndex = 0;
                }
                HouseKeeper hk = (HouseKeeper) houseKeeperList.get(houseKeeperIndex);
                houseKeeperIndex++;
                if (hk.isSweepDue()) {
                    houseKeeper = hk;
                    break;
                }
            }
        }
        return houseKeeper;
    }

    protected static void sweepNow(String alias) {
        try {
            getHouseKeeper(alias).sweep();
        } catch (ProxoolException e) {
            LOG.error("Couldn't run house keeper for " + alias, e);
        }
    }

    /**
     * Schedule a regular triggerSweep
     * @param connectionPool  identifies the pool
     */
    protected static void register(ConnectionPool connectionPool) {
        String alias = connectionPool.getDefinition().getAlias();
        LOG.debug("Registering '" + alias + "' house keeper");
        HouseKeeper houseKeeper = new HouseKeeper(connectionPool);
        synchronized (LOCK) {
            houseKeepers.put(alias, houseKeeper);
            houseKeeperList.add(houseKeeper);

            if (houseKeeperThreads.size() == 0) {
                HouseKeeperThread hkt = new HouseKeeperThread("HouseKeeper");
                LOG.debug("Starting a house keeper thread");
                hkt.start();
                houseKeeperThreads.add(hkt);
            }
        }
    }

    protected static void cancel(String alias) throws ProxoolException {
        HouseKeeper hk = getHouseKeeper(alias);
        houseKeepers.remove(alias);
        houseKeeperList.remove(hk);
    }

}


/*
 Revision history:
 $Log: HouseKeeperController.java,v $
 Revision 1.3  2003/03/10 23:43:10  billhorsman
 reapplied checkstyle that i'd inadvertently let
 IntelliJ change...

 Revision 1.2  2003/03/10 15:26:46  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.1  2003/03/05 18:42:33  billhorsman
 big refactor of prototyping and house keeping to
 drastically reduce the number of threads when using
 many pools

 */
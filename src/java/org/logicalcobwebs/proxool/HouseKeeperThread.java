/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

/**
 * Responsible for running {@link HouseKeeper#sweep sweep}
 *
 * @version $Revision: 1.4 $, $Date: 2004/03/26 15:58:56 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class HouseKeeperThread extends Thread {

    private static final Log LOG = LogFactory.getLog(HouseKeeperThread.class);

    private boolean stop;

    public HouseKeeperThread(String name) {
        setDaemon(true);
        setName(name);
    }

    public void run() {

        while (!stop) {
            HouseKeeper hk = HouseKeeperController.getHouseKeeperToRun();
            while (hk != null && !stop) {
                try {
//                    if (LOG.isDebugEnabled()) {
//                        LOG.debug("About to sweep " + hk.getAlias());
//                    }
                    hk.sweep();
                } catch (ProxoolException e) {
                    LOG.error("Couldn't sweep " + hk.getAlias(), e);
                }
                hk = HouseKeeperController.getHouseKeeperToRun();
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                LOG.error("Interrupted", e);
            }
        }

    }

    protected void cancel() {
        stop = true;
    }

}


/*
 Revision history:
 $Log: HouseKeeperThread.java,v $
 Revision 1.4  2004/03/26 15:58:56  billhorsman
 Fixes to ensure that house keeper and prototyper threads finish after shutdown.

 Revision 1.3  2003/04/19 12:57:29  billhorsman
 removed redundant debug

 Revision 1.2  2003/03/10 15:26:47  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.1  2003/03/05 18:42:33  billhorsman
 big refactor of prototyping and house keeping to
 drastically reduce the number of threads when using
 many pools

 */
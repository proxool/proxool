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
 * @version $Revision: 1.2 $, $Date: 2003/03/10 15:26:47 $
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
            while (hk != null) {
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("About to sweep " + hk.getAlias());
                    }
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
 Revision 1.2  2003/03/10 15:26:47  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.1  2003/03/05 18:42:33  billhorsman
 big refactor of prototyping and house keeping to
 drastically reduce the number of threads when using
 many pools

 */
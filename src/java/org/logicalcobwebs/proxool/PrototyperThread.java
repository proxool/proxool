/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

/**
 * Responsible for running {@link Prototyper#sweep sweep}. There
 * could be just one of the objects, or more.
 * @version $Revision: 1.1 $, $Date: 2003/03/05 18:42:33 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class PrototyperThread extends Thread {

    private static final ThreadGroup prototyperThreadGroup = new ThreadGroup("prototyperThreadGroup");

    private static final Log LOG = LogFactory.getLog(PrototyperThread.class);

    public PrototyperThread(String name) {
        super(prototyperThreadGroup, name);
        setDaemon(true);
    }

    public void run() {

        while (true) {
            int sweptCount = 0;
            while (PrototyperController.isKeepSweeping()) {
                PrototyperController.sweepStarted();
                Prototyper[] prototyperArray = PrototyperController.getPrototypers();
                for (int i = 0; i < prototyperArray.length; i++) {
                    if (prototyperArray[i].isSweepNeeded()) {
                        prototyperArray[i].sweep();
                        sweptCount++;
                    }
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Swept " + sweptCount + " pools");
            }

            doWait();
        }
    }

    private synchronized void doWait() {
        try {
            wait();
        } catch (InterruptedException e) {
            LOG.debug("Expected interruption of sleep");
        }
    }

    protected synchronized void doNotify() {
        notify();
    }

}


/*
 Revision history:
 $Log: PrototyperThread.java,v $
 Revision 1.1  2003/03/05 18:42:33  billhorsman
 big refactor of prototyping and house keeping to
 drastically reduce the number of threads when using
 many pools

 */
/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

/**
 * This is instantiated statically by ProxoolFacade. It will automatically
 * close down all the connections when teh JVM stops.
 *
 * @version $Revision: 1.3 $, $Date: 2003/02/06 17:41:05 $
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

    /**
     * Registers this ShutdownHook with Runtime
     */
    private ShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this));
        LOG.debug("Registering ShutdownHook");
    }

    /**
     * Remove all connection pools without delay
     * @see ProxoolFacade#removeAllConnectionPools
     */
    public void run() {
        LOG.debug("Running ShutdownHook");
        Thread.currentThread().setName("Shutdown Hook");
        ProxoolFacade.removeAllConnectionPools(0);
    }

}


/*
 Revision history:
 $Log: ShutdownHook.java,v $
 Revision 1.3  2003/02/06 17:41:05  billhorsman
 now uses imported logging

 Revision 1.2  2003/02/04 17:19:11  billhorsman
 ShutdownHook now initialises

 Revision 1.1  2003/02/04 15:04:17  billhorsman
 New ShutdownHook

 */
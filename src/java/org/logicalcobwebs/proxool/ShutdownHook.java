/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is instantiated statically by ProxoolFacade. It will automatically
 * close down all the connections when teh JVM stops.
 *
 * @version $Revision: 1.1 $, $Date: 2003/02/04 15:04:17 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
class ShutdownHook implements Runnable {

    private static final Log LOG = LogFactory.getLog(ShutdownHook.class);

    /**
     * Registers this ShutdownHook with Runtime
     */
    public ShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this));
    }

    /**
     * Remove all connection pools without delay
     * @see ProxoolFacade#removeAllConnectionPools
     */
    public void run() {
        Thread.currentThread().setName("Shutdown Hook");
        ProxoolFacade.removeAllConnectionPools(0);
    }

}


/*
 Revision history:
 $Log: ShutdownHook.java,v $
 Revision 1.1  2003/02/04 15:04:17  billhorsman
 New ShutdownHook

 */
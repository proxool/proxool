/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

/**
 * This class is responsible for closing down all pools when the JVM
 * stops. Only it uses code only found in JDK 1.3 and later. So here it
 * does nothing at all. You are responsible for calling
 * {@link ProxoolFacade#removeAllConnectionPools} yourself.
 *
 * @version $Revision: 1.2 $, $Date: 2003/02/08 00:56:03 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
class ShutdownHook {

    /**
     * Does nothing.
     */
    protected static void init() {
        // Nothing to do.
    }

}


/*
 Revision history:
 $Log: ShutdownHook.java,v $
 Revision 1.2  2003/02/08 00:56:03  billhorsman
 merge changes from main source

 Revision 1.1  2003/02/04 15:09:39  billhorsman
 JDK 1.2 disables use of ShutdownHook

 */
/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

/**
 * Monitors the activity of the pool so you can see whether it is
 * quiet, busy, overloaded, or down.
 * @version $Revision: 1.1 $, $Date: 2002/09/13 08:13:30 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public interface StateListenerIF {

    public final int STATE_QUIET = 0;

    public final int STATE_BUSY = 1;

    public final int STATE_OVERLOADED = 2;

    public final int STATE_DOWN = 3;

    void upStateChanged(int upState);
}

/*
 Revision history:
 $Log: StateListenerIF.java,v $
 Revision 1.1  2002/09/13 08:13:30  billhorsman
 Initial revision

 Revision 1.5  2002/07/10 16:14:47  billhorsman
 widespread layout changes and move constants into ProxoolConstants

 Revision 1.4  2002/06/28 11:19:47  billhorsman
 improved doc

 Revision 1.3  2002/06/28 11:15:41  billhorsman
 didn't really need ListenerIF

*/

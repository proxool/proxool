/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

/**
 * Monitors the activity of the pool so you can see whether it is
 * quiet, busy, overloaded, or down. You need to
 * {@link ProxoolFacade#setStateListener register} it with
 * ProxoolFacade first.
 *
 * <pre>
 * String alias = "myPool";
 * StateListenerIF myStateListener = new MyStateListener();
 * ProxoolFacade.setStateListener(alias, myStateListener);
 * </pre>
 *
 * @version $Revision: 1.3 $, $Date: 2002/12/15 19:21:42 $
 * @author billhorsman
 * @author $Author: chr32 $ (current maintainer)
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
 Revision 1.3  2002/12/15 19:21:42  chr32
 Changed @linkplain to @link (to preserve JavaDoc for 1.2/1.3 users).

 Revision 1.2  2002/10/25 16:00:27  billhorsman
 added better class javadoc

 Revision 1.1.1.1  2002/09/13 08:13:30  billhorsman
 new

 Revision 1.5  2002/07/10 16:14:47  billhorsman
 widespread layout changes and move constants into ProxoolConstants

 Revision 1.4  2002/06/28 11:19:47  billhorsman
 improved doc

 Revision 1.3  2002/06/28 11:15:41  billhorsman
 didn't really need ListenerIF

*/

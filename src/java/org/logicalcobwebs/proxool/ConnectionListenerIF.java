/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * You can listen to the lifecycle of a connection. Sometimes, you may
 * want to perform a task when the connection is born or dies. Actually,
 * the reason why we originally did this is now obsolete. But the code
 * remains here just in case. You need to
 * {@link ProxoolFacade#setConnectionListener register}
 * your implementation with ProxoolFacade.
 *
 * <pre>
 * String alias = "myPool";
 * ConnectionListenerIF myConnectionListener = new MyConnectionListener();
 * ProxoolFacade.setConnectionListener(alias, myConnectionListener);
 * </pre>
 *
 * @version $Revision: 1.5 $, $Date: 2002/12/15 19:21:42 $
 * @author billhorsman
 * @author $Author: chr32 $ (current maintainer)
 */
public interface ConnectionListenerIF {

    /**
     * Happens everytime we create a new connection. You can use this
     * to allocate resources to a connection that might be useful during
     * the lifetime of the connection.
     *
     * @param connection the connection that has just been created
     * @throws SQLException if anything goes wrong (which will then be logged but ignored)
     */
    void onBirth(Connection connection) throws SQLException;

    /**
     * Happens just before we expire a connection. You can use this to
     * reclaim resources from a connection.
     *
     * @param connection the connection that is about to expire
     * @throws SQLException if anything goes wrong (which will then be logged but ignored)
     */
    void onDeath(Connection connection) throws SQLException;

    /**
     * Happens after every successful execute. Note that the command
     * is not fully implemented at this stage. At some point it might represent
     * the SQL that is sent to the database (or the procedure call that was used).
     *
     * @param command what command was being executed
     * @param elapsedTime how long the call took (in milliseconds)
     */
    void onExecute(String command, long elapsedTime);

    /**
     * Happens everytime an exception was thrown during an execute method
     * Note that the command
     * is not fully implemented at this stage. At some point it might represent
     * the SQL that is sent to the database (or the procedure call that was used).
     *
     * @param command what command was being executed
     * @param exception what exception was thrown
     */
    void onFail(String command, Exception exception);

}

/*
 Revision history:
 $Log: ConnectionListenerIF.java,v $
 Revision 1.5  2002/12/15 19:21:42  chr32
 Changed @linkplain to @link (to preserve JavaDoc for 1.2/1.3 users).

 Revision 1.4  2002/10/25 16:00:20  billhorsman
 added better class javadoc

 Revision 1.3  2002/10/23 21:04:36  billhorsman
 checkstyle fixes (reduced max line width and lenient naming convention

 Revision 1.2  2002/10/16 11:45:52  billhorsman
 removed obsolete cleanupClob method and added more javadoc

 Revision 1.1.1.1  2002/09/13 08:12:34  billhorsman
 new

 Revision 1.5  2002/08/24 19:43:04  billhorsman
 new execute events

 Revision 1.4  2002/06/28 11:15:41  billhorsman
 didn't really need ListenerIF

*/

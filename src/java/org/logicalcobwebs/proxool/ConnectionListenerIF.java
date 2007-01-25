/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
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
 * ProxoolFacade.{@link org.logicalcobwebs.proxool.ProxoolFacade#addConnectionListener addConnectionListener}(alias, myConnectionListener);
 * </pre>
 *
 * @version $Revision: 1.9 $, $Date: 2007/01/25 23:38:24 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public interface ConnectionListenerIF {

    /**
     * We are killing a connection because the
     * {@link org.logicalcobwebs.proxool.ProxoolConstants#MAXIMUM_ACTIVE_TIME MAXIMUM_ACTIVE_TIME}
     * has been exceeded.
     * @see #onDeath(java.sql.Connection, int)
     */
    static final int MAXIMUM_ACTIVE_TIME_EXPIRED = 1;

    /**
     * We are killing a connection because it's manually been expired (by something external to
     * Proxool)
     * @see #onDeath(java.sql.Connection, int)
     */
    static final int MANUAL_EXPIRY = 2;

    /**
     * We are killing a connection because it has not been
     * {@link org.logicalcobwebs.proxool.ConnectionValidatorIF#validate(ConnectionPoolDefinitionIF, java.sql.Connection) validated}.
     * @see #onDeath(java.sql.Connection, int)
     */
    static final int VALIDATION_FAIL = 3;

    /**
     * We are killing a connection because Proxool is shutting down
     * @see #onDeath(java.sql.Connection, int)
     */
    static final int SHUTDOWN = 4;

    /**
     * We are killing a connection because it couldn't be {@link org.logicalcobwebs.proxool.ConnectionResetter#reset(java.sql.Connection, String) reset}
     * after it was returned to the pool and we don't want to give it out again in an unknown state.
     * @see #onDeath(java.sql.Connection, int)
     */
    static final int RESET_FAIL = 5;

    /**
     * We are killing a connection because the routine house keeper test failed
     * @see #onDeath(java.sql.Connection, int)
     */
    static final int HOUSE_KEEPER_TEST_FAIL = 6;

    /**
     * We are killing a connection because it's {@link org.logicalcobwebs.proxool.ProxoolConstants#MAXIMUM_CONNECTION_LIFETIME MAXIMUM_CONNECTION_LIFETIME}
     * has been exceeded.
     * @see #onDeath(java.sql.Connection, int)
     */
    static final int MAXIMUM_CONNECTION_LIFETIME_EXCEEDED = 7;

    /**
     * We are killing a connection because a {@link org.logicalcobwebs.proxool.ProxoolConstants#FATAL_SQL_EXCEPTION FATAL_SQL_EXCEPTION}
     * has been detected.
     * @see #onDeath(java.sql.Connection, int)
     */
    static final int FATAL_SQL_EXCEPTION_DETECTED = 8;

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
     * @param reasonCode {@link #MAXIMUM_ACTIVE_TIME_EXPIRED},
     * {@link #HOUSE_KEEPER_TEST_FAIL},
     * {@link #FATAL_SQL_EXCEPTION_DETECTED},
     * {@link #MANUAL_EXPIRY},
     * {@link #MAXIMUM_CONNECTION_LIFETIME_EXCEEDED},
     * {@link #RESET_FAIL},
     * {@link #SHUTDOWN}, or
     * {@link #VALIDATION_FAIL}
     * @throws SQLException if anything goes wrong (which will then be logged but ignored)
     */
    void onDeath(Connection connection, int reasonCode) throws SQLException;

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
 Revision 1.9  2007/01/25 23:38:24  billhorsman
 Scrapped onAboutToDie and altered onDeath signature instead. Now includes reasonCode (see ConnectionListenerIF)

 Revision 1.8  2007/01/25 00:10:24  billhorsman
 New onAboutToDie event for ConnectionListenerIF that gets called if the maximum-active-time is exceeded.

 Revision 1.7  2003/03/03 11:11:57  billhorsman
 fixed licence

 Revision 1.6  2003/02/08 00:35:30  billhorsman
 doc

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

/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Responisble for house keeping one pool
 *
 * @version $Revision: 1.5 $, $Date: 2006/01/18 14:40:01 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
class HouseKeeper {

    private static final Log LOG = LogFactory.getLog(HouseKeeper.class);

    private ConnectionPool connectionPool;

    private long timeLastSwept;

    public HouseKeeper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

   protected void sweep() throws ProxoolException {
       ConnectionPoolDefinitionIF definition = connectionPool.getDefinition();
       Log log = connectionPool.getLog();
       Statement testStatement = null;
       try {

           connectionPool.acquirePrimaryReadLock();

           // Right, now we know we're the right thread then we can carry on house keeping
           Connection connection = null;
           ProxyConnectionIF proxyConnection = null;

           int recentlyStartedActiveConnectionCountTemp = 0;

           // sanity check
           int[] verifiedConnectionCountByState = new int[4];

           ProxyConnectionIF[] proxyConnections = connectionPool.getProxyConnections();
           for (int i = 0; i < proxyConnections.length; i++) {
               proxyConnection = proxyConnections[i];
               connection = proxyConnection.getConnection();

               if (!connectionPool.isConnectionPoolUp()) {
                   break;
               }

               // First lets check whether the connection still works. We should only validate
               // connections that are not is use!  SetOffline only succeeds if the connection
               // is available.
               if (proxyConnection.setStatus(ProxyConnectionIF.STATUS_AVAILABLE, ProxyConnectionIF.STATUS_OFFLINE)) {
                   try {
                       testStatement = connection.createStatement();

                       // Some DBs return an object even if DB is shut down
                       if (proxyConnection.isReallyClosed()) {
                           proxyConnection.setStatus(ProxyConnectionIF.STATUS_OFFLINE, ProxyConnectionIF.STATUS_NULL);
                           connectionPool.removeProxyConnection(proxyConnection, "it appears to be closed", ConnectionPool.FORCE_EXPIRY, true);
                       }

                       String sql = definition.getHouseKeepingTestSql();
                       if (sql != null && sql.length() > 0) {
                           // A Test Statement has been provided. Execute it!
                           boolean testResult = false;
                           try {
                               testResult = testStatement.execute(sql);
                           } finally {
                               if (log.isDebugEnabled() && definition.isVerbose()) {
                                   log.debug(connectionPool.displayStatistics() + " - Testing connection " + proxyConnection.getId() + (testResult ? ": True" : ": False"));
                               }
                           }
                       }

                       proxyConnection.setStatus(ProxyConnectionIF.STATUS_OFFLINE, ProxyConnectionIF.STATUS_AVAILABLE);
                   } catch (Throwable e) {
                       // There is a problem with this connection.  Let's remove it!
                       proxyConnection.setStatus(ProxyConnectionIF.STATUS_OFFLINE, ProxyConnectionIF.STATUS_NULL);
                       connectionPool.removeProxyConnection(proxyConnection, "it has problems: " + e, ConnectionPool.REQUEST_EXPIRY, true);
                   } finally {
                       try {
                           testStatement.close();
                       } catch (Throwable t) {
                           // Never mind.
                       }
                   }
               } // END if (poolableConnection.setOffline())
               // Now to check whether the connection is due for expiry
               if (proxyConnection.getAge() > definition.getMaximumConnectionLifetime()) {
                   final String reason = "age is " + proxyConnection.getAge() + "ms";
                   // Check whether we can make it offline
                   if (proxyConnection.setStatus(ProxyConnectionIF.STATUS_AVAILABLE, ProxyConnectionIF.STATUS_OFFLINE)) {
                       if (proxyConnection.setStatus(ProxyConnectionIF.STATUS_OFFLINE, ProxyConnectionIF.STATUS_NULL)) {
                           // It is.  Expire it now .
                           connectionPool.expireProxyConnection(proxyConnection, reason, ConnectionPool.REQUEST_EXPIRY);
                       }
                   } else {
                       // Oh no, it's in use.  Never mind, we'll mark it for expiry
                       // next time it is available.  This will happen in the
                       // putConnection() method.
                       proxyConnection.markForExpiry(reason);
                       if (log.isDebugEnabled()) {
                           log.debug(connectionPool.displayStatistics() + " - #" + FormatHelper.formatMediumNumber(proxyConnection.getId())
                                   + " marked for expiry.");
                       }
                   } // END if (poolableConnection.setOffline())
               } // END if (poolableConnection.getAge() > maximumConnectionLifetime)

               // Now let's see if this connection has been active for a
               // suspiciously long time.
               if (proxyConnection.isActive()) {

                   long activeTime = System.currentTimeMillis() - proxyConnection.getTimeLastStartActive();

                   if (activeTime < definition.getRecentlyStartedThreshold()) {

                       // This connection hasn't been active for all that long
                       // after all. And as long as we have at least one
                       // connection that is "actively active" then we don't
                       // consider the pool to be down.
                       recentlyStartedActiveConnectionCountTemp++;
                   }

                   if (activeTime > definition.getMaximumActiveTime()) {

                       // This connection has been active for way too long. We're
                       // going to kill it :)
                       connectionPool.removeProxyConnection(proxyConnection,
                               "it has been active for too long", ConnectionPool.FORCE_EXPIRY, true);
                       String lastSqlCallMsg;
                       if (proxyConnection.getLastSqlCall() != null) {
                           lastSqlCallMsg = ", and the last SQL it performed is '" + proxyConnection.getLastSqlCall() + "'.";
                       } else if (!proxyConnection.getDefinition().isTrace()) {
                           lastSqlCallMsg = ", but the last SQL it performed is unknown because the trace property is not enabled.";
                       } else {
                           lastSqlCallMsg = ", but the last SQL it performed is unknown.";
                       }
                       log.warn("#" + FormatHelper.formatMediumNumber(proxyConnection.getId()) + " was active for " + activeTime
                               + " milliseconds and has been removed automaticaly. The Thread responsible was named '"
                               + proxyConnection.getRequester() + "'" + lastSqlCallMsg);

                   }

               }

               // What have we got?
               verifiedConnectionCountByState[proxyConnection.getStatus()]++;

           }

           calculateUpState(recentlyStartedActiveConnectionCountTemp);
       } catch (Throwable e) {
           // We don't want the housekeeping thread to fall over!
           log.error("Housekeeping log.error( :", e);
       } finally {
           connectionPool.releasePrimaryReadLock();
           timeLastSwept = System.currentTimeMillis();
           if (definition.isVerbose()) {
               if (log.isDebugEnabled()) {
                   log.debug(connectionPool.displayStatistics() + " - House keeping triggerSweep done");
               }
           }
       }

       PrototyperController.triggerSweep(definition.getAlias());

   }

    /**
     * Get the time since the last sweep was completed
     * @return timeSinceLastSweep (milliseconds)
     */
    private long getTimeSinceLastSweep() {
        return System.currentTimeMillis() - timeLastSwept;
    }

    /**
     * Should we sleep
     * @return true if the time since the last sweep was completed is greater
     * than the {@link ConnectionPoolDefinitionIF#getHouseKeepingSleepTime houseKeepingSleepTime}
     * property.
     */
    protected boolean isSweepDue() {
        if (connectionPool.isConnectionPoolUp()) {
            return (getTimeSinceLastSweep() > connectionPool.getDefinition().getHouseKeepingSleepTime());
        } else {
            LOG.warn("House keeper is still being asked to sweep despite the connection pool being down");
            return false;
        }
    }

    private void calculateUpState(int recentlyStartedActiveConnectionCount) {

        try {

            int calculatedUpState = StateListenerIF.STATE_QUIET;

/* We're up if the last time we tried to make a connection it
             * was successful
             */

/* I've changed the way we do this. Just because we failed to create
             * a connection doesn't mean we're down. As long as we have some
             * available connections, or the active ones we have aren't locked
             * up then we should be able to struggle on. The last thing we want
             * to do is say we're down when we're not!
             */

            // if (this.lastCreateWasSuccessful) {
            final int availableConnectionCount = connectionPool.getAvailableConnectionCount();
            if (availableConnectionCount > 0 || recentlyStartedActiveConnectionCount > 0) {

/* Defintion of overloaded is that we refused a connection
                 * (because we were too busy) within the last minute.
                 */

                if (connectionPool.getTimeOfLastRefusal() > (System.currentTimeMillis()
                        - connectionPool.getDefinition().getOverloadWithoutRefusalLifetime())) {
                    calculatedUpState = StateListenerIF.STATE_OVERLOADED;
                } else if (connectionPool.getActiveConnectionCount() > 0) {
                    /* Are we doing anything at all?
                 */
                    calculatedUpState = StateListenerIF.STATE_BUSY;
                }

            } else {
                calculatedUpState = StateListenerIF.STATE_DOWN;
            }

            connectionPool.setUpState(calculatedUpState);

        } catch (Exception e) {
            LOG.error(e);
        }
    }

    /**
     * Identifies the pool we are sweeping
     * @return alias
     */
    protected String getAlias() {
        return connectionPool.getDefinition().getAlias();
    }

}


/*
 Revision history:
 $Log: HouseKeeper.java,v $
 Revision 1.5  2006/01/18 14:40:01  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.4  2005/10/02 12:35:06  billhorsman
 Improve message when closing a connection that has been active for too long

 Revision 1.3  2003/09/11 23:57:48  billhorsman
 Test SQL now traps Throwable, not just SQLException.

 Revision 1.2  2003/03/10 15:26:46  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.1  2003/03/05 18:42:33  billhorsman
 big refactor of prototyping and house keeping to
 drastically reduce the number of threads when using
 many pools

 */
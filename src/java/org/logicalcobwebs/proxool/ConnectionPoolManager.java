/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @version $Revision: 1.16 $, $Date: 2006/01/18 14:40:01 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
class ConnectionPoolManager {
    private static final Object LOCK = new Object();

    private Map connectionPoolMap = new HashMap();

    private Set connectionPools = new HashSet();

    private static ConnectionPoolManager connectionPoolManager = null;

    private static final Log LOG = LogFactory.getLog(ProxoolFacade.class);

    public static ConnectionPoolManager getInstance() {
        if (connectionPoolManager == null) {
            synchronized (LOCK) {
                if (connectionPoolManager == null) {
                    connectionPoolManager = new ConnectionPoolManager();
                }
            }
        }
        return connectionPoolManager;
    }

    private ConnectionPoolManager() {
    }

    /**
     * Get the pool by the alias
     * @param alias identifies the pool
     * @return the pool
     * @throws ProxoolException if it couldn't be found
     */
    protected ConnectionPool getConnectionPool(String alias) throws ProxoolException {
        ConnectionPool cp = (ConnectionPool) connectionPoolMap.get(alias);
        if (cp == null) {
            throw new ProxoolException(getKnownPools(alias));
        }
        return cp;
    }

    /**
     * Convenient method for outputing a message explaining that a pool couldn't
     * be found and listing the ones that could be found.
     * @param alias identifies the pool
     * @return a description of the wht the pool couldn't be found
     */
    protected String getKnownPools(String alias) {
        StringBuffer message = new StringBuffer("Couldn't find a pool called '" + alias + "'. Known pools are: ");
        Iterator i = connectionPoolMap.keySet().iterator();
        while (i.hasNext()) {
            message.append((String) i.next());
            message.append(i.hasNext() ? ", " : ".");
        }
        return message.toString();
    }

    /**
     * Whether the pool is already registered
     * @param alias how we identify the pool
     * @return true if it already exists, else false
     */
    protected boolean isPoolExists(String alias) {
        return connectionPoolMap.containsKey(alias);
    }

    /** @return an array of the connection pools */
    protected ConnectionPool[] getConnectionPools() {
        return (ConnectionPool[]) connectionPools.toArray(new ConnectionPool[connectionPools.size()]);
    }

    protected ConnectionPool createConnectionPool(ConnectionPoolDefinition connectionPoolDefinition) throws ProxoolException {
        ConnectionPool connectionPool = new ConnectionPool(connectionPoolDefinition);
        connectionPools.add(connectionPool);
        connectionPoolMap.put(connectionPoolDefinition.getAlias(), connectionPool);
        return connectionPool;
    }

    protected void removeConnectionPool(String name) {
        ConnectionPool cp = (ConnectionPool) connectionPoolMap.get(name);
        if (cp != null) {
            connectionPoolMap.remove(cp.getDefinition().getAlias());
            connectionPools.remove(cp);
        } else {
            LOG.info("Ignored attempt to remove either non-existent or already removed connection pool " + name);
        }
    }

    public String[] getConnectionPoolNames() {
        return (String[]) connectionPoolMap.keySet().toArray(new String[connectionPoolMap.size()]);
    }
}

/*
 Revision history:
 $Log: ConnectionPoolManager.java,v $
 Revision 1.16  2006/01/18 14:40:01  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.15  2003/03/11 14:51:51  billhorsman
 more concurrency fixes relating to snapshots

 Revision 1.14  2003/03/10 23:43:09  billhorsman
 reapplied checkstyle that i'd inadvertently let
 IntelliJ change...

 Revision 1.13  2003/03/10 15:26:45  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.12  2003/03/03 11:11:57  billhorsman
 fixed licence

 Revision 1.11  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.10  2003/02/28 10:42:59  billhorsman
 ConnectionPoolManager now passes ProxoolFacade an
 array of ConnectionPools rather than a Collection
 to avoid a ConcurrentModificationException during
 shutdown.

 Revision 1.9  2003/02/07 10:27:47  billhorsman
 change in shutdown procedure to allow re-registration

 Revision 1.8  2003/02/06 17:41:04  billhorsman
 now uses imported logging

 Revision 1.7  2003/01/27 18:26:36  billhorsman
 refactoring of ProxyConnection and ProxyStatement to
 make it easier to write JDK 1.2 patch

 Revision 1.6  2003/01/23 11:08:26  billhorsman
 new setConfiguratorListener method (and remove from optional
 parameter when registering pool)

 Revision 1.5  2003/01/17 00:38:12  billhorsman
 wide ranging changes to clarify use of alias and url -
 this has led to some signature changes (new exceptions
 thrown) on the ProxoolFacade API.

 Revision 1.4  2002/12/15 19:21:42  chr32
 Changed @linkplain to @link (to preserve JavaDoc for 1.2/1.3 users).

 Revision 1.3  2002/11/09 15:49:36  billhorsman
 add method to get the name of every pool

 Revision 1.2  2002/10/13 13:39:03  billhorsman
 fix when removing pools (credit to Dan Milstein)

 Revision 1.1.1.1  2002/09/13 08:13:04  billhorsman
 new

 Revision 1.8  2002/07/10 16:14:47  billhorsman
 widespread layout changes and move constants into ProxoolConstants

 Revision 1.7  2002/07/04 09:05:36  billhorsman
 Fixes

 Revision 1.6  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.5  2002/07/02 08:47:31  billhorsman
 you can now access a pool by alias or full url

 Revision 1.4  2002/06/28 11:19:47  billhorsman
 improved doc

*/

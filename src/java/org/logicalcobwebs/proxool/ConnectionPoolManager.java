/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

/**
 *
 * @version $Revision: 1.5 $, $Date: 2003/01/17 00:38:12 $
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
     * @param alias what the pool is called
     * @return the pool
     * @throws ProxoolException if it couldn't be found
     */
    protected ConnectionPool getConnectionPool(String alias) throws ProxoolException {
        ConnectionPool cp = (ConnectionPool) connectionPoolMap.get(alias);
        if (cp == null) {
            StringBuffer message = new StringBuffer("Couldn't find a pool called '" + alias + "'. Known pools are: ");
            Iterator i = connectionPoolMap.keySet().iterator();
            while (i.hasNext()) {
                message.append((String) i.next());
                message.append(i.hasNext() ? ", " : ".");
            }
            throw new ProxoolException(message.toString());
        }
        return cp;
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
    protected Collection getConnectionPoolMap() {
        return connectionPools;
    }

    protected ConnectionPool createConnectionPool(ConnectionPoolDefinition connectionPoolDefinition) {
        ConnectionPool connectionPool = new ConnectionPool(connectionPoolDefinition);
        connectionPools.add(connectionPool);
        connectionPoolMap.put(connectionPoolDefinition.getAlias(), connectionPool);
        return connectionPool;
    }

    protected void removeConnectionPool(String name) {
        ConnectionPool cp = (ConnectionPool) connectionPoolMap.get(name);
        if (cp != null) {
            connectionPoolMap.remove(cp.getDefinition().getAlias());
        } else {
            LOG.error("Ignored attempt to remove non-existent connection pool " + name);
        }
    }

    public String[] getConnectionPoolNames() {
        return (String[]) connectionPoolMap.keySet().toArray(new String[connectionPoolMap.size()]);
    }
}

/*
 Revision history:
 $Log: ConnectionPoolManager.java,v $
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

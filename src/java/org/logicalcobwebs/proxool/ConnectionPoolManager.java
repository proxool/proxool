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

/**
 *
 * @version $Revision: 1.3 $, $Date: 2002/11/09 15:49:36 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
class ConnectionPoolManager {

    private Map connectionPoolMap = new HashMap();

    private Set connectionPools = new HashSet();

    private static ConnectionPoolManager connectionPoolManager = null;

    private static final Log LOG = LogFactory.getLog(ProxoolFacade.class);

    public static ConnectionPoolManager getInstance() {
        if (connectionPoolManager == null) {
            synchronized (ConnectionPoolManager.class) {
                if (connectionPoolManager == null) {
                    connectionPoolManager = new ConnectionPoolManager();
                }
            }
        }
        return connectionPoolManager;
    }

    private ConnectionPoolManager() {
    }

    protected ConnectionPool getConnectionPool(String alias) {
        ConnectionPool cp = null;
        if (alias == null) {
            throw new RuntimeException(("You can't ask for a Connection without defining the alias"));
        }
        cp = (ConnectionPool) connectionPoolMap.get(alias);
        return cp;
    }

    /** @return an array of the connection pools */
    protected Collection getConnectionPoolMap() {
        return connectionPools;
    }

    protected ConnectionPool createConnectionPool(ConnectionPoolDefinition connectionPoolDefinition) {
        ConnectionPool connectionPool = new ConnectionPool(connectionPoolDefinition);
        connectionPools.add(connectionPool);
        connectionPoolMap.put("proxool." + connectionPoolDefinition.getName(), connectionPool);
        connectionPoolMap.put(connectionPoolDefinition.getName(), connectionPool);
        connectionPoolMap.put(connectionPoolDefinition.getCompleteUrl(), connectionPool);
        return connectionPool;
    }

    protected void removeConnectionPool(String name) {
        ConnectionPool cp = (ConnectionPool) connectionPoolMap.get(name);
        if (cp != null) {
            connectionPoolMap.remove("proxool." + cp.getDefinition().getName());
            connectionPoolMap.remove(cp.getDefinition().getName());
            connectionPoolMap.remove(cp.getDefinition().getCompleteUrl());
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

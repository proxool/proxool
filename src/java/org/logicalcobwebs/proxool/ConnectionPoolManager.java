/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @version $Revision: 1.1 $, $Date: 2002/09/13 08:13:04 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
class ConnectionPoolManager {

    private Map connectionPoolMap = new HashMap();

    private Set connectionPools = new HashSet();

    private static ConnectionPoolManager connectionPoolManager = null;

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
        connectionPoolMap.remove(name);
    }
}

/*
 Revision history:
 $Log: ConnectionPoolManager.java,v $
 Revision 1.1  2002/09/13 08:13:04  billhorsman
 Initial revision

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

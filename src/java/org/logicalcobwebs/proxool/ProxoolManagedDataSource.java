/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A DataSource implementation maintained solely by Proxool. Proxool will instatiate this datasource and bind it
 * to JNDI. Only use this DataSource implemenation if your environment does not provide DataSource management.
 * @version $Revision: 1.1 $, $Date: 2004/03/15 02:47:02 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: chr32 $ (current maintainer)
 * @since Proxool 0.9
 */
public class ProxoolManagedDataSource extends AbstractProxoolDataSource {
    private String alias;

    public ProxoolManagedDataSource (String alias) {
        this.alias = alias;
    }

    public Connection getConnection() throws SQLException {
        try {
            return ConnectionPoolManager.getInstance().getConnectionPool(this.alias).getConnection();
        } catch (ProxoolException e) {
            final Throwable cause = e.getCause();
            if (cause != null && cause instanceof SQLException) {
                throw (SQLException) cause;
            } else {
                throw new SQLException(e.getMessage());
            }
        }
    }
}
/*
 Revision history:
 $Log: ProxoolManagedDataSource.java,v $
 Revision 1.1  2004/03/15 02:47:02  chr32
 Added initial DataSource support.

*/
/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Contains most of the functionality that we require to manipilate the
 * connection. The subclass of this defines how we delegate to the
 * real connection.
 *
 * @version $Revision: 1.2 $, $Date: 2003/01/31 16:53:13 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public abstract class AbstractDatabaseMetaData {

    private static final Log LOG = LogFactory.getLog(AbstractDatabaseMetaData.class);

    private DatabaseMetaData databaseMetaData;

    private ProxyConnectionIF proxyConnection;

    /**
     * Whether we have invoked a method that requires us to reset
     */
    private boolean needToReset = false;

    protected AbstractDatabaseMetaData(Connection connection, ProxyConnectionIF proxyConnection) throws SQLException {
        databaseMetaData = connection.getMetaData();
        this.proxyConnection = proxyConnection;
    }

    /**
     * Whether the underlying databaseMetaData are equal
     * @param obj the object (probably another databaseMetaData) that we
     * are being compared to
     * @return whether they are the same
     */
    public boolean equals(Object obj) {
        return databaseMetaData.hashCode() == obj.hashCode();
    }

    /**
     * We don't want to ask the DatabaseMetaData object for the
     * connection or we will get the delegate instead of the Proxool
     * one.
     * @see DatabaseMetaData#getConnection
     */
    public Connection getConnection() {
        return ProxyFactory.getConnection(proxyConnection);
    }

    /**
     * Get the DatabaseMetaData from the connection
     * @return databaseMetaData
     */
    protected DatabaseMetaData getDatabaseMetaData() {
        return databaseMetaData;
    }

    /**
     * @see Object#toString
     */
    public String toString() {
        return databaseMetaData.toString();
    }

}


/*
 Revision history:
 $Log: AbstractDatabaseMetaData.java,v $
 Revision 1.2  2003/01/31 16:53:13  billhorsman
 checkstyle

 Revision 1.1  2003/01/31 14:33:11  billhorsman
 fix for DatabaseMetaData

 Revision 1.3  2003/01/31 11:38:57  billhorsman
 birthDate now stored as Date not long

 Revision 1.2  2003/01/28 11:50:35  billhorsman
 more verbose debug

 Revision 1.1  2003/01/27 18:26:33  billhorsman
 refactoring of ProxyConnection and ProxyStatement to
 make it easier to write JDK 1.2 patch

 */
/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.dbscript.ConnectionAdapterIF;

import java.util.Properties;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * <link rel="stylesheet" href="{@docRoot}/cg.css" type="text/css">
 *
 * Provides Proxool connections to the {@link org.logicalcobwebs.dbscript.ScriptFacade ScriptFacade}
 *
 * @version $Revision: 1.5 $, $Date: 2002/11/09 14:45:35 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public class ProxoolAdapter implements ConnectionAdapterIF {

    private String alias = String.valueOf(hashCode());

    /**
     * Use this constructor if you want to define the alias
     * @param alias the alias of the pool
     */
    public ProxoolAdapter(String alias) {
        this.alias = alias;
    }

    /**
     * Default constructor. Will use the hashCode as the alias for the pool
     */
    public ProxoolAdapter() {
    }

    public String getName() {
        return "proxool";
    }

    public void setup(String driver, String url, Properties info) throws SQLException {

        try {
            Class.forName(ProxoolDriver.class.getName());
        } catch (ClassNotFoundException e) {
            throw new SQLException("Couldn't find " + driver);
        }

        String fullUrl = TestHelper.buildProxoolUrl(alias, driver, url);
        ProxoolFacade.registerConnectionPool(fullUrl, info);
    }

    public Connection getConnection()
            throws SQLException {
        return DriverManager.getConnection(ProxoolConstants.PROXOOL
            + ProxoolConstants.ALIAS_DELIMITER + alias);
    }

    public void closeConnection(Connection connection) throws SQLException {
        if (connection != null){
            connection.close();
        }
    }

    public void teardown() throws SQLException {
        ProxoolFacade.removeConnectionPool(alias);
    }

}

/*
 Revision history:
 $Log: ProxoolAdapter.java,v $
 Revision 1.5  2002/11/09 14:45:35  billhorsman
 only close connection if it is open

 Revision 1.4  2002/11/07 18:56:59  billhorsman
 allow explicit definition of alias

 Revision 1.3  2002/11/02 14:22:16  billhorsman
 Documentation

 Revision 1.2  2002/11/02 12:46:42  billhorsman
 improved debug

 Revision 1.1  2002/11/02 11:37:48  billhorsman
 New tests

*/

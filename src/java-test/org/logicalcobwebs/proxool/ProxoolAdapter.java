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
 * TODO
 *
 * @version $Revision: 1.1 $, $Date: 2002/11/02 11:37:48 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since GSI 5.0
 */
public class ProxoolAdapter implements ConnectionAdapterIF {

    private String alias = String.valueOf(hashCode());

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
        connection.close();
    }

    public void teardown() throws SQLException {
        ProxoolFacade.removeConnectionPool(alias);
    }

}

/*
 Revision history:
 $Log: ProxoolAdapter.java,v $
 Revision 1.1  2002/11/02 11:37:48  billhorsman
 New tests

*/

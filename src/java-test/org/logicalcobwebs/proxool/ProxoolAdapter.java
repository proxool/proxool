/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.dbscript.ConnectionAdapterIF;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Provides Proxool connections to the {@link org.logicalcobwebs.dbscript.ScriptFacade ScriptFacade}
 *
 * @version $Revision: 1.9 $, $Date: 2002/12/04 13:20:10 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public class ProxoolAdapter implements ConnectionAdapterIF, ConfiguratorIF {

    private static final Log LOG = LogFactory.getLog(ProxoolAdapter.class);

    private String alias = String.valueOf(hashCode());

    private String fullUrl;

    private ConnectionPoolDefinitionIF connectionPoolDefinition;

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

    public void defintionUpdated(ConnectionPoolDefinitionIF connectionPoolDefinition) {
        setConnectionPoolDefinition(connectionPoolDefinition);
    }

    public ConnectionPoolDefinitionIF getConnectionPoolDefinition() {
        return connectionPoolDefinition;
    }

    public void setConnectionPoolDefinition(ConnectionPoolDefinitionIF connectionPoolDefinition) {
        this.connectionPoolDefinition = connectionPoolDefinition;
        LOG.debug("Setting cpd to " + this.connectionPoolDefinition);
    }

    public String getName() {
        return "proxool";
    }

    public void update(Properties info) throws SQLException {
        ProxoolFacade.updateConnectionPool(alias, info);
    }

    public void setup(String driver, String url, Properties info) throws SQLException {

        try {
            Class.forName(ProxoolDriver.class.getName());
        } catch (ClassNotFoundException e) {
            throw new SQLException("Couldn't find " + driver);
        }

        fullUrl = TestHelper.buildProxoolUrl(alias, driver, url);
        ProxoolFacade.registerConnectionPool(fullUrl, info, this);
    }

    public Connection getConnection()
            throws SQLException {
        return DriverManager.getConnection(ProxoolConstants.PROXOOL
                + ProxoolConstants.ALIAS_DELIMITER + alias);
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public void closeConnection(Connection connection) throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public void tearDown() {
        ProxoolFacade.removeConnectionPool(alias);
    }

}

/*
 Revision history:
 $Log: ProxoolAdapter.java,v $
 Revision 1.9  2002/12/04 13:20:10  billhorsman
 ConfiguratorIF test

 Revision 1.8  2002/11/13 20:23:38  billhorsman
 change method name, throw exceptions differently, trivial changes

 Revision 1.7  2002/11/09 16:09:06  billhorsman
 checkstyle

 Revision 1.6  2002/11/09 16:02:05  billhorsman
 fix doc

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

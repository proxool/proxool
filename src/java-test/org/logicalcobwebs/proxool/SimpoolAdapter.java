/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.dbscript.ConnectionAdapterIF;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * <link rel="stylesheet" href="{@docRoot}/cg.css" type="text/css">
 *
 * This is the simplest pool you can get. It isnæt thread safe. It isn't robust.
 * But it is fast. We use it as our bench mark on how could we should strive
 * to be.
 *
 * Provides Simpool connections to the {@link org.logicalcobwebs.dbscript.ScriptFacade ScriptFacade}
 *
 * @version $Revision: 1.3 $, $Date: 2002/11/02 14:22:16 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public class SimpoolAdapter implements ConnectionAdapterIF {

    private static final Log LOG = LogFactory.getLog(SimpoolAdapter.class);

    private Connection[] connections;

    private int index = 0;

    public String getName() {
        return "simpool";
    }

    public void setup(String driver, String url, Properties info) throws SQLException {

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Couldn't find " + driver);
        }

        int connectionCount = Integer.parseInt(info.getProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY));
        connections = new Connection[connectionCount];
        for (int i = 0; i < connectionCount; i++) {
            connections[i] = DriverManager.getConnection(url, info);
        }
    }

    public Connection getConnection()
            throws SQLException {
        Connection c = connections[index];
        index++;
        if (index >= connections.length) {
            index = 0;
        }
        return c;
    }

    public void closeConnection(Connection connection) {
        // Do nothing !
    }

    public void teardown() throws SQLException {
        for (int i = 0; i < connections.length; i++) {
            connections[i].close();
        }
    }

}

/*
 Revision history:
 $Log: SimpoolAdapter.java,v $
 Revision 1.3  2002/11/02 14:22:16  billhorsman
 Documentation

 Revision 1.2  2002/11/02 12:46:42  billhorsman
 improved debug

 Revision 1.1  2002/11/02 11:37:48  billhorsman
 New tests

 Revision 1.1  2002/10/30 21:17:50  billhorsman
 new performance tests

*/

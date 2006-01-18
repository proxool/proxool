/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.dbscript.ConnectionAdapterIF;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * This is the simplest pool you can get. It isn\ufffdt thread safe. It isn't robust.
 * But it is fast. We use it as our bench mark on how could we should strive
 * to be.
 *
 * Provides Simpool connections to the {@link org.logicalcobwebs.dbscript.ScriptFacade ScriptFacade}
 *
 * @version $Revision: 1.12 $, $Date: 2006/01/18 14:40:06 $
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

    public void tearDown() {
        try {
            for (int i = 0; i < connections.length; i++) {
                connections[i].close();
            }
        } catch (SQLException e) {
            LOG.error("Problem tearing down " + getName() + " adapter", e);
        }
    }

}

/*
 Revision history:
 $Log: SimpoolAdapter.java,v $
 Revision 1.12  2006/01/18 14:40:06  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.11  2003/03/04 10:24:40  billhorsman
 removed try blocks around each test

 Revision 1.10  2003/03/03 11:12:05  billhorsman
 fixed licence

 Revision 1.9  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.8  2003/02/19 15:14:25  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.7  2003/02/06 17:41:03  billhorsman
 now uses imported logging

 Revision 1.6  2003/01/27 23:32:10  billhorsman
 encoding fix (no idea how that happened)

 Revision 1.5  2002/11/13 20:23:38  billhorsman
 change method name, throw exceptions differently, trivial changes

 Revision 1.4  2002/11/09 16:02:20  billhorsman
 fix doc

 Revision 1.3  2002/11/02 14:22:16  billhorsman
 Documentation

 Revision 1.2  2002/11/02 12:46:42  billhorsman
 improved debug

 Revision 1.1  2002/11/02 11:37:48  billhorsman
 New tests

 Revision 1.1  2002/10/30 21:17:50  billhorsman
 new performance tests

*/

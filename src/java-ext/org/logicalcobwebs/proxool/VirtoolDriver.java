/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

/**
 * This class acts as a virtual pool. When you ask it for a connection it
 * delegates to one of the designated real pools. Some assumptions:
 *
 * Getting a connection needs to be very fast.
 *
 * Switching pools can be relatively slow (but just to get that in perspective,
 * > 100ms)
 *
 * We should detect pools that don't respond (timeout), throw certain
 * SQLExceptions, or are unacceptably slow.
 *
 * We should also allow simple load balancing between pools that are
 * up.
 *
 * @version $Revision: 1.3 $, $Date: 2002/11/12 20:19:18 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public class VirtoolDriver implements Driver {

    private static final String VIRTOOL = "virtool";

    private String[] activePools;

    private int nextPool;

    public Connection connect(String url, Properties info)
            throws SQLException {
        String alias = activePools[nextPool];

        // Now we need to move to the next pool. This code isn't ThreadSafe and
        // we don't want to make it so because it would have a performance
        // impact.

        return null;
    }

    public boolean acceptsURL(String url) throws SQLException {
        return (url.startsWith(VIRTOOL));
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
            throws SQLException {
        return new DriverPropertyInfo[0];
    }

    public int getMajorVersion() {
        throw new UnsupportedOperationException("This virtual driver doesn't support this operation.");
    }

    public int getMinorVersion() {
        throw new UnsupportedOperationException("This virtual driver doesn't support this operation.");
    }

    public boolean jdbcCompliant() {
        throw new UnsupportedOperationException("This virtual driver doesn't support this operation.");
    }

    static {
        try {
            DriverManager.registerDriver(new VirtoolDriver());
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }

}

/*
 Revision history:
 $Log: VirtoolDriver.java,v $
 Revision 1.3  2002/11/12 20:19:18  billhorsman
 added some doc

 Revision 1.2  2002/10/27 13:05:01  billhorsman
 checkstyle

 Revision 1.1  2002/10/27 12:05:39  billhorsman
 early, early draft

*/

/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.util.Properties;

/**
 * <link rel="stylesheet" href="{@docRoot}/cg.css" type="text/css">
 *
 * This is the simplest pool you can get. It isnæt thread safe. It isn't robust.
 * But it is fast. We use it as our bench mark on how could we should strive
 * to be.
 *
 * @version $Revision: 1.1 $, $Date: 2002/10/30 21:17:50 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since GSI 5.0
 */
public class SimpoolDriver implements Driver {

    private static final Log LOG = LogFactory.getLog(SimpoolDriver.class);

    private Connection[] connections;

    private int index = 0;

    private boolean initialised;

    protected static final String PREFIX = "simpool.";

    static {
        try {
            DriverManager.registerDriver(new SimpoolDriver());
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }

    public Connection connect(String url, Properties info)
            throws SQLException {
        initialise(url, info);
        Connection c = connections[index];
        index++;
        if (index >= connections.length) {
            index = 0;
        }
        return c;
    }

    public boolean acceptsURL(String url) throws SQLException {
        return url.startsWith(PREFIX);
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
            throws SQLException {
        return new DriverPropertyInfo[0];
    }

    public int getMajorVersion() {
        return 1;
    }

    public int getMinorVersion() {
        return 0;
    }

    public boolean jdbcCompliant() {
        return true;
    }

    protected void initialise(String url, Properties info) throws SQLException {
        if (!initialised) {
            synchronized (this) {
                if (!initialised) {

                    int threads = Integer.parseInt(info.getProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY));

                    int pos1 = url.indexOf(':');
                    int pos2 = url.indexOf(':', pos1 + 1);
                    String driver = url.substring(pos1 + 1, pos2);
                    String delegateUrl = url.substring(pos2 + 1);
                    try {
                        Class.forName(driver);
                    } catch (ClassNotFoundException e) {
                        throw new SQLException("Couldn't find " + driver);
                    }

                    connections = new Connection[threads];
                    for (int i = 0; i < connections.length; i++) {
                        LOG.debug("Creating simpool connection #" + i);
                        connections[i] = DriverManager.getConnection(delegateUrl, info);
                    }

                    initialised = true;
                }
            }
        }
    }

    protected void destroy() throws SQLException {
        for (int i = 0; i < connections.length; i++) {
            connections[i].close();
        }
    }

}

/*
 Revision history:
 $Log: SimpoolDriver.java,v $
 Revision 1.1  2002/10/30 21:17:50  billhorsman
 new performance tests

*/

/**
 * Clever Little Trader
 *
 * Jubilee Group and Logical Cobwebs, 2002 - 2003
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * TODO
 * @version $Revision: 1.1 $, $Date: 2003/07/23 06:54:48 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since CLT 0.2
 */
public class ProxoolDataSource implements DataSource {

    private static final Log LOG = LogFactory.getLog(ProxoolDataSource.class);

    private String alias;

    public ProxoolDataSource(String alias) {
        this.alias = alias;
    }

    public Connection getConnection() throws SQLException {
        ConnectionPool cp = null;
        try {
            cp = ConnectionPoolManager.getInstance().getConnectionPool(alias);
            return cp.getConnection();

        } catch (ProxoolException e) {
            LOG.error("Problem getting connection", e);
            throw new SQLException(e.toString());
        }
    }

    public Connection getConnection(String username, String password)
            throws SQLException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("Jakarta Commons' Logging used available");
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException("Jakarta Commons' Logging used available");
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("Jakarta Commons' Logging used available");
    }

    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException("Jakarta Commons' Logging used available");
    }
}

/*
  Revision History
  $Log: ProxoolDataSource.java,v $
  Revision 1.1  2003/07/23 06:54:48  billhorsman
  draft JNDI changes (shouldn't effect normal operation)

 */

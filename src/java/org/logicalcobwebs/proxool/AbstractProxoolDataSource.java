/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Connection;

/**
 * Base class for all Proxool DataSource implementations.
 * @version $Revision: 1.1 $, $Date: 2004/03/15 02:47:02 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: chr32 $ (current maintainer)
 * @since Proxool 0.9
 */
public abstract class AbstractProxoolDataSource implements DataSource {
    private int loginTimeout;
    private PrintWriter logWriter;

    public PrintWriter getLogWriter() throws SQLException {
        return this.logWriter;
    }

    public int getLoginTimeout() throws SQLException {
        return this.loginTimeout;
    }

    public void setLogWriter(PrintWriter logWriter) throws SQLException {
        this.logWriter = logWriter;
    }

    public void setLoginTimeout(int loginTimeout) throws SQLException {
        this.loginTimeout = loginTimeout;
    }

    public Connection getConnection(String s, String s1) throws SQLException {
        throw new UnsupportedOperationException("Proxool datasources does not support setting username and"
            + "password at runtime.");
    }
}
/*
 Revision history:
 $Log: AbstractProxoolDataSource.java,v $
 Revision 1.1  2004/03/15 02:47:02  chr32
 Added initial DataSource support.

*/
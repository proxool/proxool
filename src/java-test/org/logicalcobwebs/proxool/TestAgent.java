/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * <link rel="stylesheet" href="{@docRoot}/cg.css" type="text/css">
 *
 * TODO
 *
 * @version $Revision: 1.1 $, $Date: 2002/10/30 21:17:51 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since GSI 5.0
 */
class TestAgent {

    private static final Log LOG = LogFactory.getLog(TestHelper.class);

    private String url;

    private Properties info;

    private int loops;

    public TestAgent(String driverName, String url, Properties info, int loops) throws ClassNotFoundException {
        Class.forName(driverName);
        this.url = url;
        this.info = info;
        this.loops = loops;
    }

    protected void setup(boolean ignoreClose) throws SQLException {

        // Asking for a connection will trigger the pool to startup and
        // make all the necessary connections
        try {
            Connection triggerToStart = DriverManager.getConnection(url, info);
            if (!ignoreClose) {
                triggerToStart.close();
            }
        } catch (SQLException e) {
            LOG.error("Problem setting up " + url, e);
            throw e;
        }

        LOG.debug("Sleeping whilst pool starts up");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            LOG.debug("Awoken from sleep");
        }

    }

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    protected void test(String testSql, boolean ignoreClose) throws SQLException {
        for (int i = 0; i < loops; i++) {
            for (int j = 0; j < loops; j++) {
                try {
                    execute(testSql, ignoreClose);
                } catch (SQLException e) {
                    LOG.error("TestAgent caused an error performing '" + testSql + "'", e);
                }
            }
        }
    }

    protected void execute(String sql) throws SQLException {
        execute(sql, false);
    }

    protected void execute(String sql, boolean ignoreClose) throws SQLException {
        Connection c = null;
        Statement s = null;
        try {
            c = getConnection();
            s = c.createStatement();
            s.execute(sql);
        } finally {
            if (s != null) {
                s.close();
            }
            if (c != null && !ignoreClose) {
                c.close();
            }
        }
    }

}

/*
 Revision history:
 $Log: TestAgent.java,v $
 Revision 1.1  2002/10/30 21:17:51  billhorsman
 new performance tests

*/

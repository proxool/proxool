/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.dbscript;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;

/**
 * <link rel="stylesheet" href="{@docRoot}/cg.css" type="text/css">
 *
 * TODO
 *
 * @version $Revision: 1.2 $, $Date: 2002/11/02 12:45:54 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since GSI 5.0
 */
class ScriptRunner {

    private static final Log LOG = LogFactory.getLog(ScriptRunner.class);

    protected static void runScript(Script script, ConnectionAdapterIF adapter) throws SQLException {
        adapter.setup(script.getDriver(), script.getUrl(), script.getInfo());

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }

        Command[] commands = script.getCommands();
        for (int i = 0; i < commands.length; i++) {
            Command command = commands[i];
            long start = System.currentTimeMillis();

            for (int loop = 0; loop < command.getLoops(); loop++) {

                // Open some connections
                Connection[] connections = new Connection[command.getLoad()];
                for (int load = 0; load < command.getLoad(); load++) {
                    connections[load] = adapter.getConnection();
                }

                // Execute the SQL
                for (int load = 0; load < command.getLoad(); load++) {
                    try {
                        execute(connections[load], command.getSql());
                    } catch (SQLException e) {
                        if (command.isIgnoreException()) {
                            LOG.debug("Ignoring exception in " + command.getName(), e);
                        } else {
                            throw e;
                        }
                    }
                }

                // Close the connections again
                for (int load = 0; load < command.getLoad(); load++) {
                    adapter.closeConnection(connections[load]);
                }

            }

            long elapsed = System.currentTimeMillis() - start;
            int count = command.getLoad() * command.getLoops();
            double lap = (double)elapsed / (double)count;
            if (count > 1) {
                LOG.info(adapter.getName()+ ":" + command.getName() + " ran " + count + " commands in " + elapsed + " milliseconds (avg." + lap + ")");
            } else {
                LOG.debug(adapter.getName() + ":" + command.getName() + " ran in " + elapsed + " milliseconds");
            }

        }
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute(sql);
        } finally {
            if (statement != null) {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                } catch (SQLException e) {
                    LOG.error("Couldn't close statement", e);
                }
            }
        }
    }

}

/*
 Revision history:
 $Log: ScriptRunner.java,v $
 Revision 1.2  2002/11/02 12:45:54  billhorsman
 improved debug

 Revision 1.1  2002/11/02 11:29:53  billhorsman
 new script runner for testing

*/

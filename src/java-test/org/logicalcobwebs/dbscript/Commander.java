/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.dbscript;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * <link rel="stylesheet" href="{@docRoot}/cg.css" type="text/css">
 *
 * A thread that can run a single command many times.
 *
 * @version $Revision: 1.1 $, $Date: 2002/11/09 14:45:07 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since GSI 5.0
 */
class Commander implements Runnable {

    private static final Log LOG = LogFactory.getLog(Commander.class);

    private ConnectionAdapterIF adapter;

    private CommandIF command;

    private CommandFilterIF commandFilter;

    private boolean finished = false;

    public Commander(ConnectionAdapterIF adapter, CommandIF commandIF, CommandFilterIF commandFilter) {
        this.adapter = adapter;
        this.command = commandIF;
        this.commandFilter = commandFilter;
    }

    public void run() {

        try {
            boolean keepGoing = true;
            for (int i = 0; i < command.getLoops(); i++) {

                Connection connection = null;

                try {
                    connection = adapter.getConnection();
                    boolean executeCommand = true;
                    if (commandFilter != null) {
                            executeCommand = commandFilter.beforeCommand(connection, command);
                    }

                    if (executeCommand) {
                        execute(connection, command.getSql());
                    }

                    if (commandFilter != null) {
                            commandFilter.afterCommand(connection, command);
                    }

                } catch (SQLException e) {
                    if (!commandFilter.catchException(command, e)) {
                        keepGoing = false;
                    }
                    if (command.isIgnoreException()) {
                        // Silent
                    } else if (command.isLogException()) {
                        LOG.debug("Ignoring exception in " + command.getName(), e);
                    } else {
                        LOG.error("Stopping command " + command.getName(), e);
                        keepGoing = false;
                    }
                } finally {
                    try {
                        adapter.closeConnection(connection);
                    } catch (SQLException e) {
                        LOG.error("Closing connection for " + Thread.currentThread().getName(), e);
                    }
                }

                if (!keepGoing) {
                    break;
                }

                // Give other threads a chance. (Hoping this will increase the chances
                // of some sort of conflict)
                Thread.yield();
            }
        } finally {
            finished = true;
        }

    }

    /**
     * Is the thread running
     * @return true if it's finished, else false
     */
    protected boolean isFinished() {
        return finished;
    }

    /**
     * Execute and SQL statement
     * @param connection used to execute statement
     * @param sql the SQL to perform
     * @throws java.sql.SQLException if anything goes wrong
     */
    private static final void execute(Connection connection, String sql) throws SQLException {
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
 $Log: Commander.java,v $
 Revision 1.1  2002/11/09 14:45:07  billhorsman
 now threaded and better exception handling

*/

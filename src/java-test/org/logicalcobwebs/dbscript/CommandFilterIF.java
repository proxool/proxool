/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.dbscript;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * <link rel="stylesheet" href="{@docRoot}/cg.css" type="text/css">
 *
 * TODO
 *
 * @version $Revision: 1.1 $, $Date: 2002/11/06 21:07:42 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since GSI 5.0
 */
public interface CommandFilterIF {

    /**
     * Implement this if you want to do something special before each command is run.
     * @param connection the connection being used
     * @param command the command about to be run
     * @return true if the command should be executed, or false to skip the command
     * @throws SQLException if anything goes wrong. This will terminate the script.
     */
    boolean beforeCommand(Connection connection, CommandIF command) throws SQLException;

    /**
     * Implement this if you want to do something special after each command is run.
     * @param connection the connection being used
     * @param command the command that was run
     * @throws SQLException if anything goes wrong. This will terminate the script.
     */
    void afterCommand(Connection connection, CommandIF command) throws SQLException;

}

/*
 Revision history:
 $Log: CommandFilterIF.java,v $
 Revision 1.1  2002/11/06 21:07:42  billhorsman
 New interfaces to allow filtering of commands

*/

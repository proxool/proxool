/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.dbscript;

/**
 * An SQL command to run.
 *
 * @version $Revision: 1.4 $, $Date: 2003/02/19 15:14:19 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public interface CommandIF {

    /**
     * The SQL statement to run
     * @return sql
     */
    String getSql();

    /**
     * How many "threads" to simulate. See {@link org.logicalcobwebs.dbscript.Script} to see how
     * it actually implements thread-like behaviour.
     * @return load
     */
    int getLoad();

    /**
     * The number of loops to perform. Each loop will run the {@link #getSql sql}
     * {@link #getLoad load} times.
     * @return loops
     */
    int getLoops();

    /**
     * If true then errors that occur during this command are ignored silently
     * and do not stop the {@link org.logicalcobwebs.dbscript.Script script} running.
     * @return true if exceptions should be ignored
     */
    boolean isIgnoreException();

    /**
     * If true then errors that occur during this command are logged as debug
     * messages but do not stop the {@link org.logicalcobwebs.dbscript.Script script} running.
     * @return true if exceptions should be logged
     */
    boolean isLogException();

    /**
     * A convenient name to call this command to help logging.
     * @return name
     */
    String getName();

}

/*
 Revision history:
 $Log: CommandIF.java,v $
 Revision 1.4  2003/02/19 15:14:19  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.3  2002/11/09 15:58:54  billhorsman
 fix and added doc

 Revision 1.2  2002/11/09 14:45:07  billhorsman
 now threaded and better exception handling

 Revision 1.1  2002/11/06 21:07:42  billhorsman
 New interfaces to allow filtering of commands

*/

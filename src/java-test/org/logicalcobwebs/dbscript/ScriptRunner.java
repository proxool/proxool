/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.dbscript;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.logicalcobwebs.proxool.ProxoolException;

import java.sql.SQLException;

/**
 * Run a {@link Script script}.
 *
 * @version $Revision: 1.10 $, $Date: 2003/02/19 15:14:21 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public class ScriptRunner {

    private static final Log LOG = LogFactory.getLog(ScriptRunner.class);

    /**
     * Run the script.
     *
     * @param script to run
     * @param adapter so we know where to connections from
     * @throws SQLException if anything goes wrong
     */
    protected static final void runScript(Script script, ConnectionAdapterIF adapter, CommandFilterIF commandFilter) throws SQLException, ProxoolException {
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

            // Execute the SQL
            Commander[] commanders = new Commander[command.getLoad()];
            for (int load = 0; load < command.getLoad(); load++) {
                commanders[load] = new Commander(adapter, command, commandFilter);
                Thread t = new Thread(commanders[load]);
                t.setName(script.getName() + "." + command.getName() + "." + load);
                t.start();
            }

            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOG.error("Awoken from sleep", e);
                }

                int remaining = command.getLoad();
                for (int load = 0; load < command.getLoad(); load++) {
                    if (commanders[load].isFinished()) {
                        remaining--;
                    }
                }

                if (remaining > 0) {
                    // LOG.debug("Waiting for " + remaining + " threads to complete.");
                } else {
                    break;
                }
            }

            long elapsed = System.currentTimeMillis() - start;
            int count = command.getLoad() * command.getLoops();
            double lap = (double) elapsed / (double) count;
            if (count > 1) {
                LOG.info(adapter.getName() + ":" + command.getName() + " ran " + count + " commands in " + elapsed + " milliseconds (avg." + lap + ")");
            } else {
                LOG.debug(adapter.getName() + ":" + command.getName() + " ran in " + elapsed + " milliseconds");
            }

        }
    }

}

/*
 Revision history:
 $Log: ScriptRunner.java,v $
 Revision 1.10  2003/02/19 15:14:21  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.9  2003/02/06 17:41:02  billhorsman
 now uses imported logging

 Revision 1.8  2003/01/17 00:38:12  billhorsman
 wide ranging changes to clarify use of alias and url -
 this has led to some signature changes (new exceptions
 thrown) on the ProxoolFacade API.

 Revision 1.7  2002/11/09 16:00:34  billhorsman
 fix doc

 Revision 1.6  2002/11/09 14:45:07  billhorsman
 now threaded and better exception handling

 Revision 1.5  2002/11/06 21:07:14  billhorsman
 Support for CommandFilterIF

 Revision 1.4  2002/11/02 14:22:16  billhorsman
 Documentation

 Revision 1.3  2002/11/02 13:57:34  billhorsman
 checkstyle

 Revision 1.2  2002/11/02 12:45:54  billhorsman
 improved debug

 Revision 1.1  2002/11/02 11:29:53  billhorsman
 new script runner for testing

*/

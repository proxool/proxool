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

/**
 * <link rel="stylesheet" href="{@docRoot}/cg.css" type="text/css">
 *
 * Run a {@link Script script}.
 *
 * @version $Revision: 1.6 $, $Date: 2002/11/09 14:45:07 $
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
    protected static final void runScript(Script script, ConnectionAdapterIF adapter, CommandFilterIF commandFilter) throws SQLException {
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

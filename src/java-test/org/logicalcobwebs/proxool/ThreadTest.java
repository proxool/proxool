/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.dbscript.CommandFilterIF;
import org.logicalcobwebs.dbscript.CommandIF;
import org.logicalcobwebs.dbscript.ScriptFacade;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

/**
 * Test how well Proxool works in a threaded test.
 *
 * @version $Revision: 1.9 $, $Date: 2003/03/04 10:24:40 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public class ThreadTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(ThreadTest.class);

    private int exceptionCount;

    private Random random = new Random();

    public ThreadTest(String s) {
        super(s);
    }

    /**
     * Test whether autoCommit is correctly reset when a connection is
     * returned to the pool.
     */
    public void testThreads() {
        String scriptLocation = System.getProperty("script");
        if (scriptLocation != null) {
            ScriptFacade.runScript(scriptLocation, new ProxoolAdapter(), new CommandFilterIF() {

                public boolean beforeCommand(Connection connection, CommandIF command) throws SQLException {
                    // Nothing to do
                    return true;
                }

                public void afterCommand(Connection connection, CommandIF command) throws SQLException {
                    try {
                        // Occassionally, sleep for a long time.
                        int sleep = 40;
                        if (random.nextInt(100) == 1) {
                            sleep = 10000;
                            LOG.debug(Thread.currentThread().getName() + " is sleeping for " + sleep + " ms");
                        }
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        LOG.debug("Awoken from sleep");
                    }
                }

                public boolean catchException(CommandIF command, SQLException e) {
                    incrementExceptionCount();
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException ee) {
                        LOG.debug("Awoken from sleep");
                    }
                    return true;
                }

            });

            LOG.info("Encountered " + getExceptionCount() + " exceptions during execution");

        } else {
            LOG.info("Skipping autoCommit test because 'script' System Property was not set");
        }
    }

    public int getExceptionCount() {
        return exceptionCount;
    }

    public void incrementExceptionCount() {
        this.exceptionCount++;
    }
}

/*
 Revision history:
 $Log: ThreadTest.java,v $
 Revision 1.9  2003/03/04 10:24:40  billhorsman
 removed try blocks around each test

 Revision 1.8  2003/03/03 17:09:06  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.7  2003/03/03 11:12:05  billhorsman
 fixed licence

 Revision 1.6  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.5  2003/02/19 15:14:26  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.4  2003/02/06 17:41:03  billhorsman
 now uses imported logging

 Revision 1.3  2002/12/16 17:04:37  billhorsman
 new test structure

 Revision 1.2  2002/11/09 16:09:06  billhorsman
 checkstyle

 Revision 1.1  2002/11/09 15:45:29  billhorsman
 new thread test, using thread.xml script

 Revision 1.4  2002/11/02 14:22:16  billhorsman
 Documentation

 Revision 1.3  2002/11/02 13:57:34  billhorsman
 checkstyle

 Revision 1.2  2002/11/02 11:37:48  billhorsman
 New tests

 Revision 1.1  2002/10/30 21:17:51  billhorsman
 new performance tests

*/

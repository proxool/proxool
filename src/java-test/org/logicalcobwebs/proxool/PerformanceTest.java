/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.dbscript.ScriptFacade;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Tests how fast Proxool is compared to the "perfect" pool, {@link SimpoolAdapter}.
 *
 * @version $Revision: 1.12 $, $Date: 2003/03/04 10:24:40 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public class PerformanceTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(PerformanceTest.class);

    public PerformanceTest(String s) {
        super(s);
    }

    public void testScript() throws ParserConfigurationException, SAXException, IOException, SQLException {
        String scriptLocation = System.getProperty("script");
        if (scriptLocation != null) {
            ScriptFacade.runScript(scriptLocation, new ProxoolAdapter());
            ScriptFacade.runScript(scriptLocation, new SimpoolAdapter());
        } else {
            LOG.info("Skipping performance test because 'script' System Property was not set");
        }
    }

}

/*
 Revision history:
 $Log: PerformanceTest.java,v $
 Revision 1.12  2003/03/04 10:24:40  billhorsman
 removed try blocks around each test

 Revision 1.11  2003/03/03 17:08:57  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.10  2003/03/03 11:12:04  billhorsman
 fixed licence

 Revision 1.9  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.8  2003/02/19 15:14:23  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.7  2003/02/06 17:41:03  billhorsman
 now uses imported logging

 Revision 1.6  2002/12/16 17:05:05  billhorsman
 new test structure

 Revision 1.5  2002/11/09 16:01:53  billhorsman
 fix doc

 Revision 1.4  2002/11/02 14:22:16  billhorsman
 Documentation

 Revision 1.3  2002/11/02 13:57:34  billhorsman
 checkstyle

 Revision 1.2  2002/11/02 11:37:48  billhorsman
 New tests

 Revision 1.1  2002/10/30 21:17:51  billhorsman
 new performance tests

*/

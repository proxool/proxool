/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.dbscript;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.logicalcobwebs.proxool.ProxoolException;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Allows you to run scripts from file.
 *
 * @version $Revision: 1.11 $, $Date: 2003/03/03 11:12:03 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public class ScriptFacade {

    private static final Log LOG = LogFactory.getLog(ScriptFacade.class);

    /**
     * Run the script using the appropriate handler
     * @param scriptLocation the path to the file that contains the script XML
     * @param adapter so we know where to get {@link java.sql.Connection connections} from.
     */
    public static void runScript(String scriptLocation, ConnectionAdapterIF adapter) {
        runScript(scriptLocation, adapter, null);
    }

    /**
     * Run the script using the appropriate handler
     * @param scriptLocation the path to the file that contains the script XML
     * @param adapter so we know where to get {@link java.sql.Connection connections} from.
     * @param commandFilter allows you to filter which commands get run and do things to the {@link java.sql.Connection}
     */
    public static void runScript(String scriptLocation, ConnectionAdapterIF adapter, CommandFilterIF commandFilter) {

        File scriptFile = new File(scriptLocation);
        if (!scriptFile.canRead()) {
            throw new RuntimeException("Can't read from file at " + scriptFile.getAbsolutePath());
        }

        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setValidating(false);
            saxParserFactory.setNamespaceAware(true);
            SAXParser saxParser = saxParserFactory.newSAXParser();
            saxParser.getXMLReader().setFeature("http://xml.org/sax/features/namespaces", true);
            saxParser.getXMLReader().setErrorHandler(new ErrorHandler() {
                public void warning(SAXParseException exception)
                        throws SAXException {
                    LOG.warn(exception.getLineNumber() + ":" + exception.getColumnNumber(), exception);
                }

                public void error(SAXParseException exception)
                        throws SAXException {
                    LOG.error(exception.getLineNumber() + ":" + exception.getColumnNumber(), exception);
                }

                public void fatalError(SAXParseException exception)
                        throws SAXException {
                    LOG.error(exception.getLineNumber() + ":" + exception.getColumnNumber(), exception);
                }
            });

            ScriptBuilder scriptBuilder = new ScriptBuilder();
            saxParser.parse(scriptFile, scriptBuilder);
            Script script = scriptBuilder.getScript();

            ScriptRunner.runScript(script, adapter, commandFilter);

        } catch (FactoryConfigurationError factoryConfigurationError) {
            LOG.error(factoryConfigurationError);
        } catch (ParserConfigurationException e) {
            LOG.error("Problem running script " + scriptLocation, e);
        } catch (SAXException e) {
            LOG.error("Problem running script " + scriptLocation, e);
        } catch (IOException e) {
            LOG.error("Problem running script " + scriptLocation, e);
        } catch (SQLException e) {
            LOG.error("Problem running script " + scriptLocation, e);
        } catch (ProxoolException e) {
            LOG.error("Problem running script " + scriptLocation, e);
        }

    }

}

/*
 Revision history:
 $Log: ScriptFacade.java,v $
 Revision 1.11  2003/03/03 11:12:03  billhorsman
 fixed licence

 Revision 1.10  2003/02/19 15:14:21  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.9  2003/02/06 17:41:02  billhorsman
 now uses imported logging

 Revision 1.8  2003/01/17 00:38:12  billhorsman
 wide ranging changes to clarify use of alias and url -
 this has led to some signature changes (new exceptions
 thrown) on the ProxoolFacade API.

 Revision 1.7  2002/11/13 20:23:35  billhorsman
 change method name, throw exceptions differently, trivial changes

 Revision 1.6  2002/11/09 16:00:21  billhorsman
 fix doc

 Revision 1.5  2002/11/07 19:08:54  billhorsman
 Fixed up tests a bit

 Revision 1.4  2002/11/06 21:06:21  billhorsman
 Support for CommandFilterIF

 Revision 1.3  2002/11/02 14:22:16  billhorsman
 Documentation

 Revision 1.2  2002/11/02 13:57:34  billhorsman
 checkstyle

 Revision 1.1  2002/11/02 11:29:53  billhorsman
 new script runner for testing

*/

/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.dbscript;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parser to get {@link org.logicalcobwebs.dbscript.Script} from XML source
 *
 * @version $Revision: 1.7 $, $Date: 2003/02/19 15:14:21 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
class ScriptBuilder extends DefaultHandler {

    private static final Log LOG = LogFactory.getLog(ScriptBuilder.class);

    private Script script = null;

    /**
     * @see DefaultHandler#startElement
     */
    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
            throws SAXException {

        if (qName.equals("script")) {
            script = new Script();
            script.setName(attributes.getValue("name"));
            script.setDriver(attributes.getValue("driver"));
            script.setUrl(attributes.getValue("url"));

        } else if (qName.equals("info")) {
            String name = attributes.getValue("name");
            String value = attributes.getValue("value");
            script.addProperty(name, value);

        } else if (qName.equals("command")) {
            Command command = new Command();
            command.setName(attributes.getValue("name"));
            command.setSql(attributes.getValue("sql"));
            if (attributes.getValue("load") != null) {
                int load = Integer.parseInt(attributes.getValue("load"));
                command.setLoad(load);
            }
            if (attributes.getValue("loops") != null) {
                int loops = Integer.parseInt(attributes.getValue("loops"));
                command.setLoops(loops);
            }
            if (attributes.getValue("exception") != null) {
                String exception = attributes.getValue("exception");
                command.setException(exception);
            }
            script.addCommand(command);
        }

    }

    /**
     * Get the script we just built. Call *after* {@link javax.xml.parsers.SAXParser#parse parsing}
     * @return the new script
     */
    protected Script getScript() {
        return script;
    }

}

/*
 Revision history:
 $Log: ScriptBuilder.java,v $
 Revision 1.7  2003/02/19 15:14:21  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.6  2003/02/06 17:41:02  billhorsman
 now uses imported logging

 Revision 1.5  2002/11/09 16:00:08  billhorsman
 fix doc

 Revision 1.4  2002/11/09 14:45:07  billhorsman
 now threaded and better exception handling

 Revision 1.3  2002/11/02 14:22:16  billhorsman
 Documentation

 Revision 1.2  2002/11/02 13:57:34  billhorsman
 checkstyle

 Revision 1.1  2002/11/02 11:29:53  billhorsman
 new script runner for testing

*/

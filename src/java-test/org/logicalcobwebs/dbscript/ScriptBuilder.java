/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.dbscript;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <link rel="stylesheet" href="{@docRoot}/cg.css" type="text/css">
 *
 * Parser to get {@link org.logicalcobwebs.dbscript.Script} from XML source
 *
 * @version $Revision: 1.2 $, $Date: 2002/11/02 13:57:34 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since GSI 5.0
 */
class ScriptBuilder extends DefaultHandler {

    private static final Log LOG = LogFactory.getLog(ScriptBuilder.class);

    private Script script = null;

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
            if (attributes.getValue("ignoreException") != null) {
                boolean ignoreException = Boolean.valueOf(attributes.getValue("ignoreException")).booleanValue();
                command.setIgnoreException(ignoreException);
            }
            script.addCommand(command);
        }

    }

    protected Script getScript() {
        return script;
    }
}

/*
 Revision history:
 $Log: ScriptBuilder.java,v $
 Revision 1.2  2002/11/02 13:57:34  billhorsman
 checkstyle

 Revision 1.1  2002/11/02 11:29:53  billhorsman
 new script runner for testing

*/

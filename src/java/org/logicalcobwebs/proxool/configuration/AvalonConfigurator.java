/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.configuration;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.logicalcobwebs.proxool.ProxoolConstants;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Configurator for the <a href="http://jakarta.apache.org/avalon">Avalon Framework</a>.
 * The configuration can contain any number of &lt;proxool&gt; elements. The &lt;proxool&gt; elements
 * are delegated to {@link XMLConfigurator},
 * and have exactly the same format as is documented in that class.
 *
 * @version $Revision: 1.4 $, $Date: 2002/12/16 11:47:00 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class AvalonConfigurator implements Configurable, ThreadSafe {

    /**
     * Check that all top level elements are named proxool and hand them to
     * {@link XMLConfigurator}.
     * @param configuration the configuration handed over by the Avalon Framework.
     * @throws ConfigurationException if the configuration fails.
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        final XMLConfigurator xmlConfigurator = new XMLConfigurator();
        final Configuration[] children = configuration.getChildren();
        for (int i = 0; i < children.length; ++i) {
            if (!children[i].getNamespace().equals(ProxoolConstants.PROXOOL)) {
                throw new ConfigurationException("Found element named " + children[i].getName() + ". Only "
                        + ProxoolConstants.PROXOOL + " top level elements are alowed.");
            }
        }
        try {
            xmlConfigurator.startDocument();
            reportProperties(xmlConfigurator, configuration.getChildren());
            xmlConfigurator.endDocument();
        } catch (SAXException e) {
            throw new ConfigurationException("", e);
        }
    }

    // Parse the properties recursively, and report found properties to the given XMLConfigurator
    private void reportProperties(XMLConfigurator xmlConfigurator, Configuration[] properties)
            throws ConfigurationException, SAXException {
        Configuration[] children = null;
        String value = null;
        String namespace = null;
        for (int i = 0; i < properties.length; ++i) {
            Configuration configuration = properties[i];
            namespace = configuration.getNamespace();
            if (namespace == null) {
                namespace = "";
            }
            xmlConfigurator.startElement(namespace, configuration.getName(), "", getAttributes(configuration));
            children = configuration.getChildren();
            // If this is a leaf node, report the value,
            // else recurse.
            if (children == null || children.length < 1) {
                value = configuration.getValue();
                if (value != null) {
                    xmlConfigurator.characters(value.toCharArray(), 0, value.length());
                }
            } else {
                reportProperties(xmlConfigurator, children);
            }
            xmlConfigurator.endElement(namespace, configuration.getName(), "");
        }
    }

    // create a SAX attributes instance from
    // Avalon configuration attributes
    private Attributes getAttributes(Configuration configuration) throws ConfigurationException {
        final AttributesImpl attributes = new AttributesImpl();
        final String[] avalonAttributeNames = configuration.getAttributeNames();
        if (avalonAttributeNames != null && avalonAttributeNames.length > 0) {
            for (int i = 0; i < avalonAttributeNames.length; ++i) {
                attributes.addAttribute("", avalonAttributeNames[i], "", "CDATA",
                        configuration.getAttribute(avalonAttributeNames[i]));
            }
        }
        return attributes;
    }
}

/*
 Revision history:
 $Log: AvalonConfigurator.java,v $
 Revision 1.4  2002/12/16 11:47:00  billhorsman
 checkstyle

 Revision 1.3  2002/12/16 02:37:14  chr32
 Updated to new driver-properties xml format.

 Revision 1.2  2002/12/15 19:42:18  chr32
 Rewrite. Now delegates to XMLConfigurator.

 Revision 1.1  2002/12/15 18:48:33  chr32
 Movied in from 'ext' source tree.

 Revision 1.5  2002/10/27 13:05:01  billhorsman
 checkstyle

 Revision 1.4  2002/10/27 12:00:14  billhorsman
 moved classes from ext sub-package which is now obsolete - let's keep everything together in one place

 Revision 1.2  2002/10/23 21:04:37  billhorsman
 checkstyle fixes (reduced max line width and lenient naming convention

 Revision 1.1  2002/09/19 08:53:41  billhorsman
 created new ext package

 Revision 1.2  2002/09/18 13:48:56  billhorsman
 checkstyle and doc

 Revision 1.1.1.1  2002/09/13 08:14:16  billhorsman
 new

 Revision 1.5  2002/08/24 19:57:15  billhorsman
 checkstyle changes

 Revision 1.4  2002/07/10 16:14:47  billhorsman
 widespread layout changes and move constants into ProxoolConstants

 Revision 1.3  2002/07/10 10:52:51  billhorsman
 doc fix

 Revision 1.2  2002/07/02 23:08:55  billhorsman
 new config package to allow us to introduce other ways

 Revision 1.1  2002/07/02 23:05:31  billhorsman
 New config package so that we can introduce other ways of configuring pools.

 Revision 1.6  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.5  2002/07/02 11:14:26  billhorsman
 added test (andbug fixes) for FileLogger

 Revision 1.4  2002/06/28 11:19:47  billhorsman
 improved doc

 Revision 1.3  2002/06/05 09:01:31  billhorsman
 removed ConnectionFacadeIF interface in preparation for new, cleaner ProxoolFacade class. _And_ did a code layout update. Why, of
 why did I mix that up with one commit? It makes it unclear where the cosmetic changes and code changes were made. I won't do it again.

 Revision 1.2  2002/06/04 22:23:00  billhorsman
 added class header comments

 Revision 1.1.1.1  2002/06/04 14:24:01  billhorsman
 start


*/

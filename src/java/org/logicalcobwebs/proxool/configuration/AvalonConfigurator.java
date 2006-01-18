/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.configuration;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Configurator for the <a href="http://jakarta.apache.org/avalon/framework/" target="_new">Avalon Framework</a>.
 * The configuration can contain any number of &lt;proxool&gt; elements. The &lt;proxool&gt; elements
 * are delegated to {@link XMLConfigurator},
 * and have exactly the same format as is documented in that class.
 * <p>
 * This is a "faceless" Avalon component. This means that it does not present an operational interface, it
 * simply configures Proxool when Avalon calls its <code>configure</code> method. You need to lookup this
 * component in your bootstrap code to make this happen.
 * </p>
 * <p>
 * The configuration takes one attribute: <code>close-on-dispose</code>
 * <br>
 * You can use this to let this configurator know
 * wether or not to close the pools it has created
 * when it is disposed.
 * <br>Legal values are <code>true</code> or <code>false</code>. Default: <code>true</code>.
 * </p>
 *
 * @version $Revision: 1.14 $, $Date: 2006/01/18 14:39:58 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class AvalonConfigurator implements Component, Configurable, ThreadSafe, Disposable {
    private static final Log LOG = LogFactory.getLog(AvalonConfigurator.class);

    /**
     * Avalon ROLE id for this component.
     */
    public static final String ROLE = AvalonConfigurator.class.getName();

    /**
     * Constant for the boolean "close-on-dispose" attribute that signifies
     * wether or not this configurator shall close the pools it has created
     * when it is disposed. Legal values are "true" or "false". Default: true.
     *
     */
    public static final String CLOSE_ON_DISPOSE_ATTRIBUTE = "close-on-dispose";

    private boolean closeOnDispose = true;
    private final List configuredPools = new ArrayList(3);

    /**
     * Check that all top level elements are named proxool and hand them to
     * {@link XMLConfigurator}.
     * @param configuration the configuration handed over by the Avalon Framework.
     * @throws ConfigurationException if the configuration fails.
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        final XMLConfigurator xmlConfigurator = new XMLConfigurator();
        this.closeOnDispose = configuration.getAttributeAsBoolean(CLOSE_ON_DISPOSE_ATTRIBUTE, true);
        final Configuration[] children = configuration.getChildren();
        for (int i = 0; i < children.length; ++i) {
            if (!children[i].getName().equals(ProxoolConstants.PROXOOL)) {
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

    /**
     * If {@link #CLOSE_ON_DISPOSE_ATTRIBUTE} is set: Close all connection pools that this configurator has configured.
     * <br>...else: do nothing.
     */
    public void dispose() {
        LOG.info("Disposing.");
        if (this.closeOnDispose) {
            Iterator configuredPools = this.configuredPools.iterator();
            String alias = null;
            while (configuredPools.hasNext()) {
                alias = (String) configuredPools.next();
                LOG.info("Closing connection pool '" + alias + "'.");
                try {
                    ProxoolFacade.removeConnectionPool(alias);
                } catch (ProxoolException e) {
                    LOG.error("Closing of connection pool '" + alias + "' failed.", e);
                }
            }
        } else {
            LOG.info(CLOSE_ON_DISPOSE_ATTRIBUTE + " attribute is not set, so configured pools will not be closed.");
        }
        LOG.info("Disposed.");
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
            if (LOG.isDebugEnabled()) {
                LOG.debug("Reporting element start for " + configuration.getName());
            }
            final String lName = namespace.length() == 0 ? "" : configuration.getName();
            final String qName = namespace.length() == 0 ? configuration.getName() : "";

            xmlConfigurator.startElement(namespace, lName, qName, getAttributes(configuration));
            children = configuration.getChildren();
            // If this is a leaf node, report the value,
            // else recurse.
            if (children == null || children.length < 1) {
                value = configuration.getValue(null);
                if (value != null) {
                    xmlConfigurator.characters(value.toCharArray(), 0, value.length());
                }
            } else {
                reportProperties(xmlConfigurator, children);
            }
            xmlConfigurator.endElement(namespace, lName, qName);
            if (lName.equals(ProxoolConstants.PROXOOL) || qName.equals(ProxoolConstants.PROXOOL)) {
                Configuration conf = configuration.getChild(ProxoolConstants.ALIAS, false);
                if (conf != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Adding to configured pools: " + conf.getValue());
                    }
                    this.configuredPools.add(conf.getValue());
                } else {
                    LOG.error("proxool element was missing required element 'alias'");
                }
            }
        }
    }

    // create a SAX attributes instance from
    // Avalon configuration attributes
    private Attributes getAttributes(Configuration configuration) throws ConfigurationException {
        final AttributesImpl attributes = new AttributesImpl();
        final String[] avalonAttributeNames = configuration.getAttributeNames();
        if (avalonAttributeNames != null && avalonAttributeNames.length > 0) {
            for (int i = 0; i < avalonAttributeNames.length; ++i) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Adding attribute " + avalonAttributeNames[i] + " with value "
                        + configuration.getAttribute(avalonAttributeNames[i]));
                }
                attributes.addAttribute("", avalonAttributeNames[i], avalonAttributeNames[i], "CDATA",
                        configuration.getAttribute(avalonAttributeNames[i]));
                    LOG.debug("In attributes: " + avalonAttributeNames[i] + " with value "
                        + attributes.getValue(avalonAttributeNames[i]));
                }
            }
        return attributes;
    }
}

/*
 Revision history:
 $Log: AvalonConfigurator.java,v $
 Revision 1.14  2006/01/18 14:39:58  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.13  2003/03/10 15:26:53  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.12  2003/03/03 11:12:00  billhorsman
 fixed licence

 Revision 1.11  2003/02/19 16:52:39  chr32
 Added support for close-on-dispose attribute.

 Revision 1.10  2003/02/06 17:41:05  billhorsman
 now uses imported logging

 Revision 1.9  2003/01/27 18:26:42  billhorsman
 refactoring of ProxyConnection and ProxyStatement to
 make it easier to write JDK 1.2 patch

 Revision 1.8  2002/12/23 02:59:38  chr32
 Tiny doc fix.

 Revision 1.7  2002/12/23 02:58:16  chr32
 Improved doc.

 Revision 1.6  2002/12/23 02:44:44  chr32
 Added ROLE id and started implementing Component.
 Improved namespace support.

 Revision 1.5  2002/12/18 23:31:57  chr32
 Expanded doc.

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

/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.sql.SQLException;
import java.util.Properties;

/**
 * <p>Configures a pool from an XML source.</p>
 *
 * <p>Properties that you pass onto the delegate driver have to be treated specially. They
 * must be contained within a &lt;delegate-properties&gt; element.</p>
 *
 * <pre>
 *   &lt;proxool&gt;
 *     &lt;name&gt;apple&lt;/name&gt;
 *     &lt;class&gt;org.gjt.mm.mysql.Driver&lt;/class&gt;
 *     &lt;url&gt;jdbc:mysql://localhost/test&lt;/url&gt;
 *     &lt;max-connection-count&gt;10&lt;/max-connection-count&gt;
 *     &lt;delegate-properties&gt;
 *       &lt;password&gt;abc&lt;/password&gt;
 *     &lt;/delegate-properties&gt;
 *   &lt;/proxool&gt;
 * </pre>
 *
 * <p>When the parser reaches the end of &lt;proxool&gt; element the pool
 * is automatically registered. You can contain the &lt;proxool&gt; element
 * in any other element as you wish. And the &lt;proxool&gt; element can
 * occur as many times as you wish. This allows you to use an XML file that
 * configures your whole application as the source. This configurator will
 * ignore everything apart from the contained within the &lt;proxool&gt;
 * element.</p>
 *
 * @version $Revision: 1.1 $, $Date: 2002/09/13 08:14:18 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class XMLConfigurator extends DefaultHandler {

    private StringBuffer content = new StringBuffer();

    private String poolName;

    private String delegateClass;

    private String delegateUrl;

    private Properties properties = new Properties();

    private static final String PROXOOL = "proxool";

    private static final String VALUE = "value";

    private static final String POOL_NAME = "name";

    private static final String DELEGATE_CLASS = "class";

    private static final String DELEGATE_URL = "url";

    private static final String DELEGATE_PROPERTIES = "delegate-properties";

    private boolean insideDelegateProperties;

    private boolean insideProxool;

    public void startElement(String uri, String lname, String qname, Attributes attributes) throws SAXException {
        content.setLength(0);

        if (lname.equals(PROXOOL)) {
            if (insideProxool) {
                throw new SAXException("A <" + PROXOOL + "> element can't contain another <" + PROXOOL + "> element.");
            }
            insideProxool = true;
            properties.clear();
            delegateClass = null;
            delegateUrl = null;
        }

        if (insideProxool) {
            if (lname.equals(DELEGATE_PROPERTIES)) {
                insideDelegateProperties = true;
            }
        }
    }

    public void characters(char[] chars, int start, int length) throws SAXException {
        if (insideProxool) {
            content.append(chars, start, length);
        }
    }

    public void endElement(String uri, String lname, String qname) throws SAXException {

        // Are we ending a proxool configuration section?
        if (lname.equals(PROXOOL)) {

            // Check that we have defined the minimum information
            if (delegateClass == null || delegateUrl == null) {
                throw new SAXException("You must define the " + DELEGATE_CLASS + " and the " + DELEGATE_URL + ".");
            }

            // Build the URL; optinally defining a name
            StringBuffer url = new StringBuffer();
            url.append("proxool");
            if (poolName != null) {
                url.append(ProxoolConstants.ALIAS_DELIMITER);
                url.append(poolName);
            }
            url.append(ProxoolConstants.URL_DELIMITER);
            url.append(delegateClass);
            url.append(ProxoolConstants.URL_DELIMITER);
            url.append(delegateUrl);

            // Register the pool
            try {
                ProxoolFacade.registerConnectionPool(url.toString(), properties);
            } catch (SQLException e) {
                throw new SAXException(e);
            }

            // This ensures we ignore remaining XML until we come across another
            // <proxool> element.
            insideProxool = false;
        }

        // It's not appropriate to have contents within the <proxool> element.
        // E.g <proxool>abc</proxool> is not good.
        if (insideProxool && !lname.equals(PROXOOL)) {
            if (lname.equals(DELEGATE_PROPERTIES)) {
                insideDelegateProperties = false;
            } else {
                setProperty(lname, content.toString().trim());
            }
        }
    }

    private void setProperty(String localName, String value) {

        if (localName.equals(POOL_NAME)) {
            poolName = value;
        } else if (localName.equals(DELEGATE_CLASS)) {
            delegateClass = value;
        } else if (localName.equals(DELEGATE_URL)) {
            delegateUrl = value;
        } else {
            properties.put(insideDelegateProperties ? "" : (ProxoolConstants.PROPERTY_PREFIX + localName), value);
        }

    }

}

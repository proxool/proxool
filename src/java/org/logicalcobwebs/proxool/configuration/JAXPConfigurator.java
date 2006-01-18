/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.ProxoolException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Configurator that uses JAXP to get a parser for Proxool configuration xml. The parser relies on JAXP version 1.1 or higher
 * and is namespace aware.
 * <p>
 * See {@link XMLConfigurator} for the Proxool xml configuration format.
 * </p>
 * <p>
 * All the <code>configure</code> methods of this class takes a boolean argument describing whether the
 * xml should be validated or not. If you want your xml to be validated be sure to read the
 * <a href="XMLConfigurator.html#validation">Validation</a> chapter in the JavaDoc for {@link XMLConfigurator}.
 * </p>
 * @version $Revision: 1.12 $, $Date: 2006/01/18 14:39:58 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.6
 */
public class JAXPConfigurator {
    private static final Log LOG = LogFactory.getLog(JAXPConfigurator.class);
    private static final boolean NAMESPACE_AWARE = true;

    /**
     * Configure Proxool with xml from the given file.
     * @param xmlFileName the file to read xml from.
     * @param validate <code>true</code> if the parsel shall be validating, and <code>false</code> otherwise.
     * @throws ProxoolException if the configuration fails.
     */
    public static void configure(String xmlFileName, boolean validate) throws ProxoolException {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Configuring from xml file: " + xmlFileName);
            }
            configure(new InputSource(new FileReader(xmlFileName)), validate);
        } catch (FileNotFoundException e) {
            throw new ProxoolException(e);
        }
    }

    /**
     * Configure Proxool with xml from the given InputSource.
     * @param inputSource the InputSource to read xml from.
     * @param validate <code>true</code> if the parsel shall be validating, and <code>false</code> otherwise.
     * @throws ProxoolException if the configuration fails.
     */
    public static void configure(InputSource inputSource, boolean validate) throws ProxoolException {
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            if (LOG.isDebugEnabled()) {
                LOG.debug("SAXParserFactory class: " + saxParserFactory.getClass().getName());
            }
            saxParserFactory.setValidating(validate);
            final SAXParser saxParser = saxParserFactory.newSAXParser();
            if (LOG.isDebugEnabled()) {
                LOG.debug("sax parser class" + saxParser.getClass().getName());
            }
            final XMLReader xmlReader = saxParser.getXMLReader();
            if (LOG.isDebugEnabled()) {
                LOG.debug("XML reader class: " + xmlReader.getClass().getName());
            }
            final XMLConfigurator xmlConfigurator = new XMLConfigurator();
            xmlReader.setErrorHandler(xmlConfigurator);
            setSAXFeature(xmlReader, "http://xml.org/sax/features/namespaces", NAMESPACE_AWARE);
            setSAXFeature(xmlReader, "http://xml.org/sax/features/namespace-prefixes", !NAMESPACE_AWARE);
            saxParser.parse(inputSource, xmlConfigurator);
        } catch (ParserConfigurationException pce) {
            throw new ProxoolException("Parser configuration failed", pce);
        } catch (SAXException se) {
            throw new ProxoolException("Parsing failed.", se);
        } catch (IOException ioe) {
            throw new ProxoolException("Parsing failed.", ioe);
        }
    }

    /**
     * Configure Proxool with xml from the given reader.
     * @param reader the reader to read xml from.
     * @param validate <code>true</code> if the parsel shall be validating, and <code>false</code> otherwise.
     * @throws ProxoolException if the configuration fails.
     */
    public static void configure(Reader reader, boolean validate) throws ProxoolException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Configuring from reader: " + reader);
        }
        configure(new InputSource(reader), validate);
    }

    // only log warning on problems with recognition and support of features
    // we'll probably pull through anyway...
    private static void setSAXFeature(XMLReader xmlReader, String feature, boolean state) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting sax feature: '" + feature + "'. State: " + state + ".");
        }
        try {
            xmlReader.setFeature(feature, state);
        } catch (SAXNotRecognizedException e) {
            LOG.warn("Feature: '" + feature + "' not recognised by xml reader " + xmlReader + ".", e);
        } catch (SAXNotSupportedException e) {
            LOG.warn("Feature: '" + feature + "' not supported by xml reader " + xmlReader + ".", e);
        }
    }
}

/*
 Revision history:
 $Log: JAXPConfigurator.java,v $
 Revision 1.12  2006/01/18 14:39:58  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.11  2004/05/14 21:15:47  brenuart
 Fix type in method name

 Revision 1.10  2003/03/10 23:43:15  billhorsman
 reapplied checkstyle that i'd inadvertently let
 IntelliJ change...

 Revision 1.9  2003/03/10 15:26:54  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.8  2003/03/03 11:12:00  billhorsman
 fixed licence

 Revision 1.7  2003/02/06 17:41:05  billhorsman
 now uses imported logging

 Revision 1.6  2003/01/27 18:26:42  billhorsman
 refactoring of ProxyConnection and ProxyStatement to
 make it easier to write JDK 1.2 patch

 Revision 1.5  2002/12/18 23:31:57  chr32
 Expanded doc.

 Revision 1.4  2002/12/16 11:46:59  billhorsman
 checkstyle

 Revision 1.3  2002/12/16 02:38:47  chr32
 Updated to new driver-properties xml format.

 Revision 1.2  2002/12/15 19:43:11  chr32
 Style fixes.

 Revision 1.1  2002/12/15 18:49:55  chr32
 Init rev.

*/
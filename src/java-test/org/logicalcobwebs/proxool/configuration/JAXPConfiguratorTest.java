/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.configuration;

import org.logicalcobwebs.proxool.AbstractProxoolTest;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.TestHelper;
import org.xml.sax.InputSource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;

/**
 * Tests that the JAXPConfgiuration works in various scenarios.
 * This is also a test of the {@link XMLConfigurator}, as it is delegated to.
 *
 * @version $Revision: 1.14 $, $Date: 2003/03/04 10:58:45 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.6
 */
public class JAXPConfiguratorTest extends AbstractProxoolTest {

    /**
     * @see junit.framework.TestCase#TestCase
     */
    public JAXPConfiguratorTest(String name) {
        super(name);
    }

    /**
     * Test that the confguration succeds and that all expected properties
     * has been received by Proxool. This test is done with a
     * xml without namespaces and validiation.
     * @throws ProxoolException if the configuration fails.
     * @throws SQLException if ProxoolFacade operation fails.
     */
    public void testNoNamspaces() throws ProxoolException, SQLException {
        final String xmlFile = "src/java-test/org/logicalcobwebs/proxool/configuration/test-no-ns.xml";
        JAXPConfigurator.configure(xmlFile, false);
        try {
            assertNotNull("2nd (deeply nested) pool was not configured.", ProxoolFacade.getConnectionPoolDefinition("xml-test-2"));
        } catch (ProxoolException e) {
            fail("2nd (deeply nested) pool was not configured.");
        }
        TestHelper.equalsCompleteAlternativeProperties(ProxoolFacade.getConnectionPoolDefinition("xml-test"));
        TestHelper.equalsCompleteAlternativeProperties(ProxoolFacade.getConnectionPoolDefinition("xml-test-2"));
    }

    /**
     * Test that the confguration succeds and that all expected properties
     * has been received by Proxool. This test is done with a
     * xml with namespaces and without validiation.
     * @throws ProxoolException if the configuration fails.
     * @throws SQLException if ProxoolFacade operation fails.
     */
    public void testWithNamspaces() throws ProxoolException, SQLException {
        final String xmlFile = "src/java-test/org/logicalcobwebs/proxool/configuration/test-ns.xml";
        JAXPConfigurator.configure(xmlFile, false);
        try {
            assertNotNull("2nd (deeply nested) pool was not configured.", ProxoolFacade.getConnectionPoolDefinition("xml-test-ns-2"));
        } catch (ProxoolException e) {
            fail("2nd (deeply nested) pool was not configured.");
        }
        TestHelper.equalsCompleteAlternativeProperties(ProxoolFacade.getConnectionPoolDefinition("xml-test-ns"));
        TestHelper.equalsCompleteAlternativeProperties(ProxoolFacade.getConnectionPoolDefinition("xml-test-ns-2"));
    }

    /**
     * Test that the confguration succeds and that all expected properties
     * has been received by Proxool, and that validiation succceds. This test is done with a
     * xml without namespaces.
     * @throws ProxoolException if the configuration fails.
     * @throws SQLException if ProxoolFacade operation fails.
     * @throws FileNotFoundException if the xml file is not found.
     */
    public void testValidiation() throws ProxoolException, SQLException, FileNotFoundException {
        final String xmlFile = "src/java-test/org/logicalcobwebs/proxool/configuration/test-valid.xml";
        final InputSource inputSource = new InputSource(new FileInputStream(xmlFile));
        inputSource.setSystemId(getWorkingDirectoryUrl());
        JAXPConfigurator.configure(inputSource, true);
        try {
            assertNotNull("2nd (deeply nested) pool was not configured.", ProxoolFacade.getConnectionPoolDefinition("xml-test-validating-2"));
        } catch (ProxoolException e) {
            fail("2nd (deeply nested) pool was not configured.");
        }
        TestHelper.equalsCompleteAlternativeProperties(ProxoolFacade.getConnectionPoolDefinition("xml-test-validating"));
        TestHelper.equalsCompleteAlternativeProperties(ProxoolFacade.getConnectionPoolDefinition("xml-test-validating-2"));
    }

    /**
     * Test that the confguration fails when validiation is turned on and the given xml is not valid.
     * @throws SQLException if ProxoolFacade operation fails.
     * @throws FileNotFoundException if the xml file is not found.
     */
    public void testValidiationFailure() throws SQLException, FileNotFoundException, ProxoolException {
        final String xmlFile = "src/java-test/org/logicalcobwebs/proxool/configuration/test-not-valid.xml";
        final InputSource inputSource = new InputSource(new FileInputStream(xmlFile));
        inputSource.setSystemId(getWorkingDirectoryUrl());
        boolean failure = false;
        try {
            JAXPConfigurator.configure(inputSource, true);
        } catch (ProxoolException e) {
            failure = true;
        }
        assertTrue("Configuration did not fail on unvalid xml document.", failure);
    }

    private static String getWorkingDirectoryUrl() {
        String userDir = System.getProperty("user.dir");
        String toReplace = "\\";
        String replaceWith = "/";
        int pos = 0;
        if (!toReplace.equals(replaceWith)) {
            while (true) {
                pos = userDir.indexOf(toReplace, pos);
                if (pos == -1) {
                    break;
                }
                userDir = userDir.substring(0, pos) + replaceWith + userDir.substring(pos + toReplace.length());
                pos += replaceWith.length();
            }
        }
        if (!userDir.startsWith("/")) {
            userDir = "/" + userDir;
        }
        if (!userDir.endsWith("/")) {
            userDir = userDir + "/";
        }
        return "file://" + userDir;
    }

}

/*
 Revision history:
 $Log: JAXPConfiguratorTest.java,v $
 Revision 1.14  2003/03/04 10:58:45  billhorsman
 checkstyle

 Revision 1.13  2003/03/04 10:24:41  billhorsman
 removed try blocks around each test

 Revision 1.12  2003/03/03 17:36:33  billhorsman
 leave shutdown to AbstractProxoolTest

 Revision 1.11  2003/03/03 17:09:18  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.10  2003/03/03 11:12:06  billhorsman
 fixed licence

 Revision 1.9  2003/03/01 15:27:25  billhorsman
 checkstyle

 Revision 1.8  2003/02/27 18:01:49  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 Revision 1.7  2003/02/19 15:14:27  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.6  2003/01/18 15:13:14  billhorsman
 Signature changes (new ProxoolException
 thrown) on the ProxoolFacade API.

 Revision 1.5  2002/12/18 03:13:00  chr32
 Added tests for xml validation.

 Revision 1.4  2002/12/16 17:06:41  billhorsman
 new test structure

 Revision 1.3  2002/12/16 02:35:40  chr32
 Updated to new driver-properties xml format.

 Revision 1.2  2002/12/15 19:41:26  chr32
 Style fixes.

 Revision 1.1  2002/12/15 19:10:49  chr32
 Init rev.

*/

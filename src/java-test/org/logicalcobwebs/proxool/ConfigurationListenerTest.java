/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.util.Properties;

/**
 * Test that registering a {@link org.logicalcobwebs.proxool.ConfigurationListenerIF}
 * with the {@link org.logicalcobwebs.proxool.ProxoolFacade}
 * works.
 *
 * @version $Revision: 1.8 $, $Date: 2003/03/03 17:08:54 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class ConfigurationListenerTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(ConfigurationListenerTest.class);

    /**
     * @see junit.framework.TestCase#TestCase
     */
    public ConfigurationListenerTest(String s) {
        super(s);
    }

    /**
     * Add a listener
     *
     * @throws Exception if anything goes wrong
     */
    public void testAddAndRemove() throws Exception {

        String testName = "addAndRemove";
        String alias = testName;

        try {

            // Register pool
            String url = TestHelper.buildProxoolUrl(alias,
                    TestConstants.HYPERSONIC_DRIVER,
                    TestConstants.HYPERSONIC_TEST_URL);
            Properties info = new Properties();
            info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
            info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
            ProxoolFacade.registerConnectionPool(url, info);

            // add a listener
            MyConfigurationListener mcl1 = new MyConfigurationListener();
            ProxoolFacade.addConfigurationListener(alias, mcl1);

            // Update the definition
            Properties newInfo = new Properties();
            newInfo.setProperty(ProxoolConstants.VERBOSE_PROPERTY, Boolean.TRUE.toString());
            ProxoolFacade.updateConnectionPool(url, newInfo);
            assertEquals("definitionReceived", true, mcl1.isUpdateReceived());
            mcl1.reset();

            // add another listener
            MyConfigurationListener mcl2 = new MyConfigurationListener();
            ProxoolFacade.addConfigurationListener(alias, mcl2);

            // Update the definition
            newInfo = new Properties();
            newInfo.setProperty(ProxoolConstants.VERBOSE_PROPERTY, Boolean.FALSE.toString());
            ProxoolFacade.updateConnectionPool(url, newInfo);
            assertEquals("definitionReceived", true, mcl1.isUpdateReceived());
            assertEquals("definitionReceived", true, mcl2.isUpdateReceived());
            mcl1.reset();
            mcl2.reset();

            // Remove the first listener
            ProxoolFacade.removeConfigurationListener(alias, mcl1);

            // Update the definition
            newInfo = new Properties();
            newInfo.setProperty(ProxoolConstants.VERBOSE_PROPERTY, Boolean.TRUE.toString());
            ProxoolFacade.updateConnectionPool(url, newInfo);
            assertEquals("definitionReceived", false, mcl1.isUpdateReceived());
            assertEquals("definitionReceived", true, mcl2.isUpdateReceived());
            mcl1.reset();
            mcl2.reset();

            // Remove the second listener
            ProxoolFacade.removeConfigurationListener(alias, mcl2);

            // Update the definition
            newInfo = new Properties();
            newInfo.setProperty(ProxoolConstants.VERBOSE_PROPERTY, Boolean.FALSE.toString());
            ProxoolFacade.updateConnectionPool(url, newInfo);
            assertEquals("definitionReceived", false, mcl1.isUpdateReceived());
            assertEquals("definitionReceived", false, mcl2.isUpdateReceived());
            mcl1.reset();
            mcl2.reset();

        } catch (Exception e) {
            LOG.error("Whilst performing " + testName, e);
            throw e;
        } finally {
            ProxoolFacade.removeConnectionPool(alias);
        }
    }

    /**
     * Do configuration listeners work
     *
     * @throws Exception if anything goes wrong
     */
    public void testConfigurationListeners() throws Exception {

        String testName = "configurationListener";
        String alias = testName;

        try {

            // Register pool
            final String delegateUrl1 = TestConstants.HYPERSONIC_URL_PREFIX + "1";
            final String delegateUrl2 = TestConstants.HYPERSONIC_URL_PREFIX + "2";

            final String url1 = TestHelper.buildProxoolUrl(alias,
                    TestConstants.HYPERSONIC_DRIVER,
                    delegateUrl1);
            final String url2 = TestHelper.buildProxoolUrl(alias,
                    TestConstants.HYPERSONIC_DRIVER,
                    delegateUrl2);

            Properties info = new Properties();
            info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
            info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
            ProxoolFacade.registerConnectionPool(url1, info);

            int propertyCount = info.size();

            // listen to the configuration
            MyConfigurationListener mcl = new MyConfigurationListener();
            ProxoolFacade.addConfigurationListener(alias, mcl);

            // Update the URL
            ProxoolFacade.updateConnectionPool(url2, null);
            LOG.debug("changed: " + mcl.getChangedInfo());
            LOG.debug("complete: " + mcl.getCompleteInfo());
            assertEquals("changed size", 0, mcl.getChangedInfo().size());
            assertEquals("complete size", propertyCount, mcl.getCompleteInfo().size());
            assertEquals("url", delegateUrl2, mcl.getConnectionPoolDefinition().getUrl());
            mcl.reset();

            // Add the verbose property
            Properties newInfo = new Properties();
            newInfo.setProperty(ProxoolConstants.VERBOSE_PROPERTY, Boolean.TRUE.toString());
            ProxoolFacade.updateConnectionPool(url2, newInfo);
            LOG.debug("changed: " + mcl.getChangedInfo());
            LOG.debug("complete: " + mcl.getCompleteInfo());
            assertEquals("completeInfo size", propertyCount + 1, mcl.getCompleteInfo().size());
            assertEquals("changedInfo size", 1, mcl.getChangedInfo().size());
            assertEquals("url", true, mcl.getConnectionPoolDefinition().isVerbose());
            mcl.reset();

            // modify the verbose property
            newInfo = new Properties();
            newInfo.setProperty(ProxoolConstants.VERBOSE_PROPERTY, Boolean.FALSE.toString());
            ProxoolFacade.updateConnectionPool(url2, newInfo);
            LOG.debug("changed: " + mcl.getChangedInfo());
            LOG.debug("complete: " + mcl.getCompleteInfo());
            assertEquals("completeInfo size", propertyCount + 1, mcl.getCompleteInfo().size());
            assertEquals("changedInfo size", 1, mcl.getChangedInfo().size());
            assertEquals("url", false, mcl.getConnectionPoolDefinition().isVerbose());
            mcl.reset();

        } catch (Exception e) {
            LOG.error("Whilst performing " + testName, e);
            throw e;
        } finally {
            ProxoolFacade.removeConnectionPool(alias);
        }
    }

    class MyConfigurationListener implements ConfigurationListenerIF {

        private Properties completeInfo;

        private Properties changedInfo;

        private ConnectionPoolDefinitionIF connectionPoolDefinition;

        private boolean updateReceived;

        public void definitionUpdated(ConnectionPoolDefinitionIF connectionPoolDefinition, Properties completeInfo, Properties changedInfo) {
            this.connectionPoolDefinition = connectionPoolDefinition;
            this.completeInfo = completeInfo;
            this.changedInfo = changedInfo;
            updateReceived = true;
        }

        public Properties getCompleteInfo() {
            return completeInfo;
        }

        public Properties getChangedInfo() {
            return changedInfo;
        }

        public ConnectionPoolDefinitionIF getConnectionPoolDefinition() {
            return connectionPoolDefinition;
        }

        public boolean isUpdateReceived() {
            return updateReceived;
        }

        public void reset() {
            completeInfo.clear();
            changedInfo.clear();
            updateReceived = false;
        }

    }

}

/*
 Revision history:
 $Log: ConfigurationListenerTest.java,v $
 Revision 1.8  2003/03/03 17:08:54  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.7  2003/03/03 11:12:03  billhorsman
 fixed licence

 Revision 1.6  2003/02/27 18:01:47  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 Revision 1.5  2003/02/26 16:05:49  billhorsman
 widespread changes caused by refactoring the way we
 update and redefine pool definitions.

 Revision 1.4  2003/02/24 18:04:07  chr32
 Fixde some eroneous property names.

 Revision 1.3  2003/02/19 17:00:51  chr32
 Fixed eroneous method names.

 Revision 1.2  2003/02/19 15:14:22  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.1  2003/02/19 13:47:31  chr32
 Added configuration listener test.

 Revision 1.2  2003/02/18 16:58:12  chr32
 Checkstyle.

 Revision 1.1  2003/02/18 16:51:20  chr32
 Added tests for ConnectionListeners.

*/

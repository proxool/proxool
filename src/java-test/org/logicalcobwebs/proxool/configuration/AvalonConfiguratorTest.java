/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.configuration;

import junit.framework.TestCase;

import java.io.File;

import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.GlobalTest;
import org.logicalcobwebs.proxool.TestHelper;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.excalibur.component.ExcaliburComponentManager;
import org.apache.avalon.excalibur.component.DefaultRoleManager;
import org.apache.log.Hierarchy;
import org.apache.log.LogTarget;
import org.apache.log.Logger;
import org.apache.log.Priority;

/**
 * Tests that the AvalonConfgiuration works.
 *
 * @version $Revision: 1.3 $, $Date: 2003/02/19 15:14:26 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.6
 */
public class AvalonConfiguratorTest extends TestCase {
    private ExcaliburComponentManager componentManager;

    /**
     * @see TestCase#TestCase
     */
    public AvalonConfiguratorTest(String name) {
        super(name);
    }

    /**
     * Test that the configuration succeds and that all expected properties
     * has been received by Proxool. The configuration file does not use namspaces.
     * @throws org.apache.avalon.framework.component.ComponentException if the configuration fails.
     * @throws java.sql.SQLException if ProxoolFacade operation fails.
     */
    public void testNoNamspaces() throws Exception {
        initializeAvalon("src/java-test/org/logicalcobwebs/proxool/configuration/role.conf",
            "src/java-test/org/logicalcobwebs/proxool/configuration/component.conf");
        this.componentManager.lookup(AvalonConfigurator.ROLE);
        try {
            TestHelper.equalsCompleteAlternativeProperties(ProxoolFacade.getConnectionPoolDefinition("avalon-test"));
        } catch (ProxoolException e) {
            fail(e.getMessage());
        }
        try {
            TestHelper.equalsCompleteAlternativeProperties(ProxoolFacade.getConnectionPoolDefinition("avalon-test-2"));
        } catch (ProxoolException e) {
            fail(e.getMessage());
        }
        ProxoolFacade.removeConnectionPool("avalon-test");
        ProxoolFacade.removeConnectionPool("avalon-test-2");
        this.componentManager.dispose();
    }

    /**
     * Test that the configuration succeds and that all expected properties
     * has been received by Proxool. The configuration file uses namspaces.
     * @throws org.apache.avalon.framework.component.ComponentException if the configuration fails.
     * @throws java.sql.SQLException if ProxoolFacade operation fails.
     */
    public void testWithNamspaces() throws Exception {
        initializeAvalon("src/java-test/org/logicalcobwebs/proxool/configuration/role.conf",
            "src/java-test/org/logicalcobwebs/proxool/configuration/component-ns.conf");
        this.componentManager.lookup(AvalonConfigurator.ROLE);
        try {
            TestHelper.equalsCompleteAlternativeProperties(ProxoolFacade.getConnectionPoolDefinition("avalon-test-ns"));
        } catch (ProxoolException e) {
            fail(e.getMessage());
        }
        try {
            TestHelper.equalsCompleteAlternativeProperties(ProxoolFacade.getConnectionPoolDefinition("avalon-test-ns-2"));
        } catch (ProxoolException e) {
            fail(e.getMessage());
        }
        ProxoolFacade.removeConnectionPool("avalon-test-ns");
        ProxoolFacade.removeConnectionPool("avalon-test-ns-2");
        this.componentManager.dispose();
    }

    private void initializeAvalon(String roleFile, String componentFile) throws Exception {
        // create a Avalon logger
        final LogTarget logTarget = new LogKitTargetAdapter();
        final Logger rootLogger = Hierarchy.getDefaultHierarchy().getLoggerFor("root");
        rootLogger.unsetLogTargets();
        rootLogger.setLogTargets(new LogTarget[] {logTarget});
        rootLogger.setPriority(Priority.WARN);
        final DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder(true);
        // create component configuration
        final Configuration componentConfiguration = builder.buildFromFile(new File(componentFile));
        // create role configuration
        final Configuration roleConfiguration = builder.buildFromFile(new File(roleFile));
        // create and configure role and component managers.
        this.componentManager = new ExcaliburComponentManager();
        this.componentManager.setLogger(rootLogger.getChildLogger(ExcaliburComponentManager.class.getName()));
        DefaultRoleManager roleManager = new DefaultRoleManager();
        roleManager.setLogger(rootLogger.getChildLogger(DefaultRoleManager.class.getName()));
        roleManager.configure(roleConfiguration);
        componentManager.contextualize(new DefaultContext());
        componentManager.setRoleManager(roleManager);
        componentManager.configure(componentConfiguration);
        componentManager.initialize();
    }

    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        GlobalTest.globalSetup();
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        GlobalTest.globalTeardown();
    }
}

/*
 Revision history:
 $Log: AvalonConfiguratorTest.java,v $
 Revision 1.3  2003/02/19 15:14:26  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.2  2002/12/23 02:48:07  chr32
 Checkstyle.

 Revision 1.1  2002/12/23 02:40:49  chr32
 Init rev.

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

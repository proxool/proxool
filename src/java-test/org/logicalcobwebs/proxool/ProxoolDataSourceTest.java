/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.NamingManager;
import javax.sql.DataSource;
import java.util.Hashtable;
import java.util.Properties;

import tyrex.naming.MemoryContextFactory;

/**
 * Tests the Proxool datasources.
 *
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: chr32 $ (current maintainer)
 * @version $Revision: 1.1 $, $Date: 2004/03/18 17:10:01 $
 * @since Proxool 0.9
 */
public class ProxoolDataSourceTest extends AbstractProxoolTest {
    public ProxoolDataSourceTest(String alias) {
        super(alias);
    }

    /**
     * Test the Proxool managed DataSource
     */
    public void testPreconfiguredDataSource() throws Exception {
        String alias = "preconfiguredDatasourceTest";
        String jndiName = "TestDB";
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.JNDI_NAME_PROPERTY, jndiName);
        info.setProperty(ProxoolConstants.JNDI_PROPERTY_PREFIX + "java.naming.factory.initial",
            MemoryContextFactory.class.getName());
        info.setProperty(ProxoolConstants.JNDI_PROPERTY_PREFIX + "java.naming.factory.url.pkgs",
            "tyrex.naming");
        info.setProperty(ProxoolConstants.JNDI_PROPERTY_PREFIX + "java.naming.provider.url",
                alias);
        ProxoolFacade.registerConnectionPool(url, info);

        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, MemoryContextFactory.class.getName());
        env.put(Context.URL_PKG_PREFIXES, "tyrex.naming");
        env.put(Context.PROVIDER_URL, alias);
        Context context = new InitialContext(env);
        DataSource dataSource = (DataSource) context.lookup(jndiName);
        assertNotNull("Connection was null.", dataSource.getConnection());
        ProxoolFacade.removeConnectionPool(alias);
        context.close();
    }

    /**
     * Test the bean configured DataSource.
     */
    public void testBeanConfiguredDataSource() throws Exception {
        String alias = "beanConfiguredDataSourceTest";
        String jndiName = "jdbc/beanConfiguredTestDB";

        // pretend to be a J2EE server that instantites the data source
        // populates its properties and binds it to JNDI
        ProxoolDataSource dataSource = new ProxoolDataSource();
        dataSource.setAlias(alias);
        dataSource.setDriver(TestConstants.HYPERSONIC_DRIVER);
        dataSource.setDriverUrl(TestConstants.HYPERSONIC_TEST_URL);
        dataSource.setUser(TestConstants.HYPERSONIC_USER);
        dataSource.setPassword(TestConstants.HYPERSONIC_PASSWORD);

        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, MemoryContextFactory.class.getName());
        env.put(Context.URL_PKG_PREFIXES, "tyrex.naming");
        env.put(Context.PROVIDER_URL, alias);
        Context context = new InitialContext(env);
        context.createSubcontext("jdbc");
        context.bind(jndiName, dataSource);
        context.close();
        // end J2EE server

        // now... pretend to be a client.
        context = new InitialContext(env);
        DataSource clientDataSource = (DataSource) context.lookup(jndiName);
        assertNotNull("Connection was null.", clientDataSource.getConnection());
        ProxoolFacade.removeConnectionPool(alias);
        context.close();
    }

    /**
     * Test the bean configured DataSource.
     */
    public void testFactoryConfiguredDataSource() throws Exception {
        String alias = "factoryConfiguredDataSourceTest";
        String jndiName = "jdbc/factoryConfiguredTestDB";

        Reference reference = new Reference(ProxoolDataSource.class.getName(), ProxoolDataSource.class.getName(), null);
        reference.add(new StringRefAddr(ProxoolConstants.ALIAS_PROPERTY, alias));
        reference.add(new StringRefAddr(ProxoolConstants.DRIVER_CLASS_PROPERTY, TestConstants.HYPERSONIC_DRIVER));
        reference.add(new StringRefAddr(ProxoolConstants.DRIVER_URL_PROPERTY, TestConstants.HYPERSONIC_TEST_URL));
        reference.add(new StringRefAddr(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER));
        reference.add(new StringRefAddr(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD));

        // pretend to be a JNDI aware container that builds the DataSource
        // using its factory
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, MemoryContextFactory.class.getName());
        env.put(Context.URL_PKG_PREFIXES, "tyrex.naming");
        env.put(Context.PROVIDER_URL, alias);
        Context context = new InitialContext(env);
        context.createSubcontext("jdbc");
        context.bind(jndiName, NamingManager.getObjectInstance(reference, null, null, null));
        context.close();
        // end JNDI aware container

        // now... pretend to be a client.
        context = new InitialContext(env);
        DataSource clientDataSource = (DataSource) context.lookup(jndiName);
        assertNotNull("Connection was null.", clientDataSource.getConnection());
        ProxoolFacade.removeConnectionPool(alias);
        context.close();
    }
}


/*
 Revision history:
 $Log: ProxoolDataSourceTest.java,v $
 Revision 1.1  2004/03/18 17:10:01  chr32
 Renamed DataSourceTest -> ProxoolDataSourceTest. Added test for factory-configured mode.

 Revision 1.4  2004/03/15 23:56:33  chr32
 Added test for ProxoolDataSource.

 Revision 1.3  2004/03/15 02:46:09  chr32
 Added initial ProxoolDataSourceTest.

 */
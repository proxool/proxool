/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.util.Hashtable;
import java.util.Properties;

import tyrex.naming.MemoryContextFactory;

/**
 * Tests the Proxool datasources.
 *
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: chr32 $ (current maintainer)
 * @version $Revision: 1.4 $, $Date: 2004/03/15 23:56:33 $
 * @since Proxool 0.9
 */
public class DataSourceTest extends AbstractProxoolTest {
    public DataSourceTest(String alias) {
        super(alias);
    }

    /**
     * Test the Proxool managed DataSource
     */
    public void testPreconfiguredDatasource() throws Exception {
        String alias = "managedDatasourceTest";
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
    }

    /**
     * Test the exernally manged DataSource.
     */
    public void testDatasource() throws Exception {
        String alias = "datasourceTest";
        String jndiName = "jdbc/J2EETestDB";

        // pretend to be a J2EE server that instantites the data source
        // populates its properties and binds it to JNDI
        ProxoolDataSource dataSource = new ProxoolDataSource();
        dataSource.setAlias(alias);
        dataSource.setDriver(TestConstants.HYPERSONIC_DRIVER);
        dataSource.setUrl(TestConstants.HYPERSONIC_TEST_URL);
        dataSource.setUser(TestConstants.HYPERSONIC_USER);
        dataSource.setPassword(TestConstants.HYPERSONIC_PASSWORD);

        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, MemoryContextFactory.class.getName());
        env.put(Context.URL_PKG_PREFIXES, "tyrex.naming");
        env.put(Context.PROVIDER_URL, alias);
        Context context = new InitialContext(env);
        context.createSubcontext("jdbc");
        context.bind(jndiName, dataSource);
        // end J2EE server

        // now... pretend to be a client.
        DataSource clientDataSource = (DataSource) context.lookup(jndiName);
        assertNotNull("Connection was null.", clientDataSource.getConnection());
        ProxoolFacade.removeConnectionPool(alias);
    }
}


/*
 Revision history:
 $Log: DataSourceTest.java,v $
 Revision 1.4  2004/03/15 23:56:33  chr32
 Added test for ProxoolDataSource.

 Revision 1.3  2004/03/15 02:46:09  chr32
 Added initial DataSourceTest.

 */
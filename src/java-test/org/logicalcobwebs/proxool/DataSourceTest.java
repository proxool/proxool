/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.sql.SQLException;

/**
 * Test for {@link org.logicalcobwebs.proxool.BasicDataSource}
 * @version $Revision: 1.1 $, $Date: 2003/04/19 13:01:01 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class DataSourceTest extends AbstractProxoolTest {

    public DataSourceTest(String alias) {
        super(alias);
    }

    public void testDirectRegistration() throws SQLException, ProxoolException {

        String alias = "directRegistration";

        // Use BasicDataSource directly
        BasicDataSource bds = new BasicDataSource();
        bds.setAlias(alias);
        bds.setUrl(TestConstants.HYPERSONIC_TEST_URL);
        bds.setDriver(TestConstants.HYPERSONIC_DRIVER);
        bds.setUser(TestConstants.HYPERSONIC_USER);
        bds.setPassword(TestConstants.HYPERSONIC_PASSWORD);
        bds.getConnection().close();
        assertEquals("servedCount", 1, ProxoolFacade.getSnapshot(alias).getServedCount());

    }

}

/*
 Revision history:
 $Log: 
 */
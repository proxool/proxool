/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.Hashtable;

/**
 * Test for {@link org.logicalcobwebs.proxool.BasicDataSource}
 * @version $Revision: 1.4 $, $Date: 2003/08/27 18:03:35 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class DataSourceTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(DataSourceTest.class);

    private static final String JNDI_NAME = "java:comp/env/jdbc/myDB";

    private Context context;

    /**
     * @see AbstractProxoolTest#AbstractProxoolTest
     */
    public DataSourceTest(String alias) {
        super(alias);
    }

    protected void setUp() throws Exception {
        super.setUp();

        Hashtable jndiEnvironment = new Hashtable();
/*
        jndiEnvironment.put(InitialContext.INITIAL_CONTEXT_FACTORY, org.shiftone.ooc.InitialContextFactoryImpl.class.getName());
*/
        context = new InitialContext(jndiEnvironment);
        LOG.debug("Found context " + context.getClass().getName());
    }

    /**
     * Test to see whether we can lookup the DataSource and get
     * a connection from it
     * @throws SQLException if there was a problem with  the connection
     * @throws NamingException if we couldn't find the DataSource
     */
    public void testDataSourceLookup() throws SQLException, NamingException {

        DataSource ds = (DataSource) context.lookup(JNDI_NAME);
        Connection connection = ds.getConnection();
        connection.close();

    }

}

/*
 Revision history:
 $Log:
 */
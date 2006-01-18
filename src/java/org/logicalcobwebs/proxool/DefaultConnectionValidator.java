/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.sql.Connection;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Bertrand Renuart
 */
public class DefaultConnectionValidator implements ConnectionValidatorIF {

    /**
     * 
     */
    public DefaultConnectionValidator() {
        super();
    }

    //
    // -- ConnectionValidatorIF interface implementation ----------------------
    //
    
    /* (non-Javadoc)
     * @see org.logicalcobwebs.proxool.ConnectionValidatorIF#validate(org.logicalcobwebs.proxool.ConnectionPoolDefinition, java.sql.Connection)
     */
    public boolean validate(ConnectionPoolDefinitionIF cpd, Connection connection) {
        // make sure a test SQL is defined
        //
        final String testSql = cpd.getHouseKeepingTestSql();
        if (testSql == null || (testSql.length() == 0)) {
            Log log = getPoolLog(cpd.getAlias());
            log.warn("Connection validation requested but house-keeping-test-sql not defined");
            return false;
        }
        
        
        // execute the test statement
        //
        Statement st = null;
        try {
            st = connection.createStatement();
            st.execute(testSql);

            return true;
        } 
        catch (Throwable t) {
            // got an exception while executing the test statement
            // log the problem and return false
            Log log = getPoolLog(cpd.getAlias());
            if(log.isDebugEnabled())
                log.debug("A connection failed the validation test with error: "+t);
            
            return false;
        } 
        finally {
            if (st != null) {
                try {
                    st.close();
                } catch (Throwable t) {
                    // Ignore
                    return false;
                }
            }
        }
    }


	/**
	 * 
	 * @param poolAlias
	 * @return
	 */
	private Log getPoolLog(String poolAlias) {
	    return LogFactory.getLog("org.logicalcobwebs.proxool." + poolAlias);
	}
}

/*
 Revision history:
 $Log: DefaultConnectionValidator.java,v $
 Revision 1.2  2006/01/18 14:40:01  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.1  2004/03/25 22:02:15  brenuart
 First step towards pluggable ConnectionBuilderIF & ConnectionValidatorIF.
 Include some minor refactoring that lead to deprecation of some PrototyperController methods.

 */

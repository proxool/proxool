/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author Bertrand Renuart
 *
 */
public class DefaultConnectionBuilder implements ConnectionBuilderIF {

    /**
     * 
     */
    public DefaultConnectionBuilder() {
        super();
    }

    
    //
    // -- ConnectionBuilderIF interface implementation ----------------------
    //
    
    /* (non-Javadoc)
     * @see org.logicalcobwebs.proxool.ConnectionBuilderIF#buildConnection(org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF)
     */
    public Connection buildConnection(ConnectionPoolDefinitionIF cpd) throws SQLException {
        Connection realConnection = null;
        final String url = cpd.getUrl();

        Properties info = cpd.getDelegateProperties();
        return DriverManager.getConnection(url, info);
    }

}

/*
 Revision history:
 $Log: DefaultConnectionBuilder.java,v $
 Revision 1.1  2004/03/25 22:02:15  brenuart
 First step towards pluggable ConnectionBuilderIF & ConnectionValidatorIF.
 Include some minor refactoring that lead to deprecation of some PrototyperController methods.

 */

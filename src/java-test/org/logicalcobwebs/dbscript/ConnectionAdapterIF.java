/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.dbscript;

import java.util.Properties;
import java.sql.SQLException;
import java.sql.Connection;

/**
 * <link rel="stylesheet" href="{@docRoot}/cg.css" type="text/css">
 *
 * TODO
 *
 * @version $Revision: 1.2 $, $Date: 2002/11/02 12:46:42 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since GSI 5.0
 */
public interface ConnectionAdapterIF {

    void setup(String driver, String url, Properties info) throws SQLException;

    Connection getConnection()
            throws SQLException;

    void closeConnection(Connection connection) throws SQLException;

    void teardown() throws SQLException;

    String getName();

}

/*
 Revision history:
 $Log: ConnectionAdapterIF.java,v $
 Revision 1.2  2002/11/02 12:46:42  billhorsman
 improved debug

 Revision 1.1  2002/11/02 11:29:53  billhorsman
 new script runner for testing

*/

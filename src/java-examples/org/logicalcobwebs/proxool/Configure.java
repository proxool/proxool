/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Example showing you how to configure the pool behaviour if you don't like the default.
 * @version $Revision: 1.1 $, $Date: 2002/09/13 08:14:26 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class Configure {

    /**
     * Configures a pool
     * @param args
     */
    public static void main(String[] args) {

        Connection connection = null;
        try {
            Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");

            Properties properties = new Properties();
            properties.setProperty("maximum-connection-count", "20");
            properties.setProperty("house-keeping-test-sql", "select CURRENT_DATE");
            /* Get the connection. The URL format is:
               proxool:delegate-class:delegate-url
               where:
                 delegare-class = org.gjt.mm.mysql.Driver
                 delegate-url = jdbc:mysql://localhost/test
            */
            connection = DriverManager.getConnection("proxool:org.gjt.mm.mysql.Driver:jdbc:mysql://localhost/test", properties);

            if (connection != null) {
                System.out.println("Got connection :)");
            } else {
                System.out.println("No connection :(");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    // This doesn't really close the connection. It just makes it
                    // available in the pool again.
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

}

/*
 Revision history:
 $Log: Configure.java,v $
 Revision 1.1  2002/09/13 08:14:26  billhorsman
 Initial revision

 Revision 1.4  2002/07/10 16:14:47  billhorsman
 widespread layout changes and move constants into ProxoolConstants

 Revision 1.3  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.2  2002/06/28 11:19:47  billhorsman
 improved doc

*/

/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.sql.SQLException;
import java.sql.Connection;

/**
 * Supports {@link InjectableInterfaceTest}
 * @author <a href="mailto:bill@logicalcobwebs.co.uk">Bill Horsman</a>
 * @author $Author: billhorsman $ (current maintainer)
 * @version $Revision: 1.1 $, $Date: 2004/06/02 20:59:52 $
 * @since 0.9.0
 */
public interface HsqlConnectionIF extends Connection {

    void checkClosed() throws SQLException;

}

/*
 Revision history:
 $Log: HsqlConnectionIF.java,v $
 Revision 1.1  2004/06/02 20:59:52  billhorsman
 New injectable interface tests

*/
/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.util.Properties;

/**
 * Listener for global Proxool events.
 * @version $Revision: 1.1 $, $Date: 2003/02/24 01:15:05 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: chr32 $ (current maintainer)
 * @since Proxool 0.8
 */
public interface ProxoolListenerIF {

    /**
     * Notify that a new connection pool has been registered. Gets called *after* the registered.
     * @param connectionPoolDefinition the new definition.
     * @param completeInfo the properties that could be used to create this definition.
     */
    void onRegistration(ConnectionPoolDefinitionIF connectionPoolDefinition, Properties completeInfo);

    /**
     * Notify that a connection pool will be shutdown. Gets called just *before*
     * the pool is shut down.
     * @param alias the alias of the pool about to be shut down.
     */
    void onShutdown(String alias);

}

/*
 Revision history:
 $Log: ProxoolListenerIF.java,v $
 Revision 1.1  2003/02/24 01:15:05  chr32
 Init rev.

*/
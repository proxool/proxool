/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.proxool.util.AbstractListenerContainer;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.util.Properties;
import java.util.Iterator;

/**
 * A {@link ProxoolListenerIF} that keeps a list of <code>ProxoolListenerIF</code>s
 * and notifies them in a thread safe manner.
 * It also implements {@link org.logicalcobwebs.proxool.util.ListenerContainerIF ListenerContainerIF}
 * which provides methods for
 * {@link org.logicalcobwebs.proxool.util.ListenerContainerIF#addListener(Object) adding} and
 * {@link org.logicalcobwebs.proxool.util.ListenerContainerIF#removeListener(Object) removing} listeners.
 * @version $Revision: 1.2 $, $Date: 2003/02/26 16:05:52 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class CompositeProxoolListener extends AbstractListenerContainer implements ProxoolListenerIF {
    static final Log LOG = LogFactory.getLog(CompositeProxoolListener.class);

    /**
     * @see ProxoolListenerIF#onRegistration(ConnectionPoolDefinitionIF, Properties)
     */
    public void onRegistration(ConnectionPoolDefinitionIF connectionPoolDefinition,
                                 Properties completeInfo) {
        Iterator listenerIterator = null;
        try {
            listenerIterator = getListenerIterator();
            if (listenerIterator != null) {
                ProxoolListenerIF proxoolListener = null;
                while (listenerIterator.hasNext()) {
                    proxoolListener = (ProxoolListenerIF) listenerIterator.next();
                    proxoolListener.onRegistration(connectionPoolDefinition, (Properties) completeInfo.clone());
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Tried to aquire read lock for " + ProxoolListenerIF.class.getName()
                    + " iterator but was interrupted.");
        } finally {
            releaseReadLock();
        }
    }

    /**
     * @see ProxoolListenerIF#onShutdown(String)
     */
    public void onShutdown(String alias) {
        Iterator listenerIterator = null;
        try {
            listenerIterator = getListenerIterator();
            if (listenerIterator != null) {
                ProxoolListenerIF proxoolListener = null;
                while (listenerIterator.hasNext()) {
                    proxoolListener = (ProxoolListenerIF) listenerIterator.next();
                    proxoolListener.onShutdown(alias);
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Tried to aquire read lock for " + ProxoolListenerIF.class.getName()
                    + " iterator but was interrupted.");
        } finally {
            releaseReadLock();
        }
    }
}

/*
 Revision history:
 $Log: CompositeProxoolListener.java,v $
 Revision 1.2  2003/02/26 16:05:52  billhorsman
 widespread changes caused by refactoring the way we
 update and redefine pool definitions.

 Revision 1.1  2003/02/24 01:15:05  chr32
 Init rev.


*/
/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.util.AbstractListenerContainer;

/**
 * A {@link ProxoolListenerIF} that keeps a list of <code>ProxoolListenerIF</code>s
 * and notifies them in a thread safe manner.
 * It also implements {@link org.logicalcobwebs.proxool.util.ListenerContainerIF ListenerContainerIF}
 * which provides methods for
 * {@link org.logicalcobwebs.proxool.util.ListenerContainerIF#addListener(Object) adding} and
 * {@link org.logicalcobwebs.proxool.util.ListenerContainerIF#removeListener(Object) removing} listeners.
 * 
 * @version $Revision: 1.6 $, $Date: 2006/01/18 14:40:01 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class CompositeProxoolListener extends AbstractListenerContainer implements ProxoolListenerIF {
    static final Log LOG = LogFactory.getLog(CompositeProxoolListener.class);

    /**
     * @see ProxoolListenerIF#onRegistration(ConnectionPoolDefinitionIF, Properties)
     */
    public void onRegistration( ConnectionPoolDefinitionIF connectionPoolDefinition,
                                Properties 				   completeInfo) 
    {
        Object[] listeners = getListeners();
        
        for(int i=0; i<listeners.length; i++) {
            try {
                ProxoolListenerIF proxoolListener = (ProxoolListenerIF) listeners[i];
                proxoolListener.onRegistration(connectionPoolDefinition, (Properties) completeInfo.clone());
            }
            catch (RuntimeException re) {
                LOG.warn("RuntimeException received from listener "+listeners[i]+" when dispatching onRegistration event", re);
            }
        }
    }

    /**
     * @see ProxoolListenerIF#onShutdown(String)
     */
    public void onShutdown(String alias) 
    {
        Object[] listeners = getListeners();
        
        for(int i=0; i<listeners.length; i++) {
            try {
                ProxoolListenerIF proxoolListener = (ProxoolListenerIF) listeners[i];
                proxoolListener.onShutdown(alias);
            }
            catch (RuntimeException re) {
                LOG.warn("RuntimeException received from listener "+listeners[i]+" when dispatching onShutdown event", re);
            }
        }
    }
}

/*
 Revision history:
 $Log: CompositeProxoolListener.java,v $
 Revision 1.6  2006/01/18 14:40:01  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.5  2004/03/16 08:48:32  brenuart
 Changes in the AbstractListenerContainer:
 - provide more efficient concurrent handling;
 - better handling of RuntimeException thrown by external listeners.

 Revision 1.4  2003/03/10 15:26:44  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.3  2003/03/03 11:11:56  billhorsman
 fixed licence

 Revision 1.2  2003/02/26 16:05:52  billhorsman
 widespread changes caused by refactoring the way we
 update and redefine pool definitions.

 Revision 1.1  2003/02/24 01:15:05  chr32
 Init rev.


*/
/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.proxool.util.AbstractListenerContainer;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.util.Iterator;

/**
 * A {@link StateListenerIF} that keeps a list of <code>StateListenerIF</code>s
 * and notifies them in a thread safe manner.
 * It also implements {@link org.logicalcobwebs.proxool.util.ListenerContainerIF ListenerContainerIF}
 * which provides methods for
 * {@link org.logicalcobwebs.proxool.util.ListenerContainerIF#addListener(Object) adding} and
 * {@link org.logicalcobwebs.proxool.util.ListenerContainerIF#removeListener(Object) removing} listeners.
 * @version $Revision: 1.1 $, $Date: 2003/02/07 01:47:17 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: chr32 $ (current maintainer)
 * @since Proxool 0.7
 */
public class CompositeStateListener extends AbstractListenerContainer implements StateListenerIF{
    static final Log LOG = LogFactory.getLog(CompositeStateListener.class);

    /**
     * @see StateListenerIF#upStateChanged(int)
     */
    public void upStateChanged(int upState) {
        Iterator listenerIterator = null;
        try {
            listenerIterator = getListenerIterator();
            if (listenerIterator != null) {
                StateListenerIF stateListener = null;
                while (listenerIterator.hasNext()) {
                    stateListener = (StateListenerIF) listenerIterator.next();
                    stateListener.upStateChanged(upState);
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Tried to aquire read lock for " + StateListenerIF.class.getName()
                + " iterator but was interrupted.");
        } finally {
            releaseReadLock();
        }
    }
}

/*
 Revision history:
 $Log: CompositeStateListener.java,v $
 Revision 1.1  2003/02/07 01:47:17  chr32
 Initial revition.

*/
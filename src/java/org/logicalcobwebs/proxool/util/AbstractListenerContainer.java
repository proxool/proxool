/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.util;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Implementation of {@link ListenerContainerIF} that uses a reads/write lock to handle concurrency in a safe and
 * fast way.
 * <p>
 * The registered listeners are offered to subclasses through the protected {@link #getListenerIterator} method. This
 * method aquires a read lock before it returns the iterator, but <b>it is the subclass responsibility to
 * release this lock by calling the {@link #releaseReadLock} method</b>. Failing to do this will prevent any more
 * listeneres to be added or removed. Your code sould look like this:
 * <code>
 * <pre>
 *
     Iterator listenerIterator = null;
     try {
         listenerIterator = getListenerIterator();
         if (listenerIterator != null) {
            // ... Iterate through the listeners and notify them
         }
     } catch (InterruptedException e) {
         LOG.error("Tried to aquire read lock for " + MyClass.class.getName()
             + " iterator but was interrupted.");
     } finally {
         releaseReadLock();
     }
 </pre>
 </code>
 * </p>
 * @version $Revision: 1.4 $, $Date: 2003/02/19 19:35:21 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: chr32 $ (current maintainer)
 * @since Proxool 0.7
 */
public abstract class AbstractListenerContainer implements ListenerContainerIF {
    static final Log LOG = LogFactory.getLog(AbstractListenerContainer.class);
    private List listeners;
    private ReadWriteLock readWriteLock = new ReadWriteLock();

    /**
     * @see ListenerContainerIF#addListener(Object)
     */
    public void addListener(Object listener) {
        if (listener == null) {
            return;
        }
        try {
            readWriteLock.aquireWrite();
            if (this.listeners == null) {
                this.listeners = new ArrayList(3);
            }
            this.listeners.add(listener);
        } catch (InterruptedException e) {
            LOG.error("Tried to aquire write lock for storing " + listener.getClass().getName() + " but was interrupted.");
        } finally {
            readWriteLock.release();
        }
    }

    /**
     * @see ListenerContainerIF#removeListener(Object)
     */
    public boolean removeListener(Object listener) {
        if (listener == null || isEmpty()) {
            return false;
        } else {
            boolean listenerRemoved = false;
            try {
                this.readWriteLock.aquireRead();
                listenerRemoved = this.listeners.remove(listener);
            } catch (InterruptedException e) {
                LOG.error("Tried to aquire write lock for removing " + listener.getClass().getName() + " but was interrupted.");
            } finally {
                this.readWriteLock.release();
            }
            return listenerRemoved;
        }
    }

    /**
     * Get an iterator containing the listeners in this container. Will return <code>null</code>
     * if this container is empty.
     * <p><b>Important:</b> See class documentation regarding releasing of read lock.</p>
     * @return an iterator containing the listeners in this container or <code>null</code> if it is empty.
     * @throws InterruptedException if the read lock can't be obtained.
     */
    protected Iterator getListenerIterator() throws InterruptedException {
        this.readWriteLock.aquireRead();
        if (!isEmpty()) {
            return this.listeners.iterator();
        } else {
            return null;
        }
    }

    /**
     * Release the read lock aquired by the {@link #getListenerIterator()} method.
     */
    protected void releaseReadLock() {
        this.readWriteLock.release();
    }

    /**
     * @see ListenerContainerIF#isEmpty()
     */
    public boolean isEmpty() {
        return this.listeners == null || this.listeners.size() < 1;
    }
}

/*
 Revision history:
 $Log: AbstractListenerContainer.java,v $
 Revision 1.4  2003/02/19 19:35:21  chr32
 Formated code in javadoc.

 Revision 1.3  2003/02/07 17:20:18  billhorsman
 checkstyle

 Revision 1.2  2003/02/07 15:06:43  billhorsman
 fixed isEmpty bug

 Revision 1.1  2003/02/07 01:46:31  chr32
 Initial revition.

*/
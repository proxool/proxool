/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.logicalcobwebs.proxool.util.AbstractListenerContainer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * A {@link ConnectionListenerIF} that keeps a list of <code>ConnectionListenerIF</code>s
 * and notifies them in a thread safe manner.
 * It also implements {@link org.logicalcobwebs.proxool.util.ListenerContainerIF ListenerContainerIF}
 * which provides methods for
 * {@link org.logicalcobwebs.proxool.util.ListenerContainerIF#addListener(Object) adding} and
 * {@link org.logicalcobwebs.proxool.util.ListenerContainerIF#removeListener(Object) removing} listeners.
 * @version $Revision: 1.4 $, $Date: 2003/03/10 15:26:43 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class CompositeConnectionListener extends AbstractListenerContainer implements ConnectionListenerIF {
    static final Log LOG = LogFactory.getLog(CompositeConnectionListener.class);

    /**
     * @see ConnectionListenerIF#onBirth(Connection)
     */
    public void onBirth(Connection connection) throws SQLException {
        Iterator listenerIterator = null;
        try {
            listenerIterator = getListenerIterator();
            if (listenerIterator != null) {
                ConnectionListenerIF connectionListener = null;
                while (listenerIterator.hasNext()) {
                    connectionListener = (ConnectionListenerIF) listenerIterator.next();
                    connectionListener.onBirth(connection);
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Tried to aquire read lock for " + ConnectionListenerIF.class.getName()
                    + " iterator but was interrupted.");
        } finally {
            releaseReadLock();
        }
    }

    /**
     * @see ConnectionListenerIF#onDeath(Connection)
     */
    public void onDeath(Connection connection) throws SQLException {
        Iterator listenerIterator = null;
        try {
            listenerIterator = getListenerIterator();
            if (listenerIterator != null) {
                ConnectionListenerIF connectionListener = null;
                while (listenerIterator.hasNext()) {
                    connectionListener = (ConnectionListenerIF) listenerIterator.next();
                    connectionListener.onDeath(connection);
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Tried to aquire read lock for " + ConnectionListenerIF.class.getName()
                    + " iterator but was interrupted.");
        } finally {
            releaseReadLock();
        }
    }

    /**
     * @see ConnectionListenerIF#onExecute(String, long)
     */
    public void onExecute(String command, long elapsedTime) {
        Iterator listenerIterator = null;
        try {
            listenerIterator = getListenerIterator();
            if (listenerIterator != null) {
                ConnectionListenerIF connectionListener = null;
                while (listenerIterator.hasNext()) {
                    connectionListener = (ConnectionListenerIF) listenerIterator.next();
                    connectionListener.onExecute(command, elapsedTime);
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Tried to aquire read lock for " + ConnectionListenerIF.class.getName()
                    + " iterator but was interrupted.");
        } finally {
            releaseReadLock();
        }
    }

    /**
     * @see ConnectionListenerIF#onFail(String, Exception)
     */
    public void onFail(String command, Exception exception) {
        Iterator listenerIterator = null;
        try {
            listenerIterator = getListenerIterator();
            if (listenerIterator != null) {
                ConnectionListenerIF connectionListener = null;
                while (listenerIterator.hasNext()) {
                    connectionListener = (ConnectionListenerIF) listenerIterator.next();
                    connectionListener.onFail(command, exception);
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Tried to aquire read lock for " + ConnectionListenerIF.class.getName()
                    + " iterator but was interrupted.");
        } finally {
            releaseReadLock();
        }
    }
}

/*
 Revision history:
 $Log: CompositeConnectionListener.java,v $
 Revision 1.4  2003/03/10 15:26:43  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.3  2003/03/03 11:11:56  billhorsman
 fixed licence

 Revision 1.2  2003/02/07 17:20:16  billhorsman
 checkstyle

 Revision 1.1  2003/02/07 01:47:17  chr32
 Initial revition.

*/
/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.util.AbstractListenerContainer;

/**
 * A {@link ConnectionListenerIF} that keeps a list of <code>ConnectionListenerIF</code>s
 * and notifies them in a thread safe manner.
 * It also implements {@link org.logicalcobwebs.proxool.util.ListenerContainerIF ListenerContainerIF}
 * which provides methods for
 * {@link org.logicalcobwebs.proxool.util.ListenerContainerIF#addListener(Object) adding} and
 * {@link org.logicalcobwebs.proxool.util.ListenerContainerIF#removeListener(Object) removing} listeners.
 * 
 * @version $Revision: 1.6 $, $Date: 2006/01/18 14:40:01 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class CompositeConnectionListener extends AbstractListenerContainer implements ConnectionListenerIF {
    static final Log LOG = LogFactory.getLog(CompositeConnectionListener.class);

    /**
     * @see ConnectionListenerIF#onBirth(Connection)
     */
    public void onBirth(Connection connection) throws SQLException 
    {
        Object[] listeners = getListeners();
        
        for(int i=0; i<listeners.length; i++) {
            try {
                ConnectionListenerIF connectionListener = (ConnectionListenerIF) listeners[i];
                connectionListener.onBirth(connection);
            }
            catch (RuntimeException re) {
                LOG.warn("RuntimeException received from listener "+listeners[i]+" when dispatching onBirth event", re);
            }
            catch(SQLException se) {
                LOG.warn("SQLException received from listener "+listeners[i]+" when dispatching onBirth event - event dispatching cancelled");
                throw se;
            }
        }
    }

    /**
     * @see ConnectionListenerIF#onDeath(Connection)
     */
    public void onDeath(Connection connection) throws SQLException 
    {
        Object[] listeners = getListeners();
        
        for(int i=0; i<listeners.length; i++) {
            try {
                ConnectionListenerIF connectionListener = (ConnectionListenerIF) listeners[i];
                connectionListener.onDeath(connection);
            }
            catch (RuntimeException re) {
                LOG.warn("RuntimeException received from listener "+listeners[i]+" when dispatching onDeath event", re);
            }
            catch(SQLException se) {
                LOG.warn("SQLException received from listener "+listeners[i]+" when dispatching onDeath event - event dispatching cancelled");
                throw se;
            }
        }
    }

    /**
     * @see ConnectionListenerIF#onExecute(String, long)
     */
    public void onExecute(String command, long elapsedTime) 
    {
        Object[] listeners = getListeners();
        
        for(int i=0; i<listeners.length; i++) {
            try {
                ConnectionListenerIF connectionListener = (ConnectionListenerIF) listeners[i];
                connectionListener.onExecute(command, elapsedTime);
            }
            catch (RuntimeException re) {
                LOG.warn("RuntimeException received from listener "+listeners[i]+" when dispatching onExecute event", re);
            }
        }
    }

    /**
     * @see ConnectionListenerIF#onFail(String, Exception)
     */
    public void onFail(String command, Exception exception) 
    {
        Object[] listeners = getListeners();
        
        for(int i=0; i<listeners.length; i++) {
            try {
                ConnectionListenerIF connectionListener = (ConnectionListenerIF) listeners[i];
                connectionListener.onFail(command, exception);
            }
            catch (RuntimeException re) {
                LOG.warn("RuntimeException received from listener "+listeners[i]+" when dispatching onFail event", re);
            }
        }
    }
}

/*
 Revision history:
 $Log: CompositeConnectionListener.java,v $
 Revision 1.6  2006/01/18 14:40:01  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.5  2004/03/16 08:48:32  brenuart
 Changes in the AbstractListenerContainer:
 - provide more efficient concurrent handling;
 - better handling of RuntimeException thrown by external listeners.

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
/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.util;

/**
 * A container for event listeners. Implementations should cater to only one type of listeners.
 * @version $Revision: 1.1 $, $Date: 2003/02/07 01:46:31 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: chr32 $ (current maintainer)
 * @since Proxool 0.7
 */
public interface ListenerContainerIF {
    /**
     * Add a listener to this container.
     * @param listener the listener to add.
     */
    void addListener(Object listener);

    /**
     * Remove a listener from this container.
     * @param listener the listener to be removed.
     * @return wether the listnener was found and removed or not.
     */
    boolean removeListener(Object listener);

    /**
     * Get wether this container is empty or not.
     * @return wether this container is empty or not.
     */
    boolean isEmpty() ;
}

/*
 Revision history:
 $Log: ListenerContainerIF.java,v $
 Revision 1.1  2003/02/07 01:46:31  chr32
 Initial revition.

*/
/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.util;


/**
 * Implementation of {@link ListenerContainerIF} that uses a reads/write lock to handle concurrency in a safe and
 * fast way.
 * <p>
 * The registered listeners are offered to subclasses through the protected {@link #getListeners} method. This
 * method returns a reference to an array containing the registered listeners. A new array holding the listeners
 * is created everytime a modification on the registration list is required (add/remove listener). Therefore, 
 * subclasses can safely iterate over the received array. 
 * 
 * Your code sould look like this:
 * <code>
 * <pre>
     Object[] listeners = getListeners();
     for(int i=0; i<listeners.length; i++) {
         // do something
     }
 </pre>
 </code>
 * </p>
 * 
 * @version $Revision: 1.8 $, $Date: 2004/03/16 08:48:33 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: brenuart $ (current maintainer)
 * @since Proxool 0.7
 */
public abstract class AbstractListenerContainer implements ListenerContainerIF {
        
    private Object[] listeners = EMPTY_LISTENERS;
    private static final Object[] EMPTY_LISTENERS = new Object[]{};

    
    /**
     * @see ListenerContainerIF#addListener(Object)
     */
    public synchronized void addListener(Object listener) 
    {
        if(listener==null)
            throw new NullPointerException("Unexpected NULL listener argument received");
        
        // create a new array
        Object[] newListeners = new Object[listeners.length+1];
        
        // copy listeners currently registered
        System.arraycopy(listeners, 0, newListeners, 0, listeners.length);
        
        // add the new one
        newListeners[listeners.length] = listener;
        
        // commit changes
        listeners = newListeners;
    }
    
    
    /**
     * @see ListenerContainerIF#removeListener(Object)
     */
    public synchronized boolean removeListener(Object listener) 
    {
        if(listener==null)
            throw new NullPointerException("Unexpected NULL listener argument received");
        
        // find listener to remove in the list
        int index=-1;
        for(int i=0; i<listeners.length; i++) {
            if( listeners[i]==listener ) {
                index = i;
                break;
            }
        }
        
        // not found ?
        if( index==-1 )
            return false;
        
        // create a new array of the right size
        Object[] newListeners = new Object[listeners.length-1];
        
        // copy registered listeners minus the one to remove
        if( index > 0 )
            System.arraycopy(listeners, 0, newListeners, 0, index);
        
        if( index < listeners.length-1 )
            System.arraycopy(listeners, index+1, newListeners, index, listeners.length-index-1);
        
        // commit
        listeners = newListeners;
        return true;
    }

    
    /**
     * Get a reference to the array of registered listeners. 
     * 
     * @return reference to the array containing registered listeners (always not NULL)
     */
    protected Object[] getListeners() {
        return listeners;
    }


    /**
     * @see ListenerContainerIF#isEmpty()
     */
    public boolean isEmpty() {
        return listeners.length==0;
    }
}

/*
 Revision history:
 $Log: AbstractListenerContainer.java,v $
 Revision 1.8  2004/03/16 08:48:33  brenuart
 Changes in the AbstractListenerContainer:
 - provide more efficient concurrent handling;
 - better handling of RuntimeException thrown by external listeners.

 Revision 1.7  2003/03/11 00:12:11  billhorsman
 switch to concurrent package

 Revision 1.6  2003/03/10 15:26:55  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.5  2003/03/03 11:12:01  billhorsman
 fixed licence

 Revision 1.4  2003/02/19 19:35:21  chr32
 Formated code in javadoc.

 Revision 1.3  2003/02/07 17:20:18  billhorsman
 checkstyle

 Revision 1.2  2003/02/07 15:06:43  billhorsman
 fixed isEmpty bug

 Revision 1.1  2003/02/07 01:46:31  chr32
 Initial revition.

*/
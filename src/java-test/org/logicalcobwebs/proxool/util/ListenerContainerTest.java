/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.util;

import junit.framework.TestCase;
import org.logicalcobwebs.proxool.GlobalTest;
import org.logicalcobwebs.proxool.AbstractProxoolTest;

import java.util.Iterator;

/**
 * Test {@link AbstractListenerContainer}.
 *
 * @version $Revision: 1.3 $, $Date: 2003/03/03 17:09:18 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class ListenerContainerTest extends AbstractProxoolTest {

    /**
     * @see TestCase#TestCase
     */
    public ListenerContainerTest(String name) {
        super(name);
    }

    /**
     * Test that added listeners get notified.
     */
    public void testAdd() {
        CompositeTestListener compositeTestListener = new CompositeTestListener();
        for (int i = 0; i < 10; ++i) {
            compositeTestListener.addListener(new TestListener());
        }
        compositeTestListener.onEvent();
        assertTrue("Only got " + compositeTestListener.getNumberOfNotifications()
            + " notifications but expected 10.", compositeTestListener.getNumberOfNotifications() == 10);
    }

    /**
     * Test that removed listeners are not notified, and that the remove method
     * returns <code>false</code> when trying to removed an unregistered listener.
     */
    public void testRemove() {
        TestListenerIF[] testListeners = new TestListenerIF[]{
            new TestListener(), new TestListener(), new TestListener(), new TestListener(),
            new TestListener(), new TestListener(), new TestListener(), new TestListener(),
            new TestListener(), new TestListener()
        };
        CompositeTestListener compositeTestListener = new CompositeTestListener();
        for (int i = 0; i < 10; ++i) {
            compositeTestListener.addListener(testListeners[i]);
        }
        for (int i = 0; i < 10; ++i) {
            assertTrue("Removal of a listener failed.", compositeTestListener.removeListener(testListeners[i]));
        }
        assertTrue("Removal of unregistered listener returned true",
            !compositeTestListener.removeListener(new TestListener()));
        compositeTestListener.onEvent();
        assertTrue("Listeners was notified even if all listeners had been removed.",
            compositeTestListener.getNumberOfNotifications() == 0);
    }

}

interface TestListenerIF {
    void onEvent();
}

class CompositeTestListener extends AbstractListenerContainer implements TestListenerIF {
    private int numberOfNotifications;
    public void onEvent () {
        try {
            Iterator listeners = getListenerIterator();
            if (listeners != null) {
                TestListenerIF testListener = null;
                while (listeners.hasNext()) {
                    testListener = (TestListenerIF) listeners.next();
                    notification();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            releaseReadLock();
        }
    }
    int getNumberOfNotifications() {
        return numberOfNotifications;
    }
    private synchronized void notification() {
        numberOfNotifications++;
    }
}

class TestListener implements TestListenerIF {
    public void onEvent () {
    }
}
/*
 Revision history:
 $Log: ListenerContainerTest.java,v $
 Revision 1.3  2003/03/03 17:09:18  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.2  2003/03/03 11:12:07  billhorsman
 fixed licence

 Revision 1.1  2003/02/10 00:14:33  chr32
 Added tests for AbstractListenerContainer.

*/

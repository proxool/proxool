/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.logging;

import org.logicalcobwebs.proxool.AbstractProxoolTest;

/**
 * Test {@link org.logicalcobwebs.logging.impl.Jdk14Logger}
 *
 * @version $Revision: 1.3 $, $Date: 2003/11/04 13:54:02 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class Jdk14LoggerTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(Jdk14LoggerTest.class);

    public Jdk14LoggerTest(String alias) {
        super(alias);
    }

    /**
     * Override {@link AbstractProxoolTest#setUp} because we don't want to
     * configure logging.
     * @throws Exception if anything goes wrong (it can't, we don't do anything)
     */
    protected void setUp() throws Exception {
        // Don't setup logging. Just use the default JDK14Logger
    }

    /**
     * Override {@link AbstractProxoolTest#tearDown} because it does things that rely
     * on {@link AbstractProxoolTest#setUp} having run
     * @throws Exception if anything goes wrong (it can't, we don't do anything)
     */
    protected void tearDown() throws Exception {
        // Don't do anything
    }

    public void testJdk14Logger() {
        LOG.info("Does this log correctly?");
    }

}

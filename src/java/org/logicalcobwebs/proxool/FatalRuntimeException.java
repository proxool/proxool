/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

/**
 * A type of SQLException that has been defined as fatal. It contains
 * the {@link #getCause original} plain Exception
 * just in case you need it.
 * @version $Revision: 1.1 $, $Date: 2003/09/29 17:48:08 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 * @see ConnectionPoolDefinitionIF#getFatalSqlExceptions
 */
public class FatalRuntimeException extends RuntimeException {

    /**
     * @see #getCause
     */
    private Exception cause;

    public FatalRuntimeException(Exception cause) {
        super(cause.getMessage());
        this.cause = cause;
    }

    /**
     * @see Throwable#getCause
     */
    public Throwable getCause() {
        return cause;
    }

}

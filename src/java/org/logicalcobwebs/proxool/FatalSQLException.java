/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.sql.SQLException;

/**
 * A type of SQLException that has been defined as fatal. It contains
 * the {@link #getOriginalSQLException original} plain SQLException
 * just in case you need it.
 * @version $Revision: 1.1 $, $Date: 2003/09/02 19:26:29 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 * @see ConnectionPoolDefinitionIF#getFatalSqlExceptions
 */
public class FatalSQLException extends SQLException {

    /**
     * @see #getOriginalSQLException
     */
    private SQLException cause;

    /**
     * @param cause the SQLException that was detected as being fatal
     * @param reason see {@link super#SQLException(java.lang.String, java.lang.String, int)}
     * @param SQLState see {@link super#SQLException(java.lang.String, java.lang.String, int)}
     * @param vendorCode see {@link super#SQLException(java.lang.String, java.lang.String, int)}
     */
    public FatalSQLException(SQLException cause, String reason, String SQLState, int vendorCode) {
        super(reason, SQLState, vendorCode);
        this.cause = cause;
    }

    /**
     * Same as {@link #getOriginalSQLException}
     * @see Throwable#getCause
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * Get the SQLException that was detected as being fatal
     * @return the original SQLException
     */
    public SQLException getOriginalSQLException() {
        return cause;
    }

}

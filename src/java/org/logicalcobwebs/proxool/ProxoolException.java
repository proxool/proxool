/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
  * Proxool exception class that emulates the behaviour of the new cause
  * facility in jdk 1.4.  It is also known as the <i>chained
  * exception</i> facility, as the cause can, itself, have a cause, and so on,
  * leading to a "chain" of exceptions, each caused by another.
  *
  * <p>A cause can be associated with a throwable in two ways: via a
  * constructor that takes the cause as an argument, or via the
  * {@link #initCause(Throwable)} method.  New throwable classes that
  * wish to allow causes to be associated with them should provide constructors
  * that take a cause and delegate (perhaps indirectly) to one of the
  * <tt>Throwable</tt> constructors that takes a cause.
  *
 * @version $Revision: 1.2 $, $Date: 2003/03/03 11:11:58 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.6
 */
public class ProxoolException extends Exception {
    /**
     * The throwable that caused this ProxoolException to get thrown, or null if this
     * ProxoolException was not caused by another throwable, or if the causative
     * throwable is unknown.
     */
    private Throwable cause = this;

    /**
     * Constructs a new instance with <code>null</code> as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public ProxoolException() {
        super();
    }

    /**
     * Constructs a new instance with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param   message   the detail message. The detail message is saved for
     *          later retrieval by the {@link #getMessage()} method.
     */
    public ProxoolException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance with the specified detail message and cause.
     *
     * <p>Note that the detail message associated with
     * <code>cause</code> is <i>not</i> automatically incorporated in
     * this throwable's detail message.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public ProxoolException(String message, Throwable cause) {
        this(message);
        this.cause = cause;
    }

    /**
     * Constructs a new throwable with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * This constructor is useful for throwables that are little more than
     * wrappers for other throwables.
     *
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public ProxoolException(Throwable cause) {
        this(cause == null ? null : cause.toString());
        this.cause = cause;
    }

    /**
     * Returns the cause of this exception or <code>null</code> if the
     * cause is nonexistent or unknown.  (The cause is the throwable that
     * caused this exception to get thrown.)
     *
     * <p>This implementation returns the cause that was supplied via one of
     * the constructors requiring a <tt>Throwable</tt>, or that was set after
     * creation with the {@link #initCause(Throwable)} method.
     *
     * @return  the cause of this throwable or <code>null</code> if the
     *          cause is nonexistent or unknown.
     */
    public Throwable getCause() {
        return (cause == this ? null : cause);
    }

    /**
     * Initializes the <i>cause</i> of this exception to the specified value.
     * (The cause is the throwable that caused this exception to get thrown.)
     *
     * <p>This method can be called at most once.  It is generally called from
     * within the constructor, or immediately after creating the
     * throwable.  If this throwable was created
     * with {@link #ProxoolException(Throwable)} or
     * {@link #ProxoolException(String,Throwable)}, this method cannot be called
     * even once.
     *
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     * @return  a reference to this <code>ProxoolException</code> instance.
     */
    public synchronized Throwable initCause(Throwable cause) {
        if (this.cause != this) {
            throw new IllegalStateException("Can't overwrite cause");
        }
        if (cause == this) {
            throw new IllegalArgumentException("Self-causation not permitted");
        }
        this.cause = cause;
        return this;
    }

    /**
     * Prints this ProxoolException and its backtrace to the
     * standard error stream.
     *
     * The backtrace for a ProxoolException with an initialized, non-null cause
     * should generally include the backtrace for the cause.
     */
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * Prints this ProxoolException and its backtrace to the specified print stream.
     *
     * @param stream <code>PrintStream</code> to use for output
     */
    public void printStackTrace(PrintStream stream) {
        synchronized (stream) {
            super.printStackTrace(stream);
            Throwable ourCause = getCause();
            if (ourCause != null) {
                stream.println();
                stream.println("Caused by:");
                ourCause.printStackTrace(stream);
            }
        }
    }

    /**
     * Prints this ProxoolException and its backtrace to the specified
     * print writer.
     *
     * @param writer <code>PrintWriter</code> to use for output
     */
    public void printStackTrace(PrintWriter writer) {
        synchronized (writer) {
            super.printStackTrace(writer);
            Throwable ourCause = getCause();
            if (ourCause != null) {
                writer.println();
                writer.println("Caused by:");
                ourCause.printStackTrace(writer);
            }
        }
    }

}

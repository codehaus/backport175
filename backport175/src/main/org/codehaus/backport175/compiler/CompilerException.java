/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Thrown when error in compilation of the annotations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class CompilerException extends RuntimeException {
    /**
     * Original exception which caused this exception.
     */
    private Throwable m_originalException;

    /**
     * Optional location hint
     */
    private Location m_location;

    /**
     * Sets the message for the exception.
     *
     * @param message the message
     */
    public CompilerException(final String message) {
        super(message);
    }

    /**
     * Sets the message and location for the exception.
     *
     * @param message the message
     * @param location
     */
    public CompilerException(final String message, Location location) {
        super(message);
        m_location = location;
    }

    /**
     * Sets the message for the exception and the original exception being wrapped.
     *
     * @param message   the detail of the error message
     * @param throwable the original exception
     */
    public CompilerException(String message, Throwable throwable) {
        super(message);
        m_originalException = throwable;
    }

    /**
     * Print the full stack trace, including the original exception.
     */
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * Print the full stack trace, including the original exception.
     *
     * @param ps the byte stream in which to print the stack trace
     */
    public void printStackTrace(PrintStream ps) {
        super.printStackTrace(ps);
        if (m_originalException != null) {
            m_originalException.printStackTrace(ps);
        }
    }

    /**
     * Print the full stack trace, including the original exception.
     *
     * @param pw the character stream in which to print the stack trace
     */
    public void printStackTrace(PrintWriter pw) {
        super.printStackTrace(pw);
        if (m_originalException != null) {
            m_originalException.printStackTrace(pw);
        }
    }

    public Location getLocation() {
        return m_location;
    }

    /**
     * Error reporting
     */
    public static class Location {
        String className;
        String file;
        int lineNumber;

        public static Location render(String className, String file, int line) {
            Location location = new Location();
            location.className = className;
            location.file = file;
            location.lineNumber = line;
            return location;
        }
    }
}
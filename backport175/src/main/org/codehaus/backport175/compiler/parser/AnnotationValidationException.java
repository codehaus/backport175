/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler.parser;

import org.codehaus.backport175.compiler.javadoc.RawAnnotation;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Thrown when error in the validation of the values of the annotations.
 * Those errors should interrupts the compilation.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AnnotationValidationException extends RuntimeException {
    /**
     * Original exception which caused this exception.
     */
    protected Throwable m_originalException;

    /**
     * Optional location hint
     */
    protected Location m_location;

    /**
     * Sets the message for the exception.
     *
     * @param message the message
     */
    public AnnotationValidationException(final String message) {
        super(message);
    }

    /**
     * Sets the message and location for the exception.
     *
     * @param message the message
     * @param location
     */
    public AnnotationValidationException(final String message, Location location) {
        super(message);
        m_location = location;
    }

    /**
     * Sets the message for the exception and the original exception being wrapped.
     *
     * @param message   the detail of the error message
     * @param throwable the original exception
     */
    public AnnotationValidationException(String message, Throwable throwable) {
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
        public String className;
        public String file;
        public int lineNumber;
        public String annotationClassName;

        public static Location render(RawAnnotation annotation) {
            Location location = new Location();
            location.className = annotation.getEnclosingClassName();
            location.file = annotation.getEnclosingClassFile();
            location.lineNumber = annotation.getLineNumber();
            location.annotationClassName = annotation.getName();
            return location;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(className);
            sb.append(':');
            sb.append(lineNumber);
            sb.append(" @");
            sb.append(annotationClassName);
            return sb.toString();
        }
    }
}
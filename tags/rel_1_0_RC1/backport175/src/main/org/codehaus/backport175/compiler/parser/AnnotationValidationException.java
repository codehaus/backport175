/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler.parser;

import org.codehaus.backport175.compiler.javadoc.RawAnnotation;
import org.codehaus.backport175.compiler.SourceLocation;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Thrown when error in the validation of the values of the annotations.
 * <p/>
 * Those errors should not interrupts the compilation since ties to one single annotation
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AnnotationValidationException extends ParseException {
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
    public AnnotationValidationException(final String message, SourceLocation location) {
        super(message, location);
    }

    /**
     * Sets the message for the exception and the original exception being wrapped.
     *
     * @param message   the detail of the error message
     * @param throwable the original exception
     */
    public AnnotationValidationException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Update the source location
     * @param location
     */
    public void setLocation(SourceLocation location) {
        m_sourceLocation = location;
    }
}
/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler.parser;

import org.codehaus.backport175.compiler.CompilerException;
import org.codehaus.backport175.compiler.SourceLocation;

/**
 * Thrown when error in parsing the annotation expression
 * ie when the JSR-175 checks fails.
 *
 * Those errors should not interrupt the compilation
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class ParseException extends CompilerException {

    /**
     * Sets the message for the exception.
     *
     * @param message the message
     */
    public ParseException(final String message) {
        super(message);
    }

    /**
     * Sets the message for the exception and the original exception being wrapped.
     *
     * @param message   the detail of the error message
     * @param throwable the original exception
     */
    public ParseException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    /**
     *
     * @param message
     * @param throwable
     * @param sourceLocation
     */
    public ParseException(final String message, final Throwable throwable, final SourceLocation sourceLocation) {
        super(message, sourceLocation);
        m_originalException = throwable;
    }

    /**
     *
     * @param message
     * @param sourceLocation
     */
    public ParseException(final String message, final SourceLocation sourceLocation) {
        super(message, sourceLocation);
    }
}
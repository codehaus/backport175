/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler.javadoc;

import org.codehaus.backport175.compiler.CompilerException;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Thrown when error in parsing of the source code.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class SourceParseException extends CompilerException {

    /**
     * Sets the message for the exception.
     *
     * @param message the message
     */
    public SourceParseException(final String message) {
        super(message);
    }

    /**
     * Sets the message for the exception and the original exception being wrapped.
     *
     * @param message   the detail of the error message
     * @param throwable the original exception
     */
    public SourceParseException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler;

/**
 * Handles message reporting.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface MessageHandler {

    /**
     * Handles an information message
     *
     * @param message
     */
    void info(String message);

    /**
     * Handles an error
     *
     * @param exception
     */
    void error(CompilerException exception);

    /**
     * Handles an accepted compiled annotation
     *
     * @param sourceLocation
     */
    void accept(SourceLocation sourceLocation);

    /**
     * Default impl of the MessageHandler interface, prints the messages to standard out.
     */
    public static class PrintWriter implements MessageHandler {

        private boolean m_verbose = false;

        public PrintWriter(final boolean isVerbose) {
            m_verbose = isVerbose;
        }

        public void info(final String message) {
            if (m_verbose) {
                System.out.println("INFO: " + message);
            }
        }

        public void warning(final String message) {
            if (m_verbose) {
                System.out.println("WARNING: " + message);
            }
        }

        public void error(final CompilerException exception) {
            if (exception.getLocation() != null) {
                System.err.println("ERROR: " + exception.getLocation().toString());
            } else {
                System.err.println("ERROR:");
            }
            exception.printStackTrace();
        }

        public void accept(final SourceLocation sourceLocation) {
            ;//not needed
        }
    }
}
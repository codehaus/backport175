/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.ide.intellij;

import com.intellij.openapi.diagnostic.Logger;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class BpLog {

    private static final Logger LOG = Logger.getInstance(BpLog.class.getName());

    /**
     * For debug only / stdout
     * @param message
     */
    public static void logTrace(String message) {
        System.out.println("BP TRACE : " + message);
    }

    /**
     * Errors
     * @param message
     * @param t
     */
    public static void logError(String message, Throwable t) {
        LOG.error(message, t);
    }
}

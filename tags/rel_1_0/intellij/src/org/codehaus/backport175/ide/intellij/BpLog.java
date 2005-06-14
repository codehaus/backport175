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
 * Wraps IDEA Log4J in one place
 *
 * Turn on by adding at the end of IDEA_HOME/bin/log.xml:
 * (+ idea.lax stdout redirect)
 * <category name="org.codehaus.backport175">
 *  <priority value="DEBUG"/>
 *  <appender-ref ref="FILE"/>
 * </category>
*
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class BpLog {

    private static final Logger LOG = Logger.getInstance(BpLog.class.getName());

    /**
     * For debug only / stdout
     * @param message
     */
    public static void info(String message) {
	    LOG.info(message);
    }

    /**
     * Errors
     * @param message
     * @param t
     */
    public static void error(String message, Throwable t) {
        LOG.error(message, t);
    }
}

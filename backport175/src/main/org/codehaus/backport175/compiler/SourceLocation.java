/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler;

import org.codehaus.backport175.compiler.javadoc.RawAnnotation;

/**
 * Source location used for reporting.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class SourceLocation {
    private String className;
    private String file;
    private int lineNumber;
    private String annotationClassName;

    /**
     * Renders a new source location.
     *
     * @param annotation
     * @return
     */
    public static SourceLocation render(final RawAnnotation annotation) {
        SourceLocation sourceLocation = new SourceLocation();
        sourceLocation.className = annotation.getEnclosingClassName();
        sourceLocation.file = annotation.getEnclosingClassFile();
        sourceLocation.lineNumber = annotation.getLineNumber();
        sourceLocation.annotationClassName = annotation.getName();
        return sourceLocation;
    }

    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(className);
        sb.append(':');
        sb.append(lineNumber);
        sb.append(" @");
        sb.append(annotationClassName);
        sb.append(" in source file [");
        sb.append(file);
        sb.append(']');
        return sb.toString();
    }
}
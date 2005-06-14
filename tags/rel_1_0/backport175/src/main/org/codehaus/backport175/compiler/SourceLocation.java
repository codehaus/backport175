/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler;

import org.codehaus.backport175.compiler.javadoc.RawAnnotation;
import com.thoughtworks.qdox.model.DocletTag;

/**
 * Source location used for reporting.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class SourceLocation {
    private String m_className;
    private String m_file;
    private int m_lineNumber;
    private String m_annotationClassName;

    /**
     * Renders a new source location.
     *
     * @param annotation
     * @return
     */
    public static SourceLocation render(final RawAnnotation annotation) {
        SourceLocation sourceLocation = new SourceLocation();
        sourceLocation.m_className = annotation.getEnclosingClassName();
        sourceLocation.m_file = annotation.getEnclosingClassFile();
        sourceLocation.m_lineNumber = annotation.getLineNumber();
        sourceLocation.m_annotationClassName = annotation.getName();
        return sourceLocation;
    }

    /**
     * Renders a new source location.
     *
     * @param annotationClass
     * @param tag
     * @param enclosingClassName
     * @param enclosingClassFileName
     * @return
     */
    public static SourceLocation render(final Class annotationClass, final DocletTag tag, final String enclosingClassName, final String enclosingClassFileName) {
        SourceLocation sourceLocation = new SourceLocation();
        sourceLocation.m_className = enclosingClassName;
        sourceLocation.m_file = enclosingClassFileName;
        sourceLocation.m_lineNumber = tag.getLineNumber();
        sourceLocation.m_annotationClassName = annotationClass.getName();
        return sourceLocation;
    }

    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(m_className);
        sb.append(':');
        sb.append(m_lineNumber);
        sb.append(" @");
        sb.append(m_annotationClassName);
        sb.append(" in file [");
        sb.append(m_file);
        sb.append(']');
        return sb.toString();
    }

    public String getClassName() {
        return m_className;
    }

    public String getAnnnotationClassName() {
        return m_annotationClassName;
    }

    public int getLine() {
        return m_lineNumber;
    }

    public String getFile() {
        return m_file;
    }
}
/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler.javadoc;

/**
 * Raw info about an reader. Holds the name (the FQN of the reader interface) of the annotations
 * and its unparsed "content".
 * <p/>
 * Note: Two RawAnnotation instances are considered equals when the annotationClass is the same, no matter the value.
 *
 * @author <a href="mailto:alex@gnilux.org">Alexander Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class RawAnnotation {

    private final Class m_annotationClass;
    private final String m_value;

    private final int m_lineNumber;
    private final String m_enclosingClassName;
    private final String m_enclosingClassFile;

    /**
     * Creates a new raw annotation.
     *
     * @param annotationClass the annotation interface
     * @param value the unparsed annotation "content"
     * @param line number
     * @param enclosingClassName
     * @param enclosingClassFile
     */
    public RawAnnotation(final Class annotationClass,
                         final String value,
                         final int line,
                         final String enclosingClassName,
                         final String enclosingClassFile) {
        m_annotationClass = annotationClass;
        m_value = value;
        m_lineNumber = line;
        m_enclosingClassName = enclosingClassName;
        m_enclosingClassFile = enclosingClassFile;
    }

    /**
     * Returns the annotation name (which is the FQN of the annotation interface).
     *
     * @return
     */
    public String getName() {
        return m_annotationClass.getName();
    }

    /**
     * Returns the annotation "content".
     *
     * @return
     */
    public String getValue() {
        return m_value;
    }

    /**
     * Returns the annotation class
     *
     * @return
     */
    public Class getAnnotationClass() {
        return m_annotationClass;
    }

    //---- hashcode and equals used at compile time to ensure unicity on a member: only Class is needed

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RawAnnotation)) return false;

        final RawAnnotation rawAnnotation = (RawAnnotation) o;

        if (!m_annotationClass.equals(rawAnnotation.m_annotationClass)) return false;

        return true;
    }

    public int hashCode() {
        return m_annotationClass.hashCode();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("@");
        sb.append(getName());
        sb.append('(').append(getValue()).append(')');
        return sb.toString();
    }

    public String getEnclosingClassName() {
        return m_enclosingClassName;
    }

    public String getEnclosingClassFile() {
        return m_enclosingClassFile;
    }

    public int getLineNumber() {
        return m_lineNumber;
    }
}
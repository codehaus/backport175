/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler.javadoc;

/**
 * Raw info about an annotation. Holds the name (the FQN of the annotation interface) of the annotations
 * and its unparsed "content".
 *
 * @author <a href="mailto:alex@gnilux.org">Alexander Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class RawAnnotation {

    private final Class m_annotationClass;
    private final String m_value;

    /**
     * Creates a new raw annotation.
     *
     * @param annotationClass the annotation interface
     * @param value the unparsed annotation "content"
     */
    public RawAnnotation(final Class annotationClass, final String value) {
        m_annotationClass = annotationClass;
        m_value = value;
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
}
/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler.javadoc;

import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.JavaDocBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Parses and retrieves annotations from the JavaDoc in Java source files.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class JavaDocParser {

    private static final String JAVA_LANG_OBJECT_CLASS_NAME = "java.lang.Object";

    /**
     * The JavaDoc javadoc.
     */
    private final JavaDocBuilder m_javaDocParser = new JavaDocBuilder();

    /**
     * Adds the given ClassLoader to the search path
     *
     * @param loader
     */
    public void addClassLoaderToSearchPath(final ClassLoader loader) {
        m_javaDocParser.getClassLibrary().addClassLoader(loader);
    }

    /**
     * Adds a source tree to the builder.
     *
     * @param srcDirs the source trees
     */
    public void addSourceTrees(final String[] srcDirs) {
        for (int i = 0; i < srcDirs.length; i++) {
            try {
                m_javaDocParser.addSourceTree(new File(srcDirs[i]));
            } catch (Exception e) {
                throw new SourceParseException("source file in source tree [" + srcDirs[i] + "] could not be parsed - current javadoc does not understant Java 5 specific code (annotation, enums etc)", e);
            }
        }
    }

    /**
     * Adds a source file.
     *
     * @param srcFile the source file
     */
    public void addSource(final String srcFile) {
        try {
            m_javaDocParser.addSource(new File(srcFile));
        } catch (Exception e) {
            throw new SourceParseException("source file [" + srcFile + "] could not be parsed - current javadoc does not understant Java 5 specific code (annotation, enums etc)", e);
        }
    }

    /**
     * Returns all classes.
     *
     * @return an array with all classes
     */
    public JavaClass[] getJavaClasses() {
        Collection classes = m_javaDocParser.getClassLibrary().all();
        Collection javaClasses = new ArrayList();
        String className;
        for (Iterator it = classes.iterator(); it.hasNext();) {
            className = (String)it.next();
            if (JAVA_LANG_OBJECT_CLASS_NAME.equals(className)) {
                continue;
            }
            JavaClass clazz = m_javaDocParser.getClassByName(className);
            javaClasses.add(clazz);
        }
        return (JavaClass[])javaClasses.toArray(new JavaClass[]{});
    }

    /**
     * Extract the raw information of the annotation, the "content" inside the parenthesis).
     *
     * @param annotationClass
     * @param tag
     * @return RawAnnotation or null if not found
     */
    public static RawAnnotation getRawAnnotation(final Class annotationClass, final DocletTag tag) {
        String rawAnnotationString = tag.getName() + " " + tag.getValue();
        rawAnnotationString = rawAnnotationString.trim();

        removeFormattingCharacters(rawAnnotationString);

        int contentStartIndex = rawAnnotationString.indexOf('(');
        if (contentStartIndex != -1 && !rawAnnotationString.endsWith(")")) {
            throw new SourceParseException("annotation not well-formed, needs to end with a closing parenthesis [" + rawAnnotationString + "]");
        }

        final String value;
        if (contentStartIndex != -1) {
            value = rawAnnotationString.substring(contentStartIndex + 1, rawAnnotationString.length() - 1);
        } else {
            value = "";
        }
        return new RawAnnotation(annotationClass, value);
    }

    /**
     * Removes newline, carriage return and tab characters from a string.
     *
     * @param toBeEscaped string to escape
     * @return the escaped string
     */
    private static String removeFormattingCharacters(final String toBeEscaped) {
        StringBuffer escapedBuffer = new StringBuffer();
        for (int i = 0; i < toBeEscaped.length(); i++) {
            if ((toBeEscaped.charAt(i) != '\n') && (toBeEscaped.charAt(i) != '\r') && (toBeEscaped.charAt(i) != '\t')) {
                escapedBuffer.append(toBeEscaped.charAt(i));
            }
        }
        return escapedBuffer.toString();
    }
}
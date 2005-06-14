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

import org.codehaus.backport175.compiler.CompilerException;
import org.codehaus.backport175.compiler.SourceLocation;

/**
 * Parses and retrieves annotations from the JavaDoc in Java source files.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
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
                throw new SourceParseException("source file in source tree [" + srcDirs[i] + "] could not be parsed due to: " + e.toString(), e);
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
            throw new SourceParseException("source file [" + srcFile + "] could not be parsed due to: " + e.toString(), e);
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
     * @param annotationName
     * @param tag
     * @param enclosingClassName
     * @param enclosingClassFileName
     * @return RawAnnotation or null if not found
     */
    public static RawAnnotation getRawAnnotation(final Class annotationClass, final String annotationName, final DocletTag tag, final String enclosingClassName, final String enclosingClassFileName) {
        String rawAnnotationString = tag.getName() + " " + tag.getValue();
        rawAnnotationString = rawAnnotationString.trim();
        removeFormattingCharacters(rawAnnotationString);

        if (rawAnnotationString.length() > annotationName.length()) {
            String rawValueString = rawAnnotationString.substring(annotationName.length());
            rawValueString = rawValueString.trim();
            char first = rawValueString.charAt(0);
            if (first == '(') {
                if (!rawValueString.endsWith(")")) {
                    throw new CompilerException("annotation not well-formed, needs to end with a closing parenthesis ["
                            + rawAnnotationString + "]",
                            SourceLocation.render(annotationClass, tag, enclosingClassName, enclosingClassFileName)
                    );
                }
                return new RawAnnotation(annotationClass, rawValueString.substring(1, rawValueString.length()-1), tag.getLineNumber(), enclosingClassName, enclosingClassFileName);
            } else if (first == '"') {
                if (!rawValueString.endsWith("\"")) {
                    throw new CompilerException("annotation not well-formed, needs to end with a closing \" ["
                            + rawAnnotationString + "]",
                            SourceLocation.render(annotationClass, tag, enclosingClassName, enclosingClassFileName)
                    );
                }
                return new RawAnnotation(annotationClass, rawValueString, tag.getLineNumber(), enclosingClassName, enclosingClassFileName);
            } else {
                // escape
                StringBuffer sb = new StringBuffer("\"");
                for (int i = 0; i < rawValueString.length(); i++) {
                    char c = rawValueString.charAt(i);
                    if (c == '\"') {
                        sb.append("\\\"");
                    } else {
                        sb.append(c);
                    }
                }
                sb.append("\"");
                return new RawAnnotation(annotationClass, sb.toString(), tag.getLineNumber(), enclosingClassName, enclosingClassFileName);
            }
        } else {
            return new RawAnnotation(annotationClass, "", tag.getLineNumber(), enclosingClassName, enclosingClassFileName);
        }
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
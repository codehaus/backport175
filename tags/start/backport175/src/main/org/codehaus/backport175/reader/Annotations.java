/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.reader;

import org.codehaus.backport175.reader.bytecode.AnnotationReader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Helper class for annotation retrieval of strongly typed JavaDoc annotations (as well as regular Java 5 {@link
 * java.lang.annotation.RetentionPolicy.RUNTIME} annotations  when running Java 1.5.x).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public final class Annotations {

    /**
     * Return the annotation with a specific name for a specific class.
     *
     * @param annotationName the annotation name
     * @param klass          the java.lang.Class object to find the annotation on.
     * @return the annotation or null
     */
    public static Annotation getAnnotation(final String annotationName, final Class klass) {
        final AnnotationReader reader = AnnotationReader.getReaderFor(klass);
        return reader.getAnnotation(annotationName);
    }

    /**
     * Return the annotation with a specific name for a specific class.
     *
     * @param annotation the annotation class
     * @param klass      the java.lang.Class object to find the annotation on.
     * @return the annotation or null
     */
    public static Annotation getAnnotation(final Class annotation, final Class klass) {
        return getAnnotation(getAnnnotationName(annotation), klass);
    }

    /**
     * Return the annotation with a specific name for a specific method.
     *
     * @param annotationName the annotation name
     * @param method         the java.lang.refect.Method object to find the annotation on.
     * @return the annotation or null
     */
    public static Annotation getAnnotation(final String annotationName, final Method method) {
        final AnnotationReader reader = AnnotationReader.getReaderFor(method.getDeclaringClass());
        return reader.getAnnotation(annotationName, method);
    }

    /**
     * Return the annotation with a specific name for a specific method.
     *
     * @param annotation the annotation class
     * @param method     the java.lang.refect.Method object to find the annotation on.
     * @return the annotation or null
     */
    public static Annotation getAnnotation(final Class annotation, final Method method) {
        return getAnnotation(getAnnnotationName(annotation), method);
    }

    /**
     * Return the annotation with a specific name for a specific constructor.
     *
     * @param annotationName the annotation name
     * @param constructor    the java.lang.refect.Constructor object to find the annotation on.
     * @return the annotation or null
     */
    public static Annotation getAnnotation(final String annotationName, final Constructor constructor) {
        final AnnotationReader reader = AnnotationReader.getReaderFor(constructor.getDeclaringClass());
        return reader.getAnnotation(annotationName, constructor);
    }

    /**
     * Return the annotation with a specific name for a specific constructor.
     *
     * @param annotation  the annotation class
     * @param constructor the java.lang.refect.Constructor object to find the annotation on.
     * @return the annotation or null
     */
    public static Annotation getAnnotation(final Class annotation, final Constructor constructor) {
        return getAnnotation(getAnnnotationName(annotation), constructor);
    }

    /**
     * Return the annotation with a specific name for a specific field.
     *
     * @param annotationName the annotation name
     * @param field          the java.lang.reflect.Field object to find the annotation on.
     * @return the annotation or null
     */
    public static Annotation getAnnotation(final String annotationName, final Field field) {
        final AnnotationReader reader = AnnotationReader.getReaderFor(field.getDeclaringClass());
        return reader.getAnnotation(annotationName, field);
    }

    /**
     * Return the annotation with a specific name for a specific field.
     *
     * @param annotation the annotation class
     * @param field      the java.lang.reflect.Field object to find the annotation on.
     * @return the annotation or null
     */
    public static Annotation getAnnotation(final Class annotation, final Field field) {
        return getAnnotation(getAnnnotationName(annotation), field);
    }

    /**
     * Returns the annotation class name in Java style.
     *
     * @param annotation
     * @return
     */
    private static String getAnnnotationName(final Class annotation) {
        return annotation.getName().replace('/', '.');
    }
}
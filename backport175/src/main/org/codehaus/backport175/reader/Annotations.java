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
     * Checks if an annotation is present at a specific class.
     *
     * @param annotationType the annotation type
     * @param target the annotated type
     * @return true if the annotation is present else false
     */
    public static boolean isAnnotationPresent(final Class annotationType, final Class target) {
        return AnnotationReader.getReaderFor(target).isAnnotationPresent(annotationType);
    }

    /**
     * Return all the annotations for a specific class.
     *
     * @param target          the java.lang.Class object to find the annotations on.
     * @return an array with the annotations
     */
    public static Annotation[] getAnnotations(final Class target) {
        return AnnotationReader.getReaderFor(target).getAnnotations();
    }

    /**
     * Return the annotation with a specific name for a specific class.
     *
     * @param annotation the annotation class
     * @param target      the java.lang.Class object to find the annotation on.
     * @return the annotation or null
     */
    public static Annotation getAnnotation(final Class annotation, final Class target) {
        return getAnnotation(getAnnnotationName(annotation), target);
    }

    /**
     * Return the annotation with a specific name for a specific class.
     *
     * @param annotationName the annotation name
     * @param target          the java.lang.Class object to find the annotation on.
     * @return the annotation or null
     */
    public static Annotation getAnnotation(final String annotationName, final Class target) {
        final AnnotationReader reader = AnnotationReader.getReaderFor(target);
        return reader.getAnnotation(annotationName);
    }

    /**
     * Checks if an annotation is present at a specific method.
     *
     * @param annotationType the annotation type
     * @param method the annotated type
     * @return true if the annotation is present else false
     */
    public static boolean isAnnotationPresent(final Class annotationType, final Method method) {
        return AnnotationReader.getReaderFor(method.getDeclaringClass()).isAnnotationPresent(annotationType, method);
    }

    /**
     * Return all the annotations for a specific method.
     *
     * @param method the java.lang.reflect.Method object to find the annotations on.
     * @return an array with the annotations
     */
    public static Annotation[] getAnnotations(final Method method) {
        return AnnotationReader.getReaderFor(method.getDeclaringClass()).getAnnotations(method);
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
     * Checks if an annotation is present at a specific method.
     *
     * @param annotationType the annotation type
     * @param constructor the annotated type
     * @return true if the annotation is present else false
     */
    public static boolean isAnnotationPresent(final Class annotationType, final Constructor constructor) {
        final AnnotationReader reader = AnnotationReader.getReaderFor(constructor.getDeclaringClass());
        return reader.isAnnotationPresent(annotationType, constructor);
    }

    /**
     * Return all the annotations for a specific constructor.
     *
     * @param constructor the java.lang.reflect.Constructor object to find the annotations on.
     * @return an array with the annotations
     */
    public static Annotation[] getAnnotations(final Constructor constructor) {
        return AnnotationReader.getReaderFor(constructor.getDeclaringClass()).getAnnotations(constructor);
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
     * Checks if an annotation is present at a specific field.
     *
     * @param annotationType the annotation type
     * @param field the annotated type
     * @return true if the annotation is present else false
     */
    public static boolean isAnnotationPresent(final Class annotationType, final Field field) {
        return AnnotationReader.getReaderFor(field.getDeclaringClass()).isAnnotationPresent(annotationType, field);
    }

    /**
      * Return all the annotations for a specific field.
      *
      * @param field the java.lang.reflect.Field object to find the annotations on.
      * @return an array with the annotations
      */
     public static Annotation[] getAnnotations(final Field field) {
         return AnnotationReader.getReaderFor(field.getDeclaringClass()).getAnnotations(field);
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
     * Returns the annotation class name in Java style.
     *
     * @param annotation
     * @return
     */
    private static String getAnnnotationName(final Class annotation) {
        return annotation.getName().replace('/', '.');
    }
}
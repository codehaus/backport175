/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.reader.bytecode;

import org.objectweb.asm.*;
import org.codehaus.backport175.reader.bytecode.spi.BytecodeProvider;
import org.codehaus.backport175.reader.proxy.ProxyFactory;
import org.codehaus.backport175.reader.Annotation;
import org.codehaus.backport175.reader.ReaderException;

import java.util.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;

/**
 * Reads Java 5 {@link java.lang.annotation.RetentionPolicy.RUNTIME} annotations from the class' bytecode.
 * <p/>
 * Can be used with a custom implementation of the {@link org.codehaus.backport175.reader.bytecode.spi.BytecodeProvider}
 * interface.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AnnotationReader {

    private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];
    private static final String INIT_METHOD_NAME = "<init>";

    private static BytecodeProvider BYTECODE_PROVIDER = new DefaultBytecodeProvider();

    private static final Map READERS = new WeakHashMap();

    private final WeakReference m_classRef;

    // ===========================================================================
    // Implementation notes:
    // Parsing and annotation creation is made in two steps
    //
    // 1. The bytecode is parsed and the annotation content is put in elements,
    //    which are stored for later processing
    //
    // 2. Upon annotation access the elements are processed and a dynamic proxy
    //    for the annotation is created and cached.
    //
    // This gives much better performance than reflective access of Java 5
    // annotations (reflective access is around 5 times slower)
    // ===========================================================================

    private final Map m_classAnnotationElements = new HashMap();
    private final Map m_constructorAnnotationElements = new HashMap();
    private final Map m_methodAnnotationElements = new HashMap();
    private final Map m_fieldAnnotationElements = new HashMap();

    private final Map m_classAnnotationCache = new HashMap();
    private final Map m_constructorAnnotationCache = new HashMap();
    private final Map m_methodAnnotationCache = new HashMap();
    private final Map m_fieldAnnotationCache = new HashMap();

    /**
     * Sets the bytecode provider.
     * <p/>
     * If a custom provider is not set then a default impl will be used (which reads the bytecode from disk).
     *
     * @param bytecodeProvider
     */
    public static void setBytecodeProvider(final BytecodeProvider bytecodeProvider) {
        synchronized (BYTECODE_PROVIDER) {
            BYTECODE_PROVIDER = bytecodeProvider;
        }
    }

    /**
     * Returns the annotation reader for the class specified.
     * <p/>
     * The annotation reader is created and cached if non-existant.
     *
     * @param klass
     * @return the annotation reader
     */
    public static AnnotationReader getReaderFor(final Class klass) {
        final AnnotationReader reader;
        Object value = READERS.get(klass);
        if (value == null) {
            synchronized (READERS) {
                reader = new AnnotationReader(klass);
                READERS.put(klass, reader);
            }
        } else {
            reader = (AnnotationReader)value;
        }
        return reader;
    }

    /**
     * Resets the annotation reader for the class specified and triggers a new parsing of the newly read bytecode.
     * <p/>
     * This method calls <code>parse</code> and is therefore all the is needed to invoke to get a fully updated reader.
     *
     * @param klass
     */
    public static void refresh(final Class klass) {
        AnnotationReader reader = getReaderFor(klass);
        synchronized (reader) {
            reader.refresh();
        }
    }

    /**
     * Resets *all* the annotation reader and triggers a new parsing of the newly read bytecode.
     * <p/>
     * This method will force parsing of all classes bytecode which might be very time consuming, use with care.
     * <p/>
     * This method calls <code>parse</code> and is therefore all the is needed to invoke to get a fully updated reader.
     */
    public static void refreshAll() {
        for (Iterator it = READERS.values().iterator(); it.hasNext();) {
            AnnotationReader reader = (AnnotationReader)it.next();
            synchronized (reader) {
                reader.refresh();
            }
        }
    }

    /**
     * Converts the annotion class description to a Java class name.
     *
     * @param desc
     * @return
     */
    public static String toJavaName(final String desc) {
        return desc.substring(1, desc.length() - 1).replace('/', '.');
    }

    /**
     * Checks if an annotation is present at a specific class.
     *
     * @param annotationType the annotation type
     * @return true if the annotation is present else false
     */
    public boolean isAnnotationPresent(final Class annotationType) {
        return m_classAnnotationElements.containsKey(annotationType.getName());
    }

    /**
     * Returns the class annotation with the name specified.
     *
     * @param annotationName
     * @return the class annotation
     */
    public Annotation getAnnotation(final String annotationName) {
        Object cachedAnnotation = m_classAnnotationCache.get(annotationName);
        if (cachedAnnotation != null) {
            return (Annotation)cachedAnnotation;
        } else {
            final Annotation annotation;
            final AnnotationElement.Annotation annotationInfo =
                    (AnnotationElement.Annotation)m_classAnnotationElements.get(annotationName);
            if (annotationInfo != null) {
                annotation = ProxyFactory.newAnnotationProxy(
                        annotationInfo,
                        ((Class)m_classRef.get()).getClassLoader()
                );
                m_classAnnotationCache.put(annotationName, annotation);
                return annotation;
            } else {
                return null;
            }
        }
    }

    /**
     * Returns all the class annotations.
     *
     * @return an array with the class annotations
     */
    public Annotation[] getAnnotations() {
        final Collection annotationElements = m_classAnnotationElements.values();
        if (annotationElements.isEmpty()) {
            return EMPTY_ANNOTATION_ARRAY;
        }
        return getAnnotations(annotationElements, m_classAnnotationCache);
    }

    /**
     * Checks if an annotation is present at a specific constructor.
     *
     * @param annotationType the annotation type
     * @return true if the annotation is present else false
     */
    public boolean isAnnotationPresent(final Class annotationType, final Constructor constructor) {
        final AnnotationReader.MemberKey key = AnnotationReader.MemberKey.newMemberKey(constructor);
        Object map = m_constructorAnnotationElements.get(key);
        if (map != null) {
            if (((Map)map).containsKey(annotationType.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the constructor annotation with the name specified for the constructor specified.
     *
     * @param annotationName
     * @return the constructor annotation
     */
    public Annotation getAnnotation(final String annotationName, final Constructor constructor) {
        Map annotationMap = getAnnotationCacheFor(constructor);
        Object cachedAnnotation = annotationMap.get(annotationName);
        if (cachedAnnotation != null) {
            return (Annotation)cachedAnnotation;
        }
        // not in cache - create a new DP and put in cache
        final AnnotationReader.MemberKey key = AnnotationReader.MemberKey.newMemberKey(constructor);
        final Map annotations = (Map)m_constructorAnnotationElements.get(key);
        Object annotationElement = annotations.get(annotationName);
        if (annotationElement != null) {
            Annotation annotation = ProxyFactory.newAnnotationProxy(
                    (AnnotationElement.Annotation)annotationElement,
                    constructor.getDeclaringClass().getClassLoader()
            );
            annotationMap.put(annotationName, annotation);
            return annotation;
        }
        return null;
    }

    /**
     * Returns all the constructor annotations.
     *
     * @param constructor the java.lang.reflect.Constructor object to find the annotations on.
     * @return an array with the constructor annotations
     */
    public Annotation[] getAnnotations(final Constructor constructor) {
        final AnnotationReader.MemberKey key = AnnotationReader.MemberKey.newMemberKey(constructor);
        Object map = m_constructorAnnotationElements.get(key);
        if (map != null) {
            final Collection annotationElements = ((Map)m_constructorAnnotationElements.get(key)).values();
            if (annotationElements.isEmpty()) {
                return EMPTY_ANNOTATION_ARRAY;
            }
            final Map cache = getAnnotationCacheFor(constructor);
            return getAnnotations(annotationElements, cache);
        } else {
            return EMPTY_ANNOTATION_ARRAY;
        }
    }

    /**
     * Checks if an annotation is present at a specific method.
     *
     * @param annotationType the annotation type
     * @return true if the annotation is present else false
     */
    public boolean isAnnotationPresent(final Class annotationType, final Method method) {
        final AnnotationReader.MemberKey key = AnnotationReader.MemberKey.newMemberKey(method);
        Object map = m_methodAnnotationElements.get(key);
        if (map != null) {
            if (((Map)m_methodAnnotationElements.get(key)).containsKey(annotationType.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the method annotation with the name specified for the method specified.
     *
     * @param annotationName
     * @return the method annotation
     */
    public Annotation getAnnotation(final String annotationName, final Method method) {
        Map annotationMap = (Map)m_methodAnnotationCache.get(method);
        if (annotationMap == null) {
            annotationMap = new HashMap();
            m_methodAnnotationCache.put(method, annotationMap);
        }
        Object cachedAnnotation = annotationMap.get(annotationName);
        if (cachedAnnotation != null) {
            return (Annotation)cachedAnnotation;
        }
        // not in cache - create a new DP and put in cache
        final AnnotationReader.MemberKey key = AnnotationReader.MemberKey.newMemberKey(method);
        final Map annotations = (Map)m_methodAnnotationElements.get(key);
        Object annotationElement = annotations.get(annotationName);
        if (annotationElement != null) {
            Annotation annotation = ProxyFactory.newAnnotationProxy(
                    (AnnotationElement.Annotation)annotationElement,
                    method.getDeclaringClass().getClassLoader()
            );
            annotationMap.put(annotationName, annotation);
            return annotation;
        }
        return null;
    }

    /**
     * Returns all the method annotations.
     *
     * @param method the java.lang.reflect.Method object to find the annotations on.
     * @return an array with the method annotations
     */
    public Annotation[] getAnnotations(final Method method) {
        final AnnotationReader.MemberKey key = AnnotationReader.MemberKey.newMemberKey(method);
        Object map = m_methodAnnotationElements.get(key);
        if (map != null) {
            final Collection annotationElements = ((Map)map).values();
            if (annotationElements.isEmpty()) {
                return EMPTY_ANNOTATION_ARRAY;
            }
            final Map cache = getAnnotationCacheFor(method);
            return getAnnotations(annotationElements, cache);
        } else {
            return EMPTY_ANNOTATION_ARRAY;
        }
    }

    /**
     * Checks if an annotation is present at a specific field.
     *
     * @param annotationType the annotation type
     * @return true if the annotation is present else false
     */
    public boolean isAnnotationPresent(final Class annotationType, final Field field) {
        final AnnotationReader.MemberKey key = AnnotationReader.MemberKey.newMemberKey(field);
        Object map = m_fieldAnnotationElements.get(key);
        if (map != null) {
            if (((Map)map).containsKey(annotationType.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the field annotation with the name specified for the field specified.
     *
     * @param annotationName
     * @return the field annotation
     */
    public Annotation getAnnotation(final String annotationName, final Field field) {
        Map annotationMap = (Map)m_fieldAnnotationCache.get(field);
        if (annotationMap == null) {
            annotationMap = new HashMap();
            m_fieldAnnotationCache.put(field, annotationMap);
        }
        Object cachedAnnotation = annotationMap.get(annotationName);
        if (cachedAnnotation != null) {
            return (Annotation)cachedAnnotation;
        }
         // not in cache - create a new DP and put in cache
        final AnnotationReader.MemberKey key = AnnotationReader.MemberKey.newMemberKey(field);
        final Map annotations = (Map)m_fieldAnnotationElements.get(key);
        Object annotationElement = annotations.get(annotationName);
        if (annotationElement != null) {
            Annotation annotation = ProxyFactory.newAnnotationProxy(
                    (AnnotationElement.Annotation)annotationElement,
                    field.getDeclaringClass().getClassLoader()
            );
            annotationMap.put(annotationName, annotation);
            return annotation;
        }
        return null;
    }

    /**
     * Returns all the field annotations.
     *
     * @param field the java.lang.reflect.Field object to find the annotations on.
     * @return an array with the field annotations
     */
    public Annotation[] getAnnotations(final Field field) {
        final AnnotationReader.MemberKey key = AnnotationReader.MemberKey.newMemberKey(field);
        Object map = m_fieldAnnotationElements.get(key);
        if (map != null) {
            final Collection annotationElements = ((Map)map).values();
            if (annotationElements.isEmpty()) {
                return EMPTY_ANNOTATION_ARRAY;
            }
            final Map cache = getAnnotationCacheFor(field);
            return getAnnotations(annotationElements, cache);
        } else {
            return EMPTY_ANNOTATION_ARRAY;
        }
    }

    /**
     * Returns the annotations for a specific member.
     *
     * @param annotationElements the annotation elements for the member
     * @param annotationCache    the annotation cache to use
     * @return an array with the annotations
     */
    private Annotation[] getAnnotations(final Collection annotationElements, final Map annotationCache) {
        final Annotation[] annotations = new Annotation[annotationElements.size()];
        int i = 0;
        if (annotationCache.size() == annotationElements.size()) {
            // all are cached - use cache
            for (Iterator it = annotationCache.values().iterator(); it.hasNext();) {
                annotations[i++] = (Annotation)it.next();
            }
            return annotations;
        } else {
            // cache not complete - fill it up with the ones missing
            ClassLoader loader = ((Class)m_classRef.get()).getClassLoader();
            i = 0;
            for (Iterator it = annotationElements.iterator(); it.hasNext();) {
                AnnotationElement.Annotation annotationElement = (AnnotationElement.Annotation)it.next();
                String annotationClassName = annotationElement.getInterfaceName();
                if (annotationCache.containsKey(annotationClassName)) {
                    // proxy in cache - reuse
                    annotations[i++] = (Annotation)annotationCache.get(annotationClassName);
                } else {
                    final Annotation annotation = ProxyFactory.newAnnotationProxy(annotationElement, loader);
                    annotationCache.put(annotationClassName, annotation);
                    annotations[i++] = annotation;
                }
            }
            return annotations;
        }
    }

    /**
     * Returns the annotation cache for a specific constructor.
     *
     * @param constructor the constructor
     * @return the cache
     */
    private Map getAnnotationCacheFor(final Constructor constructor) {
        Map annotationMap = (Map)m_constructorAnnotationCache.get(constructor);
        if (annotationMap == null) {
            annotationMap = new HashMap();
            m_constructorAnnotationCache.put(constructor, annotationMap);
        }
        return annotationMap;
    }

    /**
     * Returns the annotation cache for a specific method.
     *
     * @param method the method
     * @return the cache
     */
    private Map getAnnotationCacheFor(final Method method) {
        Map annotationMap = (Map)m_methodAnnotationCache.get(method);
        if (annotationMap == null) {
            annotationMap = new HashMap();
            m_methodAnnotationCache.put(method, annotationMap);
        }
        return annotationMap;
    }

    /**
     * Returns the annotation cache for a specific field.
     *
     * @param field the field
     * @return the cache
     */
    private Map getAnnotationCacheFor(final Field field) {
        Map annotationMap = (Map)m_fieldAnnotationCache.get(field);
        if (annotationMap == null) {
            annotationMap = new HashMap();
            m_fieldAnnotationCache.put(field, annotationMap);
        }
        return annotationMap;
    }

    /**
     * Resets the annotation reader and triggers a new parsing of the newly read bytecode.
     * <p/>
     * This method calls <code>parse</code> and is therefore all the is needed to invoke to get a fully updated reader.
     */
    private void refresh() {
        m_classAnnotationElements.clear();
        m_constructorAnnotationElements.clear();
        m_methodAnnotationElements.clear();
        m_fieldAnnotationElements.clear();
        m_classAnnotationCache.clear();
        m_constructorAnnotationCache.clear();
        m_methodAnnotationCache.clear();
        m_fieldAnnotationCache.clear();
        parse((Class)m_classRef.get());
    }

    /**
     * Parses the class bytecode and retrieves the annotations.
     *
     * @param klass
     */
    private void parse(final Class klass) {
        final String className = klass.getName();
        final ClassLoader loader = klass.getClassLoader();
        final byte[] bytes;
        try {
            bytes = BYTECODE_PROVIDER.getBytecode(className, loader);
        } catch (Exception e) {
            throw new ReaderException("could not retrieve the bytecode from the bytecode provider [" + BYTECODE_PROVIDER.getClass().getName() + "]", e);
        }
        ClassReader classReader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(true);
        classReader.accept(new AnnotationRetrievingVisitor(writer), false);
    }

    /**
     * Creates a new instance of the annotation reader, reads from the class specified.
     *
     * @param klass
     */
    private AnnotationReader(final Class klass) {
        if (klass == null) {
            throw new IllegalArgumentException("class can not be null");
        }
        m_classRef = new WeakReference(klass);
        parse(klass);
    }

    /**
     * Retrieves the Java 5 RuntimeVisibleAnnotations annotations from the class bytecode.
     *
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
     */
    private class AnnotationRetrievingVisitor extends ClassAdapter {

        public AnnotationRetrievingVisitor(final ClassVisitor cv) {
            super(cv);
        }

        public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
            cv.visitAnnotation(desc, visible);
            String annotationClassName = toJavaName(desc);
            final AnnotationElement.Annotation annotation = new AnnotationElement.Annotation(annotationClassName);
            m_classAnnotationElements.put(annotationClassName, annotation);
            return createAnnotationVisitor(annotation);
        }

        public FieldVisitor visitField(
                final int access,
                final String name,
                final String desc,
                final String signature,
                final Object value) {
            final FieldVisitor visitor = cv.visitField(access, name, desc, signature, value);

            final MemberKey key = new MemberKey(name, desc);
            return new FieldVisitor() {
                public AnnotationVisitor visitAnnotation(final String desc, boolean visible) {
                    final String className = toJavaName(desc);
                    final AnnotationElement.Annotation annotation = new AnnotationElement.Annotation(className);
                    if (m_fieldAnnotationElements.containsKey(key)) {
                        ((Map)m_fieldAnnotationElements.get(key)).put(className, annotation);
                    } else {
                        final Map annotations = new HashMap();
                        annotations.put(className, annotation);
                        m_fieldAnnotationElements.put(key, annotations);
                    }
                    return createAnnotationVisitor(annotation);
                }

                public void visitAttribute(final Attribute attribute) {
                    visitor.visitAttribute(attribute);
                }

                public void visitEnd() {
                    visitor.visitEnd();
                }
            };
        }

        public MethodVisitor visitMethod(
                final int access,
                final String name,
                final String desc,
                final String signature,
                final String[] exceptions) {
            MethodVisitor visitor = cv.visitMethod(access, name, desc, signature, exceptions);

            final MemberKey key = new MemberKey(name, desc);
            if (name.equals(INIT_METHOD_NAME)) {
                return new MethodAdapter(visitor) {
                    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                        final String className = toJavaName(desc);
                        final AnnotationElement.Annotation annotation = new AnnotationElement.Annotation(className);
                        if (m_constructorAnnotationElements.containsKey(key)) {
                            ((Map)m_constructorAnnotationElements.get(key)).put(className, annotation);
                        } else {
                            final Map annotations = new HashMap();
                            annotations.put(className, annotation);
                            m_constructorAnnotationElements.put(key, annotations);
                        }
                        return createAnnotationVisitor(annotation);
                    }
                };

            } else {
                return new MethodAdapter(visitor) {
                    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                        String className = toJavaName(desc);
                        final AnnotationElement.Annotation annotation = new AnnotationElement.Annotation(className);
                        if (m_methodAnnotationElements.containsKey(key)) {
                            ((Map)m_methodAnnotationElements.get(key)).put(className, annotation);
                        } else {
                            final Map annotations = new HashMap();
                            annotations.put(className, annotation);
                            m_methodAnnotationElements.put(key, annotations);
                        }
                        return createAnnotationVisitor(annotation);
                    }
                };
            }
        }

        /**
         * Returns the annotation visitor to use.
         * <p/>
         * Swap to the 'tracing' visitor for simple debugging.
         *
         * @param annotation
         * 
         * @return
         */
        private AnnotationVisitor createAnnotationVisitor(final AnnotationElement.Annotation annotation) {
            return new AnnotationBuilderVisitor(annotation);
//            return new TraceAnnotationVisitor();
        }
    }

    private class AnnotationBuilderVisitor implements AnnotationVisitor {

        private final AnnotationElement.NestedAnnotationElement m_nestedAnnotationElement;

        public AnnotationBuilderVisitor(final AnnotationElement.NestedAnnotationElement annotation) {
            m_nestedAnnotationElement = annotation;
        }

        public void visit(final String name, final Object value) {
            if (value instanceof Type) {
                // type
                m_nestedAnnotationElement.addElement(name, value);
            } else {
                // primitive value
                if (value.getClass().isArray()) {
                    // primitive array value
                    handlePrimitiveArrayValue(value, name);
                } else {
                    // primitive non-array value
                    m_nestedAnnotationElement.addElement(name, value);
                }
            }
        }

        public void visitEnum(final String name, final String desc, final String value) {
            m_nestedAnnotationElement.addElement(name, new AnnotationElement.Enum(desc, value));
        }

        public AnnotationVisitor visitAnnotation(final String name, final String desc) {
            String className = toJavaName(desc);
            AnnotationElement.NestedAnnotationElement annotation = new AnnotationElement.Annotation(className);
            m_nestedAnnotationElement.addElement(name, annotation);
            return new AnnotationBuilderVisitor(annotation);
        }

        public AnnotationVisitor visitArray(final String name) {
            AnnotationElement.NestedAnnotationElement array = new AnnotationElement.Array();
            m_nestedAnnotationElement.addElement(name, array);
            return new AnnotationBuilderVisitor(array);
        }

        public void visitEnd() {
        }

        /**
         * Handles array of primitive values. The JSR-175 spec. only suppots one dimensional arrays.
         *
         * @param value
         * @param name
         */
        private void handlePrimitiveArrayValue(final Object value, final String name) {
            if (value.getClass().getComponentType().isPrimitive()) {
                // primitive array type
                if (value instanceof String[]) {
                    // string array
                    m_nestedAnnotationElement.addElement(name, value);
                } else {
                    AnnotationElement.NestedAnnotationElement arrayElement = new AnnotationElement.Array();
                    // non-string primitive array
                    if (value instanceof int[]) {
                        int[] array = (int[])value;
                        for (int i = 0; i < array.length; i++) {
                            arrayElement.addElement(null, new Integer(array[i]));
                        }
                    } else if (value instanceof long[]) {
                        long[] array = (long[])value;
                        for (int i = 0; i < array.length; i++) {
                            arrayElement.addElement(null, new Long(array[i]));
                        }
                    } else if (value instanceof short[]) {
                        short[] array = (short[])value;
                        for (int i = 0; i < array.length; i++) {
                            arrayElement.addElement(null, new Short(array[i]));
                        }
                    } else if (value instanceof float[]) {
                        float[] array = (float[])value;
                        for (int i = 0; i < array.length; i++) {
                            arrayElement.addElement(null, new Float(array[i]));
                        }
                    } else if (value instanceof double[]) {
                        double[] array = (double[])value;
                        for (int i = 0; i < array.length; i++) {
                            arrayElement.addElement(null, new Double(array[i]));
                        }
                    } else if (value instanceof boolean[]) {
                        boolean[] array = (boolean[])value;
                        for (int i = 0; i < array.length; i++) {
                            arrayElement.addElement(null, new Boolean(array[i]));
                        }
                    } else if (value instanceof byte[]) {
                        byte[] array = (byte[])value;
                        for (int i = 0; i < array.length; i++) {
                            arrayElement.addElement(null, new Byte(array[i]));
                        }
                    } else if (value instanceof char[]) {
                        char[] array = (char[])value;
                        for (int i = 0; i < array.length; i++) {
                            arrayElement.addElement(null, new Character(array[i]));
                        }
                    }
                    m_nestedAnnotationElement.addElement(name, arrayElement);
                }
            } else {
                m_nestedAnnotationElement.addElement(name, value);
            }
        }
    }

    /**
     * Unique key for class members (methods, fields and constructors) to be used in hash maps etc.
     * <p/>
     * Needed since at bytecode parsing time we do not have access to the reflect members, only strings.
     *
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
     */
    public static class MemberKey {
        private final String m_name;
        private final String m_desc;

        public static MemberKey newMemberKey(final Constructor method) {
            return new MemberKey(INIT_METHOD_NAME, SignatureHelper.getConstructorSignature(method));
        }

        public static MemberKey newMemberKey(final Method method) {
            return new MemberKey(method.getName(), SignatureHelper.getMethodSignature(method));
        }

        public static MemberKey newMemberKey(final Field field) {
            return new MemberKey(field.getName(), SignatureHelper.getFieldSignature(field));
        }

        public MemberKey(final String name, final String desc) {
            m_name = name;
            m_desc = desc;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof MemberKey)) {
                return false;
            }

            final MemberKey memberKey = (MemberKey)o;

            if (m_desc != null ? !m_desc.equals(memberKey.m_desc) : memberKey.m_desc != null) {
                return false;
            }
            if (m_name != null ? !m_name.equals(memberKey.m_name) : memberKey.m_name != null) {
                return false;
            }

            return true;
        }

        public int hashCode() {
            int result;
            result = (m_name != null ? m_name.hashCode() : 0);
            result = 29 * result + (m_desc != null ? m_desc.hashCode() : 0);
            return result;
        }
    }

    /**
     * To be used for debugging purposes.
     */
    private class TraceAnnotationVisitor implements AnnotationVisitor {
        public void visit(final String name, final Object value) {
            System.out.println("    NAMED-VALUE: " + name + "->" + value);
        }

        public void visitEnum(final String name, final String desc, final String value) {
            System.out.println("    ENUM: " + name);
        }

        public AnnotationVisitor visitAnnotation(final String name, final String desc) {
            System.out.println("    ANNOTATION: " + name);
            return new TraceAnnotationVisitor();
        }

        public AnnotationVisitor visitArray(final String name) {
            System.out.println("    ARRAY: " + name);
            return new TraceAnnotationVisitor();
        }

        public void visitEnd() {
        }
    }
}
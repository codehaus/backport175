/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler.bytecode;

import org.codehaus.backport175.compiler.CompilerException;
import org.codehaus.backport175.compiler.SourceLocation;
import org.codehaus.backport175.compiler.MessageHandler;
import org.codehaus.backport175.compiler.javadoc.RawAnnotation;
import org.codehaus.backport175.compiler.parser.AnnotationParser;
import org.codehaus.backport175.compiler.parser.ParseException;
import org.objectweb.asm.*;
import org.objectweb.asm.Type;

import java.net.URLClassLoader;
import java.net.URL;
import java.util.*;
import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.thoughtworks.qdox.model.*;

/**
 * Enhances the target class with the JavaDoc annotations by putting them  in the class bytecode as Java 5 {@link
 * java.lang.reader.RetentionPolicy.RUNTIME} annotations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class AnnotationEnhancer {

    /**
     * The class reader.
     */
    private ClassReader m_reader = null;

    /**
     * The name of the class file.
     */
    private String m_classFileName = null;

    /**
     * The class name.
     */
    private String m_className = null;

    /**
     * Compiled class' class loader
     */
    private ClassLoader m_loader = null;

    /**
     * The class annotations.
     */
    private List m_classAnnotations = new ArrayList();

    /**
     * The constructor annotations.
     */
    private List m_constructorAnnotations = new ArrayList();

    /**
     * The method annotations.
     */
    private List m_methodAnnotations = new ArrayList();

    /**
     * The field annotations.
     */
    private List m_fieldAnnotations = new ArrayList();

    /**
     * The message handler we report success to
     */
    private final MessageHandler m_messageHandler;

    /**
     * Constructor
     * @param messageHandler
     */
    public AnnotationEnhancer(MessageHandler messageHandler) {
        m_messageHandler = messageHandler;
    }

    /**
     * Initializes the enhancer. Must always be called before use.
     *
     * @param className the class name
     * @param classPath the class path
     * @return true if the class was succefully loaded, false otherwise
     */
    public boolean initialize(final String className, final URL[] classPath) {
        return initialize(className, new URLClassLoader(classPath));
    }

    /**
     * Initializes the enhancer. Must always be called before use.
     *
     * @param className the class name
     * @param loader
     * @return true if the class was succefully loaded, false otherwise
     */
    public boolean initialize(final String className, final ClassLoader loader) {
        try {
            m_className = className;
            m_loader = loader;
            m_classFileName = className.replace('.', '/') + ".class";
            InputStream classAsStream = m_loader.getResourceAsStream(m_classFileName);
            if (classAsStream == null) {
                return false;
            }
            // setup the ASM stuff in init, but only parse at write time
            try {
                m_reader = new ClassReader(classAsStream);
            } catch (Exception e) {
                throw new ClassNotFoundException(m_className, e);
            } finally {
                classAsStream.close();
            }
        } catch (Exception e) {
            throw new InstrumentationException(
                    "could not add annotations to bytecode of class [" + className + "] due to: " + e.toString(), e
            );
        }
        return true;
    }

    /**
     * Inserts an annotation on class level.
     *
     * @param annotation the annotation
     */
    public void insertClassAnnotation(final RawAnnotation annotation) {
        if (m_reader == null) {
            throw new IllegalStateException("annotation enhancer is not initialized");
        }
        if (hasClassAnnotation(annotation)) {
            throw new CompilerException(
                    "duplicate class annotation " + annotation,
                    SourceLocation.render(annotation)
            );
        }
        m_classAnnotations.add(annotation);
    }

    /**
     * Inserts an annotation on field level.
     *
     * @param field      the QDox java field
     * @param annotation the annotation
     */
    public void insertFieldAnnotation(final JavaField field, final RawAnnotation annotation) {
        if (m_reader == null) {
            throw new IllegalStateException("annotation enhancer is not initialized");
        }
        FieldAnnotationInfo info = new FieldAnnotationInfo(field, annotation);
        if (hasFieldAnnotation(info)) {
            throw new CompilerException(
                    "duplicate field annotation " + annotation,
                    SourceLocation.render(annotation)
            );
        }
        m_fieldAnnotations.add(new FieldAnnotationInfo(field, annotation));
    }

    /**
     * Inserts an annotation on method level.
     *
     * @param method     the QDox java method
     * @param annotation the annotation
     */
    public void insertMethodAnnotation(final JavaMethod method, final RawAnnotation annotation) {
        if (m_reader == null) {
            throw new IllegalStateException("annotation enhancer is not initialized");
        }
        MethodAnnotationInfo info = new MethodAnnotationInfo(method, annotation);
        if (hasMethodAnnotation(info)) {
            throw new ParseException(
                    "duplicate method annotation " + annotation,
                    SourceLocation.render(annotation)
            );
        }
        m_methodAnnotations.add(new MethodAnnotationInfo(method, annotation));
    }

    /**
     * Inserts an annotation on constructor level.
     *
     * @param constructor the QDox java method
     * @param annotation  the annotation
     */
    public void insertConstructorAnnotation(final JavaMethod constructor, final RawAnnotation annotation) {
        if (m_reader == null) {
            throw new IllegalStateException("annotation enhancer is not initialized");
        }
        MethodAnnotationInfo info = new MethodAnnotationInfo(constructor, annotation);
        if (hasConstructorAnnotation(info)) {
            throw new CompilerException(
                    "duplicate constructor annotation " + annotation,
                    SourceLocation.render(annotation)
            );
        }
        m_constructorAnnotations.add(new MethodAnnotationInfo(constructor, annotation));
    }

    /**
     * Writes the enhanced class to file.
     *
     * @param destDir the destination directory
     */
    public void write(final String destDir) {
        if (m_reader == null) {
            throw new IllegalStateException("annotation enhancer is not initialized");
        }

        ClassWriter writer = new ClassWriter(true);
        // note: annotation validation exception will be throwned here - if any
        m_reader.accept(new AnnotationMungingVisitor(writer), false);

        final String filename = destDir + File.separator + m_classFileName;
        File file = new File(filename);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            // directory does not exist create all directories in the filename
            if (!parentFile.mkdirs()) {
                throw new CompilerException(
                        "could not create dir structure needed to write file " + filename + " to disk"
                );
            }
        }
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filename);
            os.write(writer.toByteArray());
        } catch (IOException e) {
            throw new CompilerException("could not write compiled class file to disk [" + filename + "]", e);
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                throw new CompilerException("could not close file output stream for [" + filename + "]", e);
            }
        }
    }

    /**
     * Returns the VM desc for a QDox field.
     *
     * @param field the QDox field
     * @return the desc
     */
    public static String getFieldDesc(final JavaField field) {
        return getDescForQDoxType(field.getType());
    }

    /**
     * Returns the VM desc for a QDox method.
     *
     * @param method the QDox method
     * @return the desc
     */
    public static String getMethodDesc(final JavaMethod method) {
        StringBuffer sig = new StringBuffer();
        sig.append('(');
        final String[] methodParamTypes = new String[method.getParameters().length];
        for (int i = 0; i < methodParamTypes.length; i++) {
            sig.append(getDescForQDoxType(method.getParameters()[i].getType()));
        }
        sig.append(')');
        com.thoughtworks.qdox.model.Type returns = method.getReturns();
        sig.append(getDescForQDoxType(returns));
        return sig.toString();
    }

    /**
     * Returns the VM desc for a QDox Type.
     *
     * @param type the qdox type
     * @return the desc
     */
    public static String getDescForQDoxType(final com.thoughtworks.qdox.model.Type type) {
        if (type == null) {
            return "V"; // constructor
        }
        final StringBuffer desc = new StringBuffer();
        if (type.isArray()) {
            for (int i = type.getDimensions(); i > 0; --i) {
                desc.append('[');
            }
        }
        String value = type.getValue();
        if (value.equals("void")) {
            desc.append("V");
        } else if (value.equals("int")) {
            desc.append("I");
        } else if (value.equals("long")) {
            desc.append("J");
        } else if (value.equals("short")) {
            desc.append("S");
        } else if (value.equals("float")) {
            desc.append("F");
        } else if (value.equals("double")) {
            desc.append("D");
        } else if (value.equals("boolean")) {
            desc.append("Z");
        } else if (value.equals("char")) {
            desc.append("C");
        } else if (value.equals("byte")) {
            desc.append("B");
        } else {
            desc.append('L').append(value.replace('.', '/')).append(';');
        }
        return desc.toString();
    }

    /**
     * Puts the annotations in the class bytecode as Java 5 RuntimeVisibleAnnotations.
     *
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
     */
    private class AnnotationMungingVisitor extends ClassAdapter {
        private static final String INIT_METHOD_NAME = "<init>";

        public AnnotationMungingVisitor(final ClassVisitor cv) {
            super(cv);
        }

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            // handles runtime retention to expose 1.4 annotations under 1.5
            boolean hasRetention = false;
            for (Iterator iterator = m_classAnnotations.iterator(); iterator.hasNext();) {
                RawAnnotation rawAnnotation = (RawAnnotation) iterator.next();
                if (rawAnnotation.getName().equals("java.lang.annotation.Retention")) {
                    hasRetention = true;
                    break;
                }
            }
            if (hasRetention) {
                access |= org.objectweb.asm.Opcodes.ACC_ANNOTATION;
                interfaces = new String[]{"java/lang/annotation/Annotation"};
            }
            super.visit(version, access, name, signature, superName, interfaces);
        }

        public FieldVisitor visitField(
                final int access,
                final String name,
                final String desc,
                final String signature,
                final Object value) {
            FieldVisitor fieldVisitor = super.visitField(access, name, desc, signature, value);
            for (Iterator it = m_fieldAnnotations.iterator(); it.hasNext();) {
                final FieldAnnotationInfo annotationInfo = (FieldAnnotationInfo)it.next();
                String fieldDesc = getFieldDesc(annotationInfo.field);
                if (annotationInfo.field.getName().equals(name) &&
                    fieldDesc.equals(desc)) {
                    final AnnotationVisitor bytecodeMunger = fieldVisitor.visitAnnotation(
                            Type.getDescriptor(annotationInfo.annotation.getAnnotationClass()),
                            true
                    );
                    AnnotationParser.parse(bytecodeMunger, annotationInfo.annotation);
                    bytecodeMunger.visitEnd();
                    m_messageHandler.accept(SourceLocation.render(annotationInfo.annotation));
                }
            }
            return fieldVisitor;
        }

        public MethodVisitor visitMethod(
                final int access,
                final String name,
                final String desc,
                final String signature,
                final String[] exceptions) {
            MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
            if (name.equals(INIT_METHOD_NAME)) {
                for (Iterator it = m_constructorAnnotations.iterator(); it.hasNext();) {
                    final MethodAnnotationInfo annotationInfo = (MethodAnnotationInfo)it.next();
                    if (getMethodDesc(annotationInfo.method).equals(desc)) {
                        final AnnotationVisitor bytecodeMunger = methodVisitor.visitAnnotation(
                                Type.getDescriptor(annotationInfo.annotation.getAnnotationClass()),
                                true
                        );
                        AnnotationParser.parse(bytecodeMunger, annotationInfo.annotation);
                        bytecodeMunger.visitEnd();
                        m_messageHandler.accept(SourceLocation.render(annotationInfo.annotation));
                    }
                }
            } else {
                for (Iterator it = m_methodAnnotations.iterator(); it.hasNext();) {
                    final MethodAnnotationInfo annotationInfo = (MethodAnnotationInfo)it.next();
                    if (annotationInfo.method.getName().equals(name) &&
                        getMethodDesc(annotationInfo.method).equals(desc)) {
                        final AnnotationVisitor bytecodeMunger;
                        if (annotationInfo.annotation.isAnnotationDefaultValue()) {
                            bytecodeMunger = methodVisitor.visitAnnotationDefault();
                            AnnotationParser.parse(bytecodeMunger, annotationInfo.annotation, Type.getReturnType(desc).getDescriptor());
                        } else {
                            bytecodeMunger = methodVisitor.visitAnnotation(
                                    Type.getDescriptor(annotationInfo.annotation.getAnnotationClass()),
                                    true
                            );
                            AnnotationParser.parse(bytecodeMunger, annotationInfo.annotation);
                        }
                        bytecodeMunger.visitEnd();
                        m_messageHandler.accept(SourceLocation.render(annotationInfo.annotation));
                    }
                }
            }
            return methodVisitor;
        }

        public void visitEnd() {
            for (Iterator it = m_classAnnotations.iterator(); it.hasNext();) {
                final RawAnnotation rawAnnotation = (RawAnnotation)it.next();
                final AnnotationVisitor bytecodeMunger = visitAnnotation(
                        Type.getDescriptor(rawAnnotation.getAnnotationClass()),
                        true
                );
                AnnotationParser.parse(bytecodeMunger, rawAnnotation);
                bytecodeMunger.visitEnd();
                m_messageHandler.accept(SourceLocation.render(rawAnnotation));
            }
            super.visitEnd();
        }

    }

    /**
     * Two FieldAnnotationInfo are equals if method equals and annotation class is equals ie RawAnnotation is equals.
     * Used to ensure no annotation duplicate
     *
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
     */
    private static class FieldAnnotationInfo {
        public final RawAnnotation annotation;
        public final JavaField field;

        public FieldAnnotationInfo(final JavaField field, final RawAnnotation attribute) {
            this.field = field;
            this.annotation = attribute;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof FieldAnnotationInfo)) {
                return false;
            }

            final FieldAnnotationInfo fieldAnnotationInfo = (FieldAnnotationInfo)o;

            if (!annotation.equals(fieldAnnotationInfo.annotation)) {
                return false;
            }
            if (!field.equals(fieldAnnotationInfo.field)) {
                return false;
            }

            return true;
        }

        public int hashCode() {
            int result;
            result = annotation.hashCode();
            result = 29 * result + field.hashCode();
            return result;
        }
    }

    /**
     * Two MethodAnnotationInfo are equals if method equals and annotation class is equals ie RawAnnotation is equals.
     * Used to ensure no annotation duplicate.
     *
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
     */
    private static class MethodAnnotationInfo {
        public final RawAnnotation annotation;
        public final JavaMethod method;

        public MethodAnnotationInfo(final JavaMethod method, final RawAnnotation attribute) {
            this.method = method;
            this.annotation = attribute;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof MethodAnnotationInfo)) {
                return false;
            }

            final MethodAnnotationInfo methodAnnotationInfo = (MethodAnnotationInfo)o;

            if (!annotation.equals(methodAnnotationInfo.annotation)) {
                return false;
            }
            if (!method.equals(methodAnnotationInfo.method)) {
                return false;
            }

            return true;
        }

        public int hashCode() {
            int result;
            result = annotation.hashCode();
            result = 29 * result + method.hashCode();
            return result;
        }
    }

    /**
     * Checks if the class has already the given annotation
     * <p/>
     * Note: rely on RawAnnotation.equals
     *
     * @param annotation
     * @return
     */
    private boolean hasClassAnnotation(RawAnnotation annotation) {
        return m_classAnnotations.contains(annotation);
    }

    /**
     * Checks if the method has already the given annotation
     * <p/>
     * Note: rely on RawAnnotation.equals
     *
     * @param annotation
     * @return
     */
    private boolean hasMethodAnnotation(MethodAnnotationInfo annotation) {
        return m_methodAnnotations.contains(annotation);
    }

    /**
     * Checks if the field has already the given annotation
     * <p/>
     * Note: rely on RawAnnotation.equals
     *
     * @param annotation
     * @return
     */
    private boolean hasFieldAnnotation(FieldAnnotationInfo annotation) {
        return m_fieldAnnotations.contains(annotation);
    }

    /**
     * Checks if the constructor has already the given annotation
     * <p/>
     * Note: rely on RawAnnotation.equals
     *
     * @param annotation
     * @return
     */
    private boolean hasConstructorAnnotation(MethodAnnotationInfo annotation) {
        return m_constructorAnnotations.contains(annotation);
    }

    public String getClassName() {
        return m_className;
    }

    public String getClassFileName() {
        return m_classFileName;
    }

    public ClassLoader getClassLoader() {
        return m_loader;
    }
}

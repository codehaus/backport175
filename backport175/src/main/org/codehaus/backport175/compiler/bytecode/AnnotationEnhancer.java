/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler.bytecode;

import org.codehaus.backport175.compiler.AnnotationInterfaceRepository;
import org.codehaus.backport175.compiler.CompilerException;
import org.codehaus.backport175.compiler.javadoc.RawAnnotation;
import org.codehaus.backport175.compiler.parser.AnnotationParser;
import org.objectweb.asm.*;

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
 * java.lang.annotation.RetentionPolicy.RUNTIME} annotations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
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
    private URLClassLoader m_loader = null;

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
     * Initializes the enhancer. Must always be called before use.
     *
     * @param className the class name
     * @param classPath the class path
     * @return true if the class was succefully loaded, false otherwise
     */
    public boolean initialize(final String className, final URL[] classPath) {
        try {
            m_className = className;
            m_loader = new URLClassLoader(classPath);
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
            throw new InstrumentationException("could not add annotations to bytecode of class [" + className + "]", e);
        }
        return true;
    }

    /**
     * Inserts an annotation on class level.
     *
     * @param annotation the annotation
     */
    public void insertClassAnnotation(final Object annotation) {
        if (m_reader == null) {
            throw new IllegalStateException("annotation enhancer is not initialized");
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
        final String[] methodParamTypes = new String[method.getParameters().length];
        for (int i = 0; i < methodParamTypes.length; i++) {
            methodParamTypes[i] = convertQDoxTypeNameToJavaTypeName(method.getParameters()[i].getType());
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
        final String[] methodParamTypes = new String[constructor.getParameters().length];
        for (int i = 0; i < methodParamTypes.length; i++) {
            methodParamTypes[i] = convertQDoxTypeNameToJavaTypeName(constructor.getParameters()[i].getType());
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
     * Converts a QDox type name to a Java language declaration equivalent.
     *
     * @param type the qdox type name
     * @return the java type name
     */
    public static String convertQDoxTypeNameToJavaTypeName(final com.thoughtworks.qdox.model.Type type) {
        StringBuffer dim = new StringBuffer();
        if (type.isArray()) {
            for (int i = type.getDimensions(); i > 0; --i) {
                dim.append("[]");
            }
        }
        return type.getValue() + dim;
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

        public FieldVisitor visitField(
                final int access,
                final String name,
                final String desc,
                final String signature,
                final Object value) {
            FieldVisitor fieldVisitor = super.visitField(access, name, desc, signature, value);
            for (Iterator it = m_fieldAnnotations.iterator(); it.hasNext();) {
                final FieldAnnotationInfo annotationInfo = (FieldAnnotationInfo)it.next();
                final Class annotationInterface = AnnotationInterfaceRepository.getAnnotationInterfaceFor(
                        annotationInfo.annotation.getName()
                );
                final AnnotationVisitor bytecodeMunger = fieldVisitor.visitAnnotation(
                        getTypeDesc(annotationInterface.getName()), true
                );
                AnnotationParser.parse(bytecodeMunger, annotationInterface, annotationInfo.annotation);
                bytecodeMunger.visitEnd();
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
                    final Class annIntf = AnnotationInterfaceRepository.getAnnotationInterfaceFor(
                            annotationInfo.annotation.getName()
                    );
                    final AnnotationVisitor bytecodeMunger = methodVisitor.visitAnnotation(
                            getTypeDesc(annIntf.getName()), true
                    );
                    AnnotationParser.parse(bytecodeMunger, annIntf, annotationInfo.annotation);
                    bytecodeMunger.visitEnd();
                }
            } else {
                for (Iterator it = m_methodAnnotations.iterator(); it.hasNext();) {
                    final MethodAnnotationInfo annotationInfo = (MethodAnnotationInfo)it.next();
                    final Class annIntf = AnnotationInterfaceRepository.getAnnotationInterfaceFor(
                            annotationInfo.annotation.getName()
                    );
                    final AnnotationVisitor bytecodeMunger = methodVisitor.visitAnnotation(
                            getTypeDesc(annIntf.getName()), true
                    );
                    AnnotationParser.parse(bytecodeMunger, annIntf, annotationInfo.annotation);
                    bytecodeMunger.visitEnd();
                }
            }
            return methodVisitor;
        }

        public void visitEnd() {
            for (Iterator it = m_classAnnotations.iterator(); it.hasNext();) {
                final RawAnnotation rawAnnotation = (RawAnnotation)it.next();
                final Class annIntf = AnnotationInterfaceRepository.getAnnotationInterfaceFor(rawAnnotation.getName());
                final AnnotationVisitor bytecodeMunger = visitAnnotation(getTypeDesc(annIntf.getName()), true);
                AnnotationParser.parse(bytecodeMunger, annIntf, rawAnnotation);
                bytecodeMunger.visitEnd();
            }
            super.visitEnd();
        }

        private String getTypeDesc(final String className) {
            return "L" + className.replace('.', '/') + ';';
        }
    }

    /**
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
     */
    private static class FieldAnnotationInfo {
        public final RawAnnotation annotation;
        public final JavaField field;

        public FieldAnnotationInfo(final JavaField field, final RawAnnotation attribute) {
            this.field = field;
            this.annotation = attribute;
        }
    }

    /**
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
     */
    private static class MethodAnnotationInfo {
        public final RawAnnotation annotation;
        public final JavaMethod method;

        public MethodAnnotationInfo(final JavaMethod method, final RawAnnotation attribute) {
            this.method = method;
            this.annotation = attribute;
        }
    }
}

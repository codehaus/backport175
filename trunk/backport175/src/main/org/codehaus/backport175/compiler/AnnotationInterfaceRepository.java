/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler;

import java.util.*;
import java.io.InputStream;
import java.io.FileInputStream;

/**
 * Manages the annotation interface and their mappings to aliases (if there is an alias else the regular classname).
 * <p/>
 * An 'alias' is a shorter name that can be used in the source code instead of the fully qualified name of the
 * interface. These are defined in the external 'annotation.properties' file, which needs fed to the compiler.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AnnotationInterfaceRepository {
    /**
     * Map with the registered annotations mapped to their interface implementation classes.
     */
    private static final Map ANNOTATION_ALIAS_INTERFACE_MAP = new HashMap();

    /**
     * The annotations properties file define by the user.
     */
    public static final Properties ANNOTATION_DEFINITION = new Properties();

    /**
     * Registers an annotation interface into the repository.
     *
     * @param interfaceClass
     */
    public static void registerAnnotationInterface(final Class interfaceClass) {
        ANNOTATION_ALIAS_INTERFACE_MAP.put(interfaceClass.getName(), interfaceClass);
    }

    /**
     * Registers the annotation property files.
     *
     * @param propertiesFiles
     * @param loader
     */
    public static void registerPropertyFiles(final String[] propertiesFiles, final ClassLoader loader) {
        if (propertiesFiles == null) {
            return;
        }
        InputStream in = null;
        for (int i = 0; i < propertiesFiles.length; i++) {
            String propertiesFile = propertiesFiles[i];
            try {
                in = new FileInputStream(propertiesFile);
                ANNOTATION_DEFINITION.load(in);
            } catch (Exception e) {
                String message = "annotation properties file " + propertiesFile + " can not be loaded: " + e.toString();
                AnnotationC.logWarning(message);
                throw new CompilerException(message);
            } finally {
                try {
                    in.close();
                } catch (Exception e) {
                    ;
                }
            }
        }

        for (Iterator it = ANNOTATION_DEFINITION.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            String name = ((String)entry.getKey()).trim();
            String className = ((String)entry.getValue()).trim();
            Class annotationInterfaceClass;
            if (className.equals("")) {
                throw new CompilerException("no annotation interface mapped to the alias [" + name + ']');
            } else {
                try {
                    annotationInterfaceClass = Class.forName(className, false, loader);
                } catch (ClassNotFoundException e) {
                    throw new CompilerException("[" + className + "] could not be found on system classpath or class path provided as argument to the compiler");
                }
            }
            AnnotationC.logInfo("register annotation alias [" + name + " :: " + className + ']');

            ANNOTATION_ALIAS_INTERFACE_MAP.put(name, annotationInterfaceClass);
        }
    }

    /**
     * Returns the annotation interface class for a specific annotation or NULL if the annotation name is unknown.
     *
     * @param annotationName
     * @return the interface class for the annotation or NULL if the annotation name is unknown
     */
    public static Class getAnnotationInterfaceFor(final String annotationName) {
        final Class annotationInterfaceClass;
        Object klass = ANNOTATION_ALIAS_INTERFACE_MAP.get(annotationName);
        if (klass != null) {
            annotationInterfaceClass = (Class) klass;
        }  else {
            try {
                annotationInterfaceClass = Class.forName(annotationName);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        if (!annotationInterfaceClass.isInterface()) {
            throw new CompilerException("annotation class is not defined as an interface for " + annotationName);
        }
        return annotationInterfaceClass;
    }
}

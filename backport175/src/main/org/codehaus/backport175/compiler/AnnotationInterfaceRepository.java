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
    private static final Properties ANNOTATION_DEFINITION = new Properties();

    /**
     * Registers an annotation interface into the repository.
     *
     * @param interfaceName
     * @param loader
     */
    public static void registerAnnotationInterface(final String interfaceName, final ClassLoader loader) {
        final Class interfaceClass = loadAnnotationInterface(interfaceName, loader);
        if (interfaceClass != null) {
            ANNOTATION_ALIAS_INTERFACE_MAP.put(interfaceName, interfaceClass);
        }
    }

    /**
     * Registers the annotation property files.
     *
     * @param propertiesFiles
     * @param loader
     */
    public static void registerPropertiesFiles(final String[] propertiesFiles, final ClassLoader loader) {
        if (propertiesFiles == null) {
            return;
        }
        loadPropertiesFiles(propertiesFiles);
        loadAnnotationInterfacesDefinedInPropertiesFiles(loader);
    }

    /**
     * Returns the annotation interface class for a specific annotation or NULL if the annotation name is unknown.
     *
     * @param annotationName
     * @param loader
     * @return the interface class for the annotation or NULL if the annotation name is unknown
     */
    public static Class getAnnotationInterfaceFor(final String annotationName, final ClassLoader loader) {
        Object value = ANNOTATION_ALIAS_INTERFACE_MAP.get(annotationName);
        if (value != null) {
            // interface found in map
            final Class interfaceClass = (Class)value;
            if (!interfaceClass.isInterface()) {
                throw new CompilerException("annotation class [" + interfaceClass.getName() + "] is not an interface");
            }
            return interfaceClass;
        }
        // interface not found in map, resolve class and put it in map
        final Class interfaceClass = loadAnnotationInterface(annotationName, loader);
        if (interfaceClass != null && !interfaceClass.isInterface()) {
            ANNOTATION_ALIAS_INTERFACE_MAP.put(annotationName, interfaceClass);
        }
        return interfaceClass;
    }

    /**
     * Loads the annotation interface. Has logic to handle resolvment of inner classes specific with a dot.
     *
     * @param interfaceName
     * @param loader
     * @return the class if resolved else null
     */
    private static Class loadAnnotationInterface(final String interfaceName, final ClassLoader loader) {
        Class interfaceClass = null;
        try {
            interfaceClass = Class.forName(interfaceName, false, loader);
        } catch (ClassNotFoundException e) {
            int index = interfaceName.lastIndexOf('.');
            if (index != -1) {
                // recusively search for potential inner classes
                String split1 = interfaceName.substring(0, index);
                String split2 = interfaceName.substring(index + 1, interfaceName.length());
                String innerClassInterface = split1 + '$' + split2;
                interfaceClass = loadAnnotationInterface(innerClassInterface, loader);
            }
        }
        return interfaceClass;
    }

    /**
     * Loads the property files passed to the compiler.
     *
     * @param propertiesFiles
     */
    private static void loadPropertiesFiles(final String[] propertiesFiles) {
        InputStream in = null;
        for (int i = 0; i < propertiesFiles.length; i++) {
            final String propertiesFile = propertiesFiles[i];
            try {
                in = new FileInputStream(propertiesFile);
                ANNOTATION_DEFINITION.load(in);
            } catch (Exception e) {
                throw new CompilerException(
                        "annotation properties file " + propertiesFile + " can not be loaded: " + e.toString()
                );
            } finally {
                try {
                    in.close();
                } catch (Exception e) {
                    ;
                }
            }
        }
    }

    /**
     * Loads the annotation interface defined in the property files.
     *
     * @param loader
     */
    private static void loadAnnotationInterfacesDefinedInPropertiesFiles(final ClassLoader loader) {
        for (Iterator it = ANNOTATION_DEFINITION.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            String name = ((String)entry.getKey()).trim();
            String className = ((String)entry.getValue()).trim();
            Class annotationInterfaceClass;
            if (className.equals("")) {
                throw new CompilerException(
                        "no annotation interface mapped to the alias [" + name +
                        "] defined in the 'annotation.properties' file"
                );
            } else {
                annotationInterfaceClass = loadAnnotationInterface(className, loader);
            }
            AnnotationC.logInfo("register annotation alias [" + name + " :: " + className + ']');

            ANNOTATION_ALIAS_INTERFACE_MAP.put(name, annotationInterfaceClass);
        }
    }
}

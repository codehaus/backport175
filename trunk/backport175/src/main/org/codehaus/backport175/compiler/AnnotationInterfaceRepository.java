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
 * Manages the reader interface and their mappings to aliases (if there is an alias else the regular classname).
 * <p/>
 * An 'alias' is a shorter name that can be used in the source code instead of the fully qualified name of the
 * interface. These are defined in the external 'reader.properties' file, which needs fed to the compiler.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AnnotationInterfaceRepository {

    /**
     * Map with the registered annotations mapped to their interface implementation classes.
     */
    private final Map m_aliasInterfaceMap = new HashMap();

    /**
     * The annotations properties file define by the user.
     */
    private final Properties m_properties = new Properties();

    /**
     * String list of doclet not found, to avoid fallback lookup everytime
     */
    private final List m_ignoredDocletNames = new ArrayList();

    /**
     * The handler we report to
     */
    private MessageHandler m_handler;

    /**
     * Creates a new interface repository.
     *
     * @param handler
     */
    public AnnotationInterfaceRepository(final MessageHandler handler) {
        m_handler = handler;
    }

    /**
     * Registers an annotation interface into the repository.
     *
     * @param name
     * @param interfaceClass
     */
    private void registerAnnotationInterface(final String name, final Class interfaceClass) {
        if (m_aliasInterfaceMap.containsValue(interfaceClass)) {
            for (Iterator iterator = m_aliasInterfaceMap.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                if (entry.getValue().equals(interfaceClass)) {
                    throw new CompilerException("unable to register [" + interfaceClass.getName() +
                            "] as [" + name + "] since it is already registered under the name [" +
                            entry.getKey() + ']');
                }
            }
        }
        m_handler.info("register annotation [" + name + " :: " + interfaceClass + ']');
        m_aliasInterfaceMap.put(name, interfaceClass);
    }

    /**
     * Registers the annotation property files.
     *
     * @param propertiesFiles
     * @param loader
     */
    public void registerPropertiesFiles(final String[] propertiesFiles, final ClassLoader loader) {
        if (propertiesFiles == null) {
            return;
        }
        loadPropertiesFiles(propertiesFiles);
        loadAnnotationInterfacesDefinedInPropertiesFiles(loader);
    }

    /**
     * Loads the property files passed to the compiler.
     *
     * @param propertiesFiles
     */
    private void loadPropertiesFiles(final String[] propertiesFiles) {
        InputStream in = null;
        for (int i = 0; i < propertiesFiles.length; i++) {
            final String propertiesFile = propertiesFiles[i];
            try {
                in = new FileInputStream(propertiesFile);
                m_properties.load(in);
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
    private void loadAnnotationInterfacesDefinedInPropertiesFiles(final ClassLoader loader) {
        for (Iterator it = m_properties.entrySet().iterator(); it.hasNext();) {
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
                annotationInterfaceClass = loadClassHandlingNestedSyntax(className, loader);
                if (annotationInterfaceClass == null) {
                    throw new CompilerException("[" + className + "] could not be found on system classpath or class path provided as argument to the compiler");
                }
            }
            registerAnnotationInterface(name, annotationInterfaceClass);
        }
    }

    /**
     * Returns the annotation interface class for a specific annotation or NULL if the annotation name is unknown.
     * <p/>
     * Hanldes nested class with either '$' or '.' ('$' is faster).
     * Classes are added to the mapping map when found.
     *
     * @param annotationName
     * @param loader the loader where to search for the class if not found in aliased ones
     * @return the interface class for the annotation or NULL if the annotation name is unknown
     */
    public Class getAnnotationInterfaceFor(final String annotationName, ClassLoader loader) {
        // check ignored list
        if (m_ignoredDocletNames.contains(annotationName)) {
            return null;
        }

        // look up, and register if found, else add to ignore list
        final Class annotationInterfaceClass;
        Object klass = m_aliasInterfaceMap.get(annotationName);
        if (klass != null) {
            annotationInterfaceClass = (Class) klass;
        }  else {
            annotationInterfaceClass = loadClassHandlingNestedSyntax(annotationName, loader);
            if (annotationInterfaceClass == null) {
                // add it to ignored ones
                //TODO log ??
                m_ignoredDocletNames.add(annotationName);
                return null;
            } else {
                // add it to the alias for faster lookup next time
                registerAnnotationInterface(annotationName, annotationInterfaceClass);
            }
        }

        if (!annotationInterfaceClass.isInterface()) {
            throw new CompilerException("annotation class is not defined as an interface for " + annotationName);
        }
        return annotationInterfaceClass;
    }

    /**
     * Try to load the given class from its className.
     * <p/>
     * Nested classes are handled thru a fallback mechanism so that foo.Bar.Nested is resolved in foo.Bar$Nested
     * when not found at the first lookup.
     *
     * @param className
     * @param loader
     * @return
     */
    private static Class loadClassHandlingNestedSyntax(String className, ClassLoader loader) {
        try {
            return Class.forName(className, false, loader);
        } catch (Throwable e) {//NoClassDefFoundError / ClassNotFound etc
            int lastDot = className.lastIndexOf('.');
            if (lastDot > 0) {
                char[] classNameHopes = className.toCharArray();
                classNameHopes[lastDot] = '$';
                return loadClassHandlingNestedSyntax(new String(classNameHopes), loader);
            } else {
                return null;
            }
        }
    }
}

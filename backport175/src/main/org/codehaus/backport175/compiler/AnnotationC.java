/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.DocletTag;

import org.apache.tools.ant.BuildException;
import org.codehaus.backport175.compiler.bytecode.AnnotationEnhancer;
import org.codehaus.backport175.compiler.javadoc.JavaDocParser;
import org.codehaus.backport175.compiler.javadoc.RawAnnotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * <p/>Annotation compiler. <p/>Extracts the annotations from JavaDoc tags and inserts them into the bytecode of the
 * class.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class AnnotationC {
    private static final String COMMAND_LINE_OPTION_DASH = "-";
    private static final String COMMAND_LINE_OPTION_VERBOSE = "-verbose";
    private static final String COMMAND_LINE_OPTION_CUSTOM = "-config";
    private static final String COMMAND_LINE_OPTION_SRC = "-src";
    private static final String COMMAND_LINE_OPTION_SRCFILES = "-srcfiles";
    private static final String COMMAND_LINE_OPTION_SRCINCLUDES = "-srcincludes";
    private static final String COMMAND_LINE_OPTION_CLASSES = "-classes";
    private static final String COMMAND_LINE_OPTION_DEST = "-dest";

    private static final String FILE_SEPARATOR = File.separator;

    /**
     * Verbose logging.
     */
    private static boolean s_verbose = false;

    /**
     * The class loader.
     */
    private static URLClassLoader s_loader;

    /**
     * Runs the compiler from the command line.
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 4) {
            printUsage();
        }
        Map commandLineOptions = parseCommandLineOptions(args);

        String propertiesFilesPath = (String)commandLineOptions.get(COMMAND_LINE_OPTION_CUSTOM);
        List propertiesFilesList = new ArrayList();
        if (propertiesFilesPath != null) {
            StringTokenizer st = new StringTokenizer(propertiesFilesPath, File.pathSeparator);
            while (st.hasMoreTokens()) {
                propertiesFilesList.add(st.nextToken());
            }
        }
        String[] propertiesFiles = (String[])propertiesFilesList.toArray(new String[0]);

        compile(
                (String)commandLineOptions.get(COMMAND_LINE_OPTION_SRC),
                (String)commandLineOptions.get(COMMAND_LINE_OPTION_SRCFILES),
                (String)commandLineOptions.get(COMMAND_LINE_OPTION_SRCINCLUDES),
                (String)commandLineOptions.get(COMMAND_LINE_OPTION_CLASSES),
                (String)commandLineOptions.get(COMMAND_LINE_OPTION_DEST),
                propertiesFiles
        );
    }

    /**
     * Compiles the annotations.
     *
     * @param srcDirList
     * @param srcFileList
     * @param classPath
     * @param destDir
     * @param annotationPropetiesFiles
     */
    private static void compile(
            final String srcDirList,
            final String srcFileList,
            final String srcFileIncludes,
            final String classPath,
            String destDir,
            final String[] annotationPropetiesFiles) {
        if (srcDirList == null && srcFileList == null && srcFileIncludes == null) {
            throw new IllegalArgumentException("one of src or srcfiles or srcincludes must be not null");
        }
        if ((srcDirList != null && srcFileList != null) ||
            (srcDirList != null && srcFileIncludes != null) ||
            (srcFileList != null && srcFileIncludes != null)) {
            throw new IllegalArgumentException("maximum one of src, srcfiles or srcincludes must be not null");
        }
        if (classPath == null) {
            throw new IllegalArgumentException("class path can not be null");
        }
        if (destDir == null) {
            destDir = classPath;
        }

        String[] srcDirs = new String[0];
        String[] srcFiles = new String[0];
        if (srcDirList != null) {
            srcDirs = split(srcDirList, File.pathSeparator);
        } else if (srcFileList != null) {
            srcFiles = split(srcFileList, FILE_SEPARATOR);
        } else {
            srcFiles = loadSourceList(srcFileIncludes);
        }

        compile(s_verbose, srcDirs, srcFiles, split(classPath, File.pathSeparator), destDir, annotationPropetiesFiles);
    }

    /**
     * Compiles the annotations.
     *
     * @param verbose
     * @param srcDirs
     * @param srcFiles
     * @param classpath
     * @param destDir
     * @param annotationPropertiesFiles
     */
    public static void compile(
            final boolean verbose,
            final String[] srcDirs,
            final String[] srcFiles,
            final String[] classpath,
            final String destDir,
            final String[] annotationPropertiesFiles) {

        s_verbose = verbose;
        URL[] classPath = new URL[classpath.length];
        try {
            for (int i = 0; i < classpath.length; i++) {
                classPath[i] = new File(classpath[i]).toURL();
            }
            s_loader = new URLClassLoader(classPath, AnnotationC.class.getClassLoader());
        } catch (MalformedURLException e) {
            String message = "URL [" + classPath + "] is not valid: " + e.toString();
            logError(message);
            throw new CompilerException(message, e);
        }

        String destDirToUse = destDir;
        if (destDir == null) {
            if (classpath.length != 1) {
                throw new CompilerException("destDir must be specified since classpath is composite");
            }
            destDirToUse = classpath[0];
        }

        JavaDocParser.addClassLoaderToSearchPath(s_loader);

        logInfo("parsing source dirs:");
        for (int i = 0; i < srcDirs.length; i++) {
            logInfo("    " + srcDirs[i]);
        }
        JavaDocParser.addSourceTrees(srcDirs);

        for (int i = 0; i < srcFiles.length; i++) {
            logInfo("    " + srcFiles[i]);
            JavaDocParser.addSource(srcFiles[i]);
        }

        AnnotationInterfaceRepository.registerPropertyFiles(annotationPropertiesFiles, s_loader);

        doCompile(classPath, destDirToUse);
    }

    /**
     * Compiles the annotations.
     *
     * @param classPath
     * @param destDir
     */
    private static void doCompile(final URL[] classPath, final String destDir) {
        logInfo("compiling annotations...");
        logInfo("note: if no output is seen, then nothing is compiled");

        // get all the classes
        JavaClass[] classes = JavaDocParser.getJavaClasses();
        for (int i = 0; i < classes.length; i++) {
            JavaClass clazz = classes[i];
            logInfo("parsing class [" + clazz.getFullyQualifiedName() + ']');
            try {
                AnnotationEnhancer enhancer = new AnnotationEnhancer();
                if (enhancer.initialize(clazz.getFullyQualifiedName(), classPath)) {
                    handleClassAnnotations(enhancer, clazz);
                    handleInnerClassAnnotations(enhancer, clazz);
                    JavaMethod[] methods = clazz.getMethods();
                    for (int j = 0; j < methods.length; j++) {
                        JavaMethod method = methods[j];
                        if (method.isConstructor()) {
                            handleConstructorAnnotations(enhancer, method);
                        } else {
                            handleMethodAnnotations(enhancer, method);
                        }
                    }
                    JavaField[] fields = clazz.getFields();
                    for (int j = 0; j < fields.length; j++) {
                        handleFieldAnnotations(enhancer, fields[j]);
                    }

                    // write enhanced class to disk
                    enhancer.write(destDir);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                logWarning(
                        "could not compile annotations for class ["
                        + clazz.getFullyQualifiedName() + "] due to: " + e.toString()
                );
            }
        }
        logInfo("compiled classes written to " + destDir);
        logInfo("compilation successful");
    }

    /**
     * Handles the class annotations.
     *
     * @param enhancer
     * @param clazz
     */
    private static void handleClassAnnotations(final AnnotationEnhancer enhancer, final JavaClass clazz) {
        DocletTag[] tags = clazz.getTags();
        for (int i = 0; i < tags.length; i++) {
            RawAnnotation rawAnnotation = getRawAnnotation(tags[i]);
            if (rawAnnotation == null) {
                continue;
            }
            enhancer.insertClassAnnotation(rawAnnotation);
            logInfo(
                    "    processing class annotation [" + rawAnnotation.getName() + " @ "
                    + clazz.getFullyQualifiedName() + ']'
            );
        }
    }

    /**
     * Handles the method annotations.
     *
     * @param enhancer
     * @param method
     */
    private static void handleMethodAnnotations(final AnnotationEnhancer enhancer, final JavaMethod method) {
        DocletTag[] tags = method.getTags();
        for (int i = 0; i < tags.length; i++) {
            RawAnnotation rawAnnotation = getRawAnnotation(tags[i]);
            if (rawAnnotation == null) {
                continue;
            }
            enhancer.insertMethodAnnotation(method, rawAnnotation);
            logInfo(
                    "    processing method annotation [" + rawAnnotation.getName() + " @ "
                    + method.getParentClass().getName() + '.' +
                    method.getName()
                    + ']'
            );
        }
    }

    /**
     * Handles the constructor annotations.
     *
     * @param enhancer
     * @param constructor
     */
    private static void handleConstructorAnnotations(final AnnotationEnhancer enhancer, final JavaMethod constructor) {
        DocletTag[] tags = constructor.getTags();
        for (int i = 0; i < tags.length; i++) {
            RawAnnotation rawAnnotation = getRawAnnotation(tags[i]);
            if (rawAnnotation == null) {
                continue;
            }
            enhancer.insertConstructorAnnotation(constructor, rawAnnotation);
            logInfo(
                    "    processing constructor annotation [" + rawAnnotation.getName() + " @ "
                    + constructor.getParentClass().getName() + '.' +
                    constructor.getName()
                    + ']'
            );
        }
    }

    /**
     * Handles the field annotations.
     *
     * @param enhancer
     * @param field
     */
    private static void handleFieldAnnotations(final AnnotationEnhancer enhancer, final JavaField field) {
        DocletTag[] tags = field.getTags();
        for (int i = 0; i < tags.length; i++) {
            RawAnnotation rawAnnotation = getRawAnnotation(tags[i]);
            if (rawAnnotation == null) {
                continue;
            }
            enhancer.insertFieldAnnotation(field, rawAnnotation);
            logInfo("    processing field annotation [" + rawAnnotation.getName() + " @ " + field.getName() + ']');
        }
    }

    /**
     * Handles the inner class annotations.
     *
     * @param enhancer
     * @param clazz
     */
    private static void handleInnerClassAnnotations(final AnnotationEnhancer enhancer, final JavaClass clazz) {
        JavaClass[] innerClasses = clazz.getInnerClasses();
        for (int i = 0; i < innerClasses.length; i++) {
            handleClassAnnotations(enhancer, innerClasses[i]);
        }
    }

    /**
     * Processes the doclet tags and creates a raw annotation to use for further processing.
     *
     * @param tag the doclet tag
     * @return the raw annotation data
     */
    private static RawAnnotation getRawAnnotation(final DocletTag tag) {
        String annotationName = tag.getName();

        int index = annotationName.indexOf('(');
        if (index != -1) {
            annotationName = annotationName.substring(0, index);
        }

        String interfaceName;
        Class annotationInterface = AnnotationInterfaceRepository.getAnnotationInterfaceFor(annotationName);
        if (annotationInterface != null) {
            interfaceName = annotationInterface.getName();
        } else {
            interfaceName = annotationName;
            try {
                Class interfaceClass = Class.forName(interfaceName, false, s_loader);
                AnnotationInterfaceRepository.registerAnnotationInterface(interfaceClass);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        return JavaDocParser.getRawAnnotation(interfaceName, tag);
    }

    /**
     * Prints the usage.
     */
    private static void printUsage() {
        System.out.println("backport175 (c) 2002-2005 Jonas Bonér, Alexandre Vasseur");
        System.out.println(
                "usage: java [options...] org.codehaus.backport175.compiler.AnnotationC [-verbose] -src <path to src dir> | -srcfiles <list of files> | -srcincludes <path to file> -classes <path to classes dir> [-dest <path to destination dir>] [-config <property file>]"
        );
        System.out.println(
                "       -src <path to src dir> - provides the list of source directories separated by File.pathSeparator"
        );
        System.out.println("       -srcpath <list of files> - provides a comma separated list of source files");
        System.out.println(
                "       -srcincludes <path to file> - provides the path to a file containing the list of source files (one name per line)"
        );
        System.out.println(
                "       -dest <path to destination dir> - optional, if omitted the compiled classes will be written to the initial directory"
        );
        System.out.println(
                "       -config <property file with aliases to the FQN of the annotation interfaces> - optional"
        );
        System.out.println("       -verbose - activates compilation status information");
        System.out.println("");
        System.out.println("Note: only one of -src -srcpath and -srcincludes may be used");

        System.exit(0);
    }

    /**
     * Parses the command line options.
     *
     * @param args the arguments
     * @return a map with the options
     */
    private static Map parseCommandLineOptions(final String[] args) {
        final Map arguments = new HashMap();
        try {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals(COMMAND_LINE_OPTION_VERBOSE)) {
                    s_verbose = true;
                } else if (args[i].startsWith(COMMAND_LINE_OPTION_DASH)) {
                    String option = args[i++];
                    String value = args[i];
                    arguments.put(option, value);
                }
            }
        } catch (Exception e) {
            logError("options list to compiler is not valid");
            System.exit(1);
        }
        return arguments;
    }

    /**
     * Logs an INFO message.
     *
     * @param message the message
     */
    public static void logInfo(final String message) {
        if (s_verbose) {
            System.out.println("backport175::INFO - " + message);
        }
    }

    /**
     * Logs an ERROR message.
     *
     * @param message the message
     */
    public static void logError(final String message) {
        if (s_verbose) {
            System.err.println("backport175::ERROR - " + message);
        }
    }

    /**
     * Logs an WARNING message.
     *
     * @param message the message
     */
    public static void logWarning(final String message) {
        if (s_verbose) {
            System.err.println("backport175::WARNING - " + message);
        }
    }

    /**
     * Splits a string into parts.
     *
     * @param str the string to split
     * @param sep the separator
     * @return the string parts in a string array
     */
    private static String[] split(final String str, final String sep) {
        if (str == null || str.length() == 0) {
            return new String[0];
        }

        int start = 0;
        int idx = str.indexOf(sep, start);
        int len = sep.length();
        List strings = new ArrayList();

        while (idx != -1) {
            strings.add(str.substring(start, idx));
            start = idx + len;
            idx = str.indexOf(sep, start);
        }

        strings.add(str.substring(start));

        return (String[])strings.toArray(new String[strings.size()]);
    }

    /**
     * Load and solve relative to working directory the list of files.
     *
     * @param srcIncludes
     * @return
     */
    private static String[] loadSourceList(final String srcIncludes) {
        File currentDir = new File(".");
        List files = new ArrayList();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(srcIncludes));

            String line = reader.readLine();
            File tmpFile;
            while (line != null) {
                if (line.length() > 0) {
                    tmpFile = new File(currentDir, line);
                    if (!tmpFile.isFile()) {
                        logWarning("file not found: [" + tmpFile + "]");
                    } else {
                        files.add(tmpFile.getAbsolutePath());
                    }
                }
                line = reader.readLine();
            }
        } catch (IOException ioe) {
            throw new BuildException("an error occured while reading from pattern file: " + srcIncludes, ioe);
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    //Ignore exception
                }
            }
        }
        return (String[])files.toArray(new String[files.size()]);
    }
}
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

import org.codehaus.backport175.compiler.bytecode.AnnotationEnhancer;
import org.codehaus.backport175.compiler.javadoc.JavaDocParser;
import org.codehaus.backport175.compiler.javadoc.RawAnnotation;
import org.codehaus.backport175.compiler.javadoc.SourceParseException;
import org.codehaus.backport175.compiler.parser.ParseException;

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
     * Compilation event handler
     */
    private final MessageHandler m_handler;

    /**
     * The class loader
     */
    private final ClassLoader m_loader;

    /**
     * The parser for src files
     */
    private final JavaDocParser m_javaDocParser;

    /**
     * The annotation repository
     */
    private final AnnotationInterfaceRepository m_repository;

    /**
     * Creates a new AnnotationC compiler instance.
     *
     * @param loader
     * @param parser
     * @param repository
     * @param handler
     */
    private AnnotationC(
            final ClassLoader loader,
            final JavaDocParser parser,
            final AnnotationInterfaceRepository repository,
            final MessageHandler handler) {
        m_loader = loader;
        m_javaDocParser = parser;
        m_repository = repository;
        m_handler = handler;
    }

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
                "true".equals(commandLineOptions.get(COMMAND_LINE_OPTION_VERBOSE)),
                (String)commandLineOptions.get(COMMAND_LINE_OPTION_SRC),
                (String)commandLineOptions.get(COMMAND_LINE_OPTION_SRCFILES),
                (String)commandLineOptions.get(COMMAND_LINE_OPTION_SRCINCLUDES),
                (String)commandLineOptions.get(COMMAND_LINE_OPTION_CLASSES),
                (String)commandLineOptions.get(COMMAND_LINE_OPTION_DEST),
                propertiesFiles
        );
    }

    /**
     * Compiles the annotations, called from the main method.
     *
     * @param verbose
     * @param srcDirList
     * @param srcFileList
     * @param classPath
     * @param destDir
     * @param annotationPropetiesFiles
     */
    private static void compile(
            final boolean verbose,
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

        compile(
                srcDirs,
                srcFiles,
                split(classPath, File.pathSeparator),
                destDir,
                annotationPropetiesFiles,
                new MessageHandler.PrintWriter(verbose)
        );
    }

    /**
     * Compiles the annotations.
     *
     * @param srcDirs
     * @param srcFiles
     * @param classpath
     * @param destDir
     * @param annotationPropertiesFiles
     * @param messageHandler
     */
    public static void compile(
            final String[] srcDirs,
            final String[] srcFiles,
            final String[] classpath,
            final String destDir,
            final String[] annotationPropertiesFiles,
            final MessageHandler messageHandler) {

        URL[] classPath = new URL[classpath.length];
        final ClassLoader compilationLoader;
        try {
            for (int i = 0; i < classpath.length; i++) {
                classPath[i] = new File(classpath[i]).toURL();
            }
            compilationLoader = new URLClassLoader(classPath, AnnotationC.class.getClassLoader());
        } catch (MalformedURLException e) {
            String message = "URL [" + classPath + "] is not valid: " + e.toString();
            messageHandler.error(new CompilerException(message, e));
            return;
        }

        String destDirToUse = destDir;
        if (destDir == null) {
            if (classpath.length != 1) {
                messageHandler.error(new CompilerException("destDir must be specified since classpath is composite"));
                return;
            }
            destDirToUse = classpath[0];
        }

        // set up the parser sources
        final JavaDocParser javaDocParser = new JavaDocParser();
        try {
            // add classloader
            javaDocParser.addClassLoaderToSearchPath(compilationLoader);

            // add src dirs
            StringBuffer logDirs = new StringBuffer("parsing source dirs:");
            for (int i = 0; i < srcDirs.length; i++) {
                logDirs.append("\n\t" + srcDirs[i]);
            }
            messageHandler.info(logDirs.toString());
            javaDocParser.addSourceTrees(srcDirs);

            // add src files
            logDirs = new StringBuffer();
            for (int i = 0; i < srcFiles.length; i++) {
                logDirs.append("\n\t" + srcFiles[i]);
                javaDocParser.addSource(srcFiles[i]);
            }
            if (srcFiles.length > 0) {
                messageHandler.info(logDirs.toString());
            }

            final AnnotationInterfaceRepository repository = new AnnotationInterfaceRepository(messageHandler);
            repository.registerPropertiesFiles(annotationPropertiesFiles, compilationLoader);

            final AnnotationC compiler = new AnnotationC(compilationLoader, javaDocParser, repository, messageHandler);

            // do the actual compile
            compiler.doCompile(classPath, destDirToUse);

        } catch (SourceParseException e) {
            messageHandler.error(e);
            return;
        } catch (CompilerException e) {
            messageHandler.error(e);
            return;
        } catch (Throwable t) {
            messageHandler.error(new CompilerException("unexpected exception: " + t.toString(), t));
            return;
        }
    }

    /**
     * Compiles the annotations.
     *
     * @param classPath
     * @param destDir
     */
    private void doCompile(final URL[] classPath, final String destDir) {
        logInfo("compiling annotations...");

        // get all the classes
        JavaClass[] classes = m_javaDocParser.getJavaClasses();
        for (int i = 0; i < classes.length; i++) {
            JavaClass clazz = classes[i];
            logInfo("parsing class [" + clazz.getFullyQualifiedName() + ']');
            try {
                AnnotationEnhancer enhancer = new AnnotationEnhancer(m_handler);
                if (enhancer.initialize(clazz.getFullyQualifiedName(), classPath)) {
                    handleClassAnnotations(enhancer, clazz);
                    //handleInnerClassAnnotations(enhancer, clazz, destDir);
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
            } catch (ParseException pe) {
                m_handler.error(pe);
                // non critical, go on
            } catch (CompilerException ce) {
                m_handler.error(ce);
                return;
            } catch (Throwable t) {
                t.printStackTrace();
                m_handler.error(
                        new CompilerException(
                                "could not compile annotations for class ["//FIXME location
                                + clazz.getFullyQualifiedName() +
                                "] due to: " +
                                t.toString()
                        )
                );
                return;
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
    private void handleClassAnnotations(final AnnotationEnhancer enhancer, final JavaClass clazz) {
        DocletTag[] tags = clazz.getTags();
        for (int i = 0; i < tags.length; i++) {
            RawAnnotation rawAnnotation = getRawAnnotation(
                    tags[i], enhancer.getClassName(), enhancer.getClassFileName()
            );
            if (rawAnnotation == null) {
                continue;
            }
            enhancer.insertClassAnnotation(rawAnnotation);
            logInfo(
                    "\tprocessing class annotation [" + rawAnnotation.getName() + " @ "
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
    private void handleMethodAnnotations(final AnnotationEnhancer enhancer, final JavaMethod method) {
        DocletTag[] tags = method.getTags();
        for (int i = 0; i < tags.length; i++) {
            RawAnnotation rawAnnotation = getRawAnnotation(
                    tags[i], enhancer.getClassName(), enhancer.getClassFileName()
            );
            if (rawAnnotation == null) {
                continue;
            }
            enhancer.insertMethodAnnotation(method, rawAnnotation);
            logInfo(
                    "\tprocessing method annotation [" + rawAnnotation.getName() + " @ "
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
    private void handleConstructorAnnotations(final AnnotationEnhancer enhancer, final JavaMethod constructor) {
        DocletTag[] tags = constructor.getTags();
        for (int i = 0; i < tags.length; i++) {
            RawAnnotation rawAnnotation = getRawAnnotation(
                    tags[i], enhancer.getClassName(), enhancer.getClassFileName()
            );
            if (rawAnnotation == null) {
                continue;
            }
            enhancer.insertConstructorAnnotation(constructor, rawAnnotation);
            logInfo(
                    "\tprocessing constructor annotation [" + rawAnnotation.getName() + " @ "
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
    private void handleFieldAnnotations(final AnnotationEnhancer enhancer, final JavaField field) {
        DocletTag[] tags = field.getTags();
        for (int i = 0; i < tags.length; i++) {
            RawAnnotation rawAnnotation = getRawAnnotation(
                    tags[i], enhancer.getClassName(), enhancer.getClassFileName()
            );
            if (rawAnnotation == null) {
                continue;
            }
            enhancer.insertFieldAnnotation(field, rawAnnotation);
            logInfo("\tprocessing field annotation [" + rawAnnotation.getName() + " @ " + field.getName() + ']');
        }
    }

    /**
     * Processes the doclet tags and creates a raw annotation to use for further processing.
     *
     * @param tag                    the doclet tag
     * @param enclosingClassName
     * @param enclosingClassFileName
     * @return the raw annotation data
     */
    private RawAnnotation getRawAnnotation(
            final DocletTag tag, final String enclosingClassName, final String enclosingClassFileName) {
        String annotationName = tag.getName();
        int index = annotationName.indexOf('(');
        if (index != -1) {
            annotationName = annotationName.substring(0, index);
        }

        Class annotationInterface = m_repository.getAnnotationInterfaceFor(annotationName, m_loader);
        if (annotationInterface == null) {
            // not found, and the AnnotationInterfaceRepository.ANNOTATION_IGNORED has been populated
            //FIXME - raise what ? a warning with location but then we should ignore real javadoc tags like author etc ?
            logInfo(
                    "JavaDoc tag [" + annotationName +
                    "] is not treated as an annotation - class could not be resolved"
                    + " at " + enclosingClassName + " in " + enclosingClassFileName + ", line " +
                    tag.getLineNumber()
            );
            return null;
        }

        return JavaDocParser.getRawAnnotation(annotationInterface, tag, enclosingClassName, enclosingClassFileName);
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
                "       -src <path to src dir> - provides the list of source directories separated by 'File.pathSeparator'"
        );
        System.out.println("       -srcfiles <list of files> - provides a comma separated list of source files");
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
                //-verbose has no value
                if (args[i].equals(COMMAND_LINE_OPTION_VERBOSE)) {
                    arguments.put(COMMAND_LINE_OPTION_VERBOSE, "true");
                } else if (args[i].startsWith(COMMAND_LINE_OPTION_DASH)) {
                    String option = args[i];
                    String value = args[++i];
                    arguments.put(option, value);
                }
            }
        } catch (Exception e) {
            System.err.println("options list to compiler is not valid");
            System.exit(1);
        }
        return arguments;
    }

    /**
     * Logs an INFO message (helper)
     *
     * @param message the message
     */
    public void logInfo(final String message) {
        m_handler.info(message);
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
                        System.err.println("file not found: [" + tmpFile + "]");
                    } else {
                        files.add(tmpFile.getAbsolutePath());
                    }
                }
                line = reader.readLine();
            }
        } catch (IOException ioe) {
            throw new CompilerException("an error occured while reading from pattern file: " + srcIncludes, ioe);
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
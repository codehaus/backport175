/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.ide.intellij;

import com.intellij.openapi.compiler.ClassInstrumentingCompiler;
import com.intellij.openapi.compiler.FileProcessingCompiler;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.ValidityState;
import com.intellij.openapi.compiler.TimestampValidityState;
import com.intellij.openapi.compiler.ValidityStateFactory;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.util.Key;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ActionRunner;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.net.URLClassLoader;
import java.net.URL;

import org.codehaus.backport175.compiler.MessageHandler;
import org.codehaus.backport175.compiler.AnnotationC;
import org.codehaus.backport175.compiler.SourceLocation;
import org.codehaus.backport175.compiler.CompilerException;

import javax.swing.*;

/**
 * An IDEA compiler that is triggered after javac.
 * <p/>
 * It wrapps the AnnotationC and for each error/accepted annotation keep track of them in the
 * message handler wich is later on flushed thru a compilation listener.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class BpCompiler implements ClassInstrumentingCompiler {
    public Project m_project;

    private BufferedMessageHandler m_messageHandler;

    /**
     * All markers have this custom user data to remember them and remove them
     */
    private final static Key MARKER_KEY = new Key(BpCompiler.class.getName());

    public BpCompiler(Project project) {
        m_project = project;
        m_messageHandler = new BufferedMessageHandler(m_project);
    }

    public ProcessingItem[] getProcessingItems(CompileContext compileContext) {
        //TODO: we should link a class with its annotation to ensure correct recompilation
        // using the PSI / javadoc

        // when Anno interface changes
        List item = new ArrayList();
        CompileScope compileScope = compileContext.getCompileScope();
        VirtualFile[] files = compileScope.getFiles(FileTypeManager.getInstance().getFileTypeByExtension("JAVA"), true);
        for (int i = 0; i < files.length; i++) {
            final VirtualFile file = files[i];
            item.add(
                    new ProcessingItem() {
                public VirtualFile getFile() {
                    return file;
                }

                public ValidityState getValidityState() {
                    return new TimestampValidityState(file.getModificationStamp());
                }
            }
            );
        }

        BpLog.info("getProcessingItems.." + item.size());
        return (ProcessingItem[]) item.toArray(new ProcessingItem[]{});
    }

    public ProcessingItem[] process(final CompileContext compileContext, ProcessingItem[] processingItems) {
        // flush messages
        m_messageHandler.flush();

        // correctly compiled
        final List processedItems = new ArrayList();

        BpLog.info("process.." + processingItems.length);

        for (int i = 0; i < processingItems.length; i++) {
            final ProcessingItem processingItem = processingItems[i];
            try {
                // run in an IDEA read action since we access VirtualFiles..
                ActionRunner.runInsideReadAction(
                        new ActionRunner.InterruptibleRunnable() {
                    public void run() throws Exception {
                        VirtualFile vf = processingItem.getFile();

                        BpLog.info("do " + vf.getPath());

                        //IDEA v4.5
                        // we end up in having non java files here.. despite getProcessingItems()
                        if (vf.getPath().endsWith(".java")) {
//                            compileContext.addMessage(
//                                    CompilerMessageCategory.INFORMATION,
//                                    "Annotating " + vf.getName(),
//                                    vf.getUrl(),
//                                    -1,
//                                    -1
//                            );

                            m_messageHandler.vfs.add(vf);
                            
                            final Module module = compileContext.getModuleByFile(vf);
                            VirtualFile destDir = compileContext.getModuleOutputDirectory(module);

                            // project paths
                            ModuleRootManager pathManager = ModuleRootManager.getInstance(module);
                            String[] paths = pathManager.getUrls(OrderRootType.CLASSES_AND_OUTPUT);
                            URL[] pathsU = new URL[paths.length];
                            // IDEA path are URL like and we just need file based path
                            for (int j = 0; j < paths.length; j++) {
                                if (paths[j].startsWith("file:")) {
                                    pathsU[j] = new URL(paths[j]);
                                    paths[j] = paths[j].substring(7);
                                } else if (paths[j].startsWith("jar:")) {
                                    pathsU[j] = new File(paths[j].substring(6, paths[j].length() - 2)).toURL();
                                    paths[j] = paths[j].substring(6, paths[j].length() - 2);
                                } else {
                                continue;//TODO ignore ?
                                }
                            }

                            // get the annotation.properties files which must be in module root
                            // we do not support discovery thru classloader since it is VERY SLOW [5 s]
                            List annotationPropsFileList = new ArrayList();
                            VirtualFile[] srcRoots = ModuleRootManager.getInstance(module).getSourceRoots();
                            for (int j = 0; j < srcRoots.length; j++) {
                                VirtualFile srcRoot = srcRoots[j];
                                VirtualFile annoProps = srcRoot.findChild("annotation.properties");
                                if (annoProps != null) {
                                    annotationPropsFileList.add(annoProps.getPath());
                                    compileContext.addMessage(
                                            CompilerMessageCategory.INFORMATION,
                                            "Using custom annotations " + annoProps.getPath(),
                                            null,
                                            -1,
                                            -1
                                    );
                                }
                            }
                            String[] annotationPropsFiles = (String[]) annotationPropsFileList.toArray(new String[]{});

                            // do the AnnotationC
                            m_messageHandler.currentFile = vf;
                            //ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
                            try {
                                //Thread.currentThread().setContextClassLoader(loader);
                                AnnotationC.compile(new String[0],
                                        new String[]{vf.getPath()},
                                        paths,
                                        destDir.getPath(),
                                                annotationPropsFiles.length == 0 ? null : annotationPropsFiles,
                                        m_messageHandler
                                );
                            } finally {
                                //Thread.currentThread().setContextClassLoader(currentCL);
                            }

                            // handle error without location as compilation errors
                            boolean vfHasError = false;
                            for (Iterator iterator = m_messageHandler.vfCompilerExceptions.iterator(
                                         ); iterator.hasNext();) {
                                BufferedMessageHandler.VfCompilerException vfCompilerException = (BufferedMessageHandler.VfCompilerException) iterator.next(
                                        );
                                if (vfCompilerException.vf.equals(vf)) {
                                    vfHasError = true;
                                    int line = -1;
                                    if (vfCompilerException.compilerException.getLocation() != null) {
                                        line = vfCompilerException.compilerException.getLocation().getLine();
                                    }
                                    compileContext.addMessage(
                                            CompilerMessageCategory.ERROR,
                                            "Annotation: " + vfCompilerException.compilerException.getMessage(),
                                            vf.getUrl(),
                                            line,
                                            -1
                                    );
                                }
                            }
                            if (!vfHasError) {
                                processedItems.add(processingItem);
                            }

                        } else { // end if ".java"
                            BpLog.info("skip " + vf.getPath());
                        }
                    }
                }
                );
            } catch (Exception e) {
                BpLog.error("Unexpected", e);
            }
        }
        // return only correctly processed items
        return (ProcessingItem[]) processedItems.toArray(new ProcessingItem[]{});
    }

    /**
     * Callback upon compilation completion to handle gutters
     */
    public void addMarkers() {
        // cleanup
        removeMarkers();

        // errors
        for (Iterator iterator = m_messageHandler.vfCompilerExceptions.iterator(); iterator.hasNext();) {
            BufferedMessageHandler.VfCompilerException vfCompilerException = (BufferedMessageHandler.VfCompilerException) iterator.next(
                    );
            if (vfCompilerException.compilerException.getLocation() != null) {
                Document document = FileDocumentManager.getInstance().getDocument(vfCompilerException.vf);
                MarkupModel model = document.getMarkupModel(m_project);
                RangeHighlighter rh = model.addLineHighlighter(
                        vfCompilerException.compilerException.getLocation().getLine() - 1,
                        HighlighterLayer.FIRST,
                        null
                );
                rh.putUserData(MARKER_KEY, Boolean.TRUE);
                addGutterIcon(
                        rh,
                        vfCompilerException.compilerException,
                        vfCompilerException.compilerException.getLocation()
                );

            }
        }

        // accepted
        for (Iterator iterator = m_messageHandler.vfSourceLocations.iterator(); iterator.hasNext();) {
            BufferedMessageHandler.VfSourceLocation vfSourceLocation = (BufferedMessageHandler.VfSourceLocation) iterator.next(
                    );
            Document document = FileDocumentManager.getInstance().getDocument(vfSourceLocation.vf);
            MarkupModel model = document.getMarkupModel(m_project);
            RangeHighlighter rh = model.addLineHighlighter(
                    vfSourceLocation.sourceLocation.getLine() - 1,
                    HighlighterLayer.FIRST,
                    null
            );
            rh.putUserData(MARKER_KEY, Boolean.TRUE);
            addGutterIcon(rh, vfSourceLocation.sourceLocation);
        }
    }

    public void removeMarkers() {
        Set vfs = m_messageHandler.getVirtualFiles();
        for (Iterator iterator = vfs.iterator(); iterator.hasNext();) {
            VirtualFile vf = (VirtualFile) iterator.next();
            Document document = FileDocumentManager.getInstance().getDocument(vf);
            MarkupModel model = document.getMarkupModel(m_project);
            RangeHighlighter[] rhs = model.getAllHighlighters();
            for (int i = 0; i < rhs.length; i++) {
                RangeHighlighter rh = rhs[i];
                if (rh.getUserData(MARKER_KEY) != null) {
                    model.removeHighlighter(rh);
                }
            }
        }
    }

    public String getDescription() {
        return "Backport175 AnnotationC compiler";
    }


    // IDEA v4
    public boolean validateConfiguration() {
        return true;
    }

    // IDEA v5
    public boolean validateConfiguration(CompileScope compileScope) {
        //        // debug some
        //        VirtualFile[] files = compileScope.getFiles(StdFileTypes.JAVA, true);
        //        for (int i = 0; i < files.length; i++) {
        //            VirtualFile file = files[i];
        //            System.out.println(file.getName());
        //            System.out.println(file.getPath());
        //            System.out.println(file.getUrl());
        //        }
        return true;
    }

    public ValidityState createValidityState(DataInputStream dataInputStream) throws IOException {
        //TODO can we return null
        return new TimestampValidityState(System.currentTimeMillis());
    }

    /**
     * Accepted annotation
     *
     * @param rangeHighlighter
     * @param location
     */
    private void addGutterIcon(RangeHighlighter rangeHighlighter,
                               final SourceLocation location) {
        rangeHighlighter.setGutterIconRenderer(new GutterIconRenderer() {
            public Icon getIcon() {
                return Icons.OK;
            }

            public String getTooltipText() {
                return location.getAnnnotationClassName();
            }

            public boolean isNavigateAction() {
                return false;
            }

            public AnAction getClickAction() {
                //TODO jump to annotation class
                return null;
            }
        });
    }

    /**
     * Error annotation
     *
     * @param rangeHighlighter
     * @param error
     * @param location
     */
    private void addGutterIcon(RangeHighlighter rangeHighlighter,
                               final CompilerException error,
                               final SourceLocation location) {
        rangeHighlighter.setGutterIconRenderer(new GutterIconRenderer() {
            public Icon getIcon() {
                return Icons.ERROR;
            }

            public String getTooltipText() {
                return location.getAnnnotationClassName() + " : " + error.getMessage();
            }

            public boolean isNavigateAction() {
                return false;
            }

            public AnAction getClickAction() {
                //TODO jump to annotation class
                return null;
            }
        });
    }

}

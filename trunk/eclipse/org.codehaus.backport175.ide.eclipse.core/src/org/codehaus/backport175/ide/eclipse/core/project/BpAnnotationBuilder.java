/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.ide.eclipse.core.project;

import org.codehaus.backport175.compiler.AnnotationC;
import org.codehaus.backport175.compiler.MessageHandler;
import org.codehaus.backport175.ide.eclipse.core.BpCorePlugin;
import org.codehaus.backport175.ide.eclipse.core.BpLog;
import org.codehaus.backport175.ide.eclipse.core.Strings;
import org.codehaus.backport175.ide.eclipse.ui.AnnotationEventHandler;
import org.codehaus.backport175.ide.eclipse.ui.AnnotationMarkerResolution;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import java.io.*;
import java.net.URL;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.ui.PlatformUI;

/**
 * @author avasseur
 * 
 * FIXME: when incremental, when an aspect is annC after other class compiled,
 * those will miss some advised def. FIXME: full weave: should annC all the
 * aspect first ==> need split of builders so that annC is run entirely BEFORE
 * awC FIXME: aop.xml change should trigger full weave ??
 * 
 */
public class BpAnnotationBuilder extends IncrementalProjectBuilder {

    public static final String BUILDER_ID = BpAnnotationBuilder.class.getName();

    protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
            throws CoreException {

        BpLog.logTrace("annotation for "
                + ((kind == FULL_BUILD) ? "full" : "incremental"));

        switch (kind) {
        case FULL_BUILD:
        case CLEAN_BUILD:
            monitor.beginTask("Backport175 annotation", 50);
            fullAnnotationC(monitor);
            break;

        case AUTO_BUILD:
        case INCREMENTAL_BUILD:
            if (getDelta(getProject()) == null)
                break;
            monitor.beginTask("Backport175 annotation", 1 + 2 * getDelta(
                    getProject()).getAffectedChildren().length);
            incrementalAnnotationC(monitor);
            break;

        default:
            break;
        }

        return null;
    }

    //	   private boolean shouldWeave(int kind) {
    //	      if (kind == FULL_BUILD)
    //	         return true;
    //	      IResourceDelta delta = getDelta(getProject());
    //	      if (delta == null)
    //	         return false;
    //	      IResourceDelta[] children = delta.getAffectedChildren();
    //	      for (int i = 0; i < children.length; i++) {
    //	         IResourceDelta child = children[i];
    //	         String fileName = child.getProjectRelativePath().lastSegment();
    //	         // at least one class
    //	         if (fileName.endsWith(".class"))
    //	            return true;
    //	      }
    //	      return false;
    //	   }

    private void fullAnnotationC(IProgressMonitor monitor) throws CoreException {
        getProject().accept(new BpAnnotationBuilderVisitor(monitor, true));
    }

    private void incrementalAnnotationC(IProgressMonitor monitor)
            throws CoreException {
        BpAnnotationBuilderVisitor bpAnnCVisitor = new BpAnnotationBuilderVisitor(monitor, false);
        getDelta(getProject()).accept(bpAnnCVisitor);
    }

    class BpAnnotationBuilderVisitor implements IResourceVisitor,
            IResourceDeltaVisitor {
        
        private IProgressMonitor m_monitor;

        private final boolean m_isFull;
        
        private ClassLoader m_projectClassLoader;
        
        private String[] m_pathFiles;
        
        private IJavaProject m_jproject;
        
        private String[] m_annotationPropsFiles;
        
        private MessageHandler m_eventHandler;
        
        public BpAnnotationBuilderVisitor(IProgressMonitor monitor, boolean isFull) {
            m_monitor = monitor;
            m_isFull = isFull;
            
            // get the project classloader
            m_jproject = JavaCore.create(getProject());
            m_projectClassLoader = BpCorePlugin.getDefault().getProjectClassLoader(m_jproject);
            m_eventHandler = new AnnotationEventHandler(m_jproject);
            
            // get the annotation.properties files from classloader lookup (v1.0.1)
//            try  {
//	            Enumeration annotationProps = m_projectClassLoader.getResources("annotation.properties");
//	            List annotationPropsFilesList = new ArrayList();
//	            while (annotationProps.hasMoreElements()) {
//	                String annFile = ((URL)annotationProps.nextElement()).getFile().toString();
//	                annotationPropsFilesList.add(annFile);
//	                BpLog.logInfo("using custom annotations " + annFile);
//	            }
//	            m_annotationPropsFiles = (String[])annotationPropsFilesList.toArray(new String[]{});
//            } catch (Throwable t) {
//                BpLog.logError("cannot access annotation.properties file(s)", t);
//                m_annotationPropsFiles = new String[0];
//            }

            // get them from property panel (v1.0.2)
            m_annotationPropsFiles = new String[0];
            IScopeContext projectScope = new ProjectScope(BpAnnotationBuilder.this.getProject());
            IEclipsePreferences node = projectScope.getNode(BpCorePlugin.pluginID());
            if(node != null)
            {
                String value = node.get(BpCorePlugin.annotationFileID, "");
                if(value != "")
                {
                    m_annotationPropsFiles = new String[1];
                    m_annotationPropsFiles[0] = value;
                    BpLog.logInfo("using custom annotations " + value + " for project " + BpAnnotationBuilder.this.getProject().getName());
                }
             }            
            
            
            // build the classpath we will use to run AnnotationC so that it find 
            // custom annotations etc
            List pathURLs = BpCorePlugin.getDefault().getProjectClassPathURLs(m_jproject);
            m_pathFiles = new String[pathURLs.size()];
            int i = 0;
            for (Iterator urls = pathURLs.iterator(); urls.hasNext(); i++) {
                m_pathFiles[i] = ((URL) urls.next()).getFile().toString();
            }
        }

        public boolean visit(IResource resource) throws CoreException {
            if (resource.getType() == IResource.FILE) {
                if ("class".equals(resource.getFileExtension())) {
                    m_monitor.subTask(resource.getName());

                    annotate(resource, m_monitor, m_isFull);

                    m_monitor.worked(1);
                }
            }
            return true;
        }

        public boolean visit(IResourceDelta delta) throws CoreException {
            IResource resource = delta.getResource();

            if (resource.getType() == IResource.FILE) {
                if ("class".equals(resource.getFileExtension())) {
                    m_monitor.subTask(resource.getName());

                    switch (delta.getKind()) {
                    case IResourceDelta.ADDED:
                    case IResourceDelta.CHANGED:
                    case IResourceDelta.CONTENT:
                        annotate(resource, m_monitor, m_isFull);
                        break;

                    default:
                        break;
                    }

                    m_monitor.worked(1);
                }
            }
            return true;
        }
        
        private void annotate(IResource resource, IProgressMonitor monitor, boolean isFull) {
            // change the Thread classloader
            ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
            try {
                BpLog.logInfo("annotate " + resource.getName());
                Thread.currentThread().setContextClassLoader(m_projectClassLoader);
                
                File resourceFile = resource.getRawLocation().toFile();
                String className = BpCorePlugin.getDefault().extractClassNameFromClassFile(resourceFile);
                BpLog.logTrace("got name from bytes " + className);
                
                // skip inner class since they will be annnotated when their outer is annotated
                // TODO  handle aspect as inner class change triggering
                if (className.indexOf('$')>0) {
                    return;
                }
//                
//                // check if we have at least one aspect in the delta when we are not doing a full build
//                if (!m_isFull && !m_hasEncounteredAspects) {
//                    boolean isAspect = isAspect(className);
//                    if (isAspect) {
//                        BpLog.logInfo("Detected a change in aspect " + className);
//                        m_hasEncounteredAspects = true;
//                    }
//                }
////                    IProject project = resource.getProject();
//                    //TODO how to trigger a nested build / clear state etc ?
////                    project.getProject().build(IncrementalProjectBuilder.FULL_BUILD,monitor);                
////                    getProject().getProject().build(AwAnnotationBuilder.FULL_BUILD,
////                            AwAnnotationBuilder.BUILDER_ID, null, monitor);
////                    getProject().getProject().build(AwProjectBuilder.FULL_BUILD,
////                            AwProjectBuilder.BUILDER_ID, null, monitor);
////                    BpLog.logInfo("DONE Detected a change in aspect " + className);
////                    BpLog.logInfo("full .? " + isFull );
////                    //return;

                // extract the file path
                int segments = Strings.splitString(className, "/").length;
                // = 2 for pack.Hello
                IPath pathToFile = resource.getRawLocation().removeLastSegments(segments);
                String destDir = pathToFile.toFile().toString();
                BpLog.logTrace("will annotate to " + destDir);

                // call AnnotationC for only one file, dest dir = src dir
                boolean verbose = true;

                // source file for this resource
                IResource sourceFile = BpCorePlugin.getDefault().findSourceForResource(getProject(), resource, className);
                if (checkCancel(monitor))
                    return;

                if (sourceFile == null) {
                    BpLog.logInfo("cannot find source for compiled resource "
                            + resource.getRawLocation().toString());
                } else {
    	                String targetFile = sourceFile.getRawLocation().toFile().toString();
    	                
    	                // delete all annotation markers before running
    	                AnnotationEventHandler.deleteMarkers(sourceFile);
    	                
    	                AnnotationC.compile(new String[0],
    	                        new String[] { targetFile }, m_pathFiles, destDir,
    	                        m_annotationPropsFiles.length==0?null:m_annotationPropsFiles,
    	                        m_eventHandler,
								true//ignore unknown since markers
    	                );
    	                BpLog.logTrace("annotated " + className + " from " + targetFile);
                }
                if (checkCancel(monitor))
                    return;

                monitor.worked(1);

                // write back
                // -> has been done by AnnotationC
            } catch (Throwable e) {
                BpLog.logError(e);
    	    } finally {
    	        Thread.currentThread().setContextClassLoader(currentCL);
    	    }
        }
    }

    private boolean checkCancel(IProgressMonitor monitor) {
        if (monitor.isCanceled()) {
            // discard build state if necessary
            throw new OperationCanceledException();
        }
        if (isInterrupted()) {
            // discard build state if necessary
            return true;
        }
        return false;
    }

    private String readFile(IFile file) {
        if (!file.exists())
            return "";
        InputStream stream = null;
        try {
            stream = file.getContents();
            Reader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuffer result = new StringBuffer(2048);
            char[] buf = new char[2048];
            while (true) {
                int count = reader.read(buf);
                if (count < 0)
                    break;
                result.append(buf, 0, count);
            }
            return result.toString();
        } catch (Exception e) {
            BpLog.logError(e);
            return "";
        } finally {
            try {
                if (stream != null)
                    stream.close();
            } catch (IOException e) {
                BpLog.logError(e);
                return "";
            }
        }
    }

}
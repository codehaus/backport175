/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.ide.eclipse.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.ui.texteditor.MarkerUtilities;

import org.codehaus.backport175.compiler.AnnotationC;
import org.codehaus.backport175.compiler.CompilerException;
import org.codehaus.backport175.compiler.MessageHandler;
import org.codehaus.backport175.compiler.SourceLocation;
import org.codehaus.backport175.ide.eclipse.core.BpCorePlugin;
import org.codehaus.backport175.ide.eclipse.core.BpLog;
import org.codehaus.backport175.org.objectweb.asm.Type;

/**
 * @author avasseur
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AnnotationEventHandler implements MessageHandler {
    
    private final static String ANNOTATION_MARKER = "org.codehaus.backport175.ide.eclipse.core.annotation";
    private final static String ANNOTATION_PROBLEM_MARKER = "org.codehaus.backport175.ide.eclipse.core.annotationProblem";
    public final static String LOCATION_ATTRIBUTE = "backport175.location";
    public final static String JAVAPROJECT_ATTRIBUTE = "backport175.jproject";

    private IJavaProject m_jproject;
    
    public AnnotationEventHandler(IJavaProject jproject) {
    	m_jproject = jproject;
    }
    
    
	/* (non-Javadoc)
	 * @see org.codehaus.backport175.compiler.AnnotationC.IEventHandler#info(java.lang.String, org.codehaus.backport175.compiler.CompilerException.Location)
	 */
	public void info(String message) {
		BpLog.logTrace(message); 
	}
	
	public void accept(final SourceLocation location) {
		String className = location.getClassName();
		BpLog.logInfo("notified for " + className + ":" + location.getLine());
        final IType atClass;
        final IResource resource;
        try {
            atClass = m_jproject.findType(className);
	        if (atClass == null) {
	            return;
	        }
	        resource= atClass.getUnderlyingResource();
	        if (resource == null) {
	            return;
	        }
        } catch (JavaModelException e) {
            BpLog.logError(e);
            return;
        }
		        
        // do markers creation async
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor)
              throws CoreException {
		        try {
			        createMarker(resource, location);
		        } catch (Exception e) {
		            BpLog.logError(e);
		        }
            }
        };
        try {
            resource.getWorkspace().run(runnable, null);
        } catch (CoreException e1) {
            BpLog.logError(e1);
            return;
        }
	}
	

	/* (non-Javadoc)
	 * @see org.codehaus.backport175.compiler.AnnotationC.IEventHandler#error(org.codehaus.backport175.compiler.CompilerException)
	 */
	public void error(CompilerException compilerException) {
		SourceLocation location = compilerException.getLocation();
		if (location == null) {
			BpLog.logError(compilerException);
		} else {
	        final IType atClass;
	        final IResource resource;
	        try {
	            atClass = m_jproject.findType(location.getClassName());
		        if (atClass == null) {
		            return;
		        }
		        resource= atClass.getUnderlyingResource();
		        if (resource == null) {
		            return;
		        }
		        
				createErrorMarker(resource, compilerException.getMessage(), location);
	        } catch (Exception e) {
	            BpLog.logError(e);
	            return;
	        }
			
			
			BpLog.logTrace(compilerException.toString()
					       + " " + compilerException.getLocation().toString());
		}
		compilerException.printStackTrace();
	}
    

	
	
	
	public static void deleteMarkers(IResource resource) {
        try {
          resource.deleteMarkers(
            ANNOTATION_MARKER,
            false,
            IResource.DEPTH_INFINITE);
          resource.deleteMarkers(
                ANNOTATION_PROBLEM_MARKER,
                false,
                IResource.DEPTH_INFINITE);
        } catch (CoreException e) {
            BpLog.logError(e);
        }
      }
    
    private void createMarker(IResource resource, SourceLocation location)
            throws JavaModelException, CoreException {
		Map map= new HashMap();
		MarkerUtilities.setLineNumber(map, location.getLine());
		MarkerUtilities.setMessage(map, location.getAnnnotationClassName());
		map.put(LOCATION_ATTRIBUTE, location);
		map.put(JAVAPROJECT_ATTRIBUTE, m_jproject);
        MarkerUtilities.createMarker(resource, map, ANNOTATION_MARKER);
    }
    
    private void createErrorMarker(IResource resource, String hint, SourceLocation location)
    throws JavaModelException, CoreException {
		Map map= new HashMap();
		MarkerUtilities.setLineNumber(map, location.getLine());
		MarkerUtilities.setMessage(map, location.getAnnnotationClassName() + " : " + hint);
		map.put(LOCATION_ATTRIBUTE, location);
		map.put(JAVAPROJECT_ATTRIBUTE, m_jproject);
		map.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_ERROR));
		MarkerUtilities.createMarker(resource, map, ANNOTATION_PROBLEM_MARKER);
    }

}

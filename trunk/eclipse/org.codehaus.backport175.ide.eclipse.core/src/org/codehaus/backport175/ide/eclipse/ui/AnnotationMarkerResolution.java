/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.ide.eclipse.ui;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.backport175.compiler.CompilerException;
import org.codehaus.backport175.compiler.SourceLocation;
import org.codehaus.backport175.ide.eclipse.core.BpLog;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;

/**
 * @author avasseur
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AnnotationMarkerResolution implements IMarkerResolutionGenerator2 {

    /* (non-Javadoc)
     * @see org.eclipse.ui.IMarkerResolutionGenerator2#hasResolutions(org.eclipse.core.resources.IMarker)
     */
    public boolean hasResolutions(IMarker marker) {
        try {
            return (marker.getAttribute(AnnotationEventHandler.LOCATION_ATTRIBUTE) != null);
        } catch (CoreException e) {
            BpLog.logError(e);
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IMarkerResolutionGenerator#getResolutions(org.eclipse.core.resources.IMarker)
     */
    public IMarkerResolution[] getResolutions(IMarker marker) {
        try {
            SourceLocation location = (SourceLocation) marker.getAttribute(AnnotationEventHandler.LOCATION_ATTRIBUTE);
            IJavaProject jproject = (IJavaProject) marker.getAttribute(AnnotationEventHandler.JAVAPROJECT_ATTRIBUTE);
            
            List resolutions = new ArrayList();
            resolutions.add(new AnnotationMarkerResolutionAction(location, jproject));
            return (IMarkerResolution[]) resolutions.toArray(new IMarkerResolution[]{});
        } catch (CoreException e) {
            BpLog.logError(e);
            return null;
        }
    }
    
    private static class AnnotationMarkerResolutionAction implements IMarkerResolution {
        
        private final String m_label;
        private final String m_annotationClassName;
        
        private final IJavaProject m_jproject;
        
        public AnnotationMarkerResolutionAction(SourceLocation location, IJavaProject jproject) {
            m_jproject = jproject;
            m_annotationClassName = location.getAnnnotationClassName();
            
            //label
            StringBuffer sb = new StringBuffer();
            sb.append("Go to: " + location.getAnnnotationClassName());
            m_label = sb.toString();
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IMarkerResolution#getLabel()
         */
        public String getLabel() {
            return m_label;
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IMarkerResolution#run(org.eclipse.core.resources.IMarker)
         */
        public void run(IMarker marker) {
            try {
                IType annotation = m_jproject.findType(m_annotationClassName.replace('/', '.'));
                if (annotation != null) {
                    //IResource resource = annotation.getUnderlyingResource();
                    IEditorPart editor = JavaUI.openInEditor(annotation);
                    JavaUI.revealInEditor(editor, (IJavaElement)annotation);
                }
            } catch (Exception e) {
                BpLog.logError(e);
                return;
            }
        }
    }
    
    

}

/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.ide.eclipse.core.project;

import org.codehaus.backport175.ide.eclipse.core.BpCorePlugin;
import org.codehaus.backport175.ide.eclipse.core.BpLog;
import org.codehaus.backport175.ide.eclipse.ui.AnnotationEventHandler;
import org.codehaus.backport175.ide.eclipse.ui.AnnotationMarkerResolution;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;


/**
 * @author avasseur
 * 
 */
public class BpProjectNature implements IProjectNature {

    public final static String NATURE_ID = BpProjectNature.class.getName();

    private IProject project;

    public IProject getProject() {
        return project.getProject();
    }

    public void setProject(IProject project) {
        BpLog.logInfo("nature.setProject " + project.getName());
        this.project = project;
    }

    public void configure() throws CoreException {
//        AwCorePlugin.getDefault().registerWeaverListener(
//                new WeaverListener());
        
        BpLog.logInfo("nature configure");        
        BpCorePlugin.getDefault().addBuilderToProject(project,
                BpAnnotationBuilder.BUILDER_ID);
        new Job("Backport175 Nature") {
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    project.getProject().build(IncrementalProjectBuilder.FULL_BUILD,
                            monitor);
//                    project.getProject().build(AwAnnotationBuilder.FULL_BUILD,
//                            AwAnnotationBuilder.BUILDER_ID, null, monitor);
                } catch (CoreException e) {
                    BpLog.logError(e);
                }
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    public void deconfigure() throws CoreException {
        BpCorePlugin.getDefault().removeBuilderFromProject(project,
                BpAnnotationBuilder.BUILDER_ID);
        AnnotationEventHandler.deleteMarkers(project);
    }

}
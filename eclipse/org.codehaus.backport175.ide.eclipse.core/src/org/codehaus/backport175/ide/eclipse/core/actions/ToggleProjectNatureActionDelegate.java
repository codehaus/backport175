/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.ide.eclipse.core.actions;

import java.util.*;

import org.codehaus.backport175.ide.eclipse.core.BpLog;
import org.codehaus.backport175.ide.eclipse.core.project.BpProjectNature;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;


/**
 * @author avasseur
 * 
 */
public class ToggleProjectNatureActionDelegate implements
        IWorkbenchWindowActionDelegate {

    private ISelection m_selection;

    public void init(IWorkbenchWindow window) {
        // ignored
    }

    public void selectionChanged(IAction action, ISelection selection) {
        this.m_selection = selection;
    }

    public void run(IAction action) {
        BpLog.logTrace("toggle Backport175 nature");
        if (!(m_selection instanceof IStructuredSelection))
            return;
        Iterator iter = ((IStructuredSelection) m_selection).iterator();
        while (iter.hasNext()) {
            Object element = iter.next();
            IProject project = null;
            if (element instanceof IJavaProject) {
                project = ((IJavaProject) element).getProject();
            } else if (element instanceof IProject) {
                project = (IProject) element;
            } else {
                BpLog.logTrace("cannot add nature to " + element.getClass());
                continue;
            }

            // cannot modify closed projects
            if (!project.isOpen()) {
                BpLog.logTrace("cannot add nature to closed project");
                continue;
            }
            try {

                IProjectDescription desc = project.getDescription();

                if (!project.hasNature(BpProjectNature.NATURE_ID)) {
                    BpLog.logInfo("adding nature");
                    String natureIds[] = desc.getNatureIds();
                    String newNatureIds[] = new String[natureIds.length + 1];

                    System.arraycopy(natureIds, 0, newNatureIds, 1,
                            natureIds.length);
                    newNatureIds[0] = BpProjectNature.NATURE_ID;
                    desc.setNatureIds(newNatureIds);

                    project.setDescription(desc, null);//new
                                                       // ProgressMonitorPart(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                                       // new RowLayout()));
                    //							new NullProgressMonitor());
                } else {
                    BpLog.logInfo("removing nature");
                    //TODO remove markers
                    String natureIds[] = desc.getNatureIds();
                    String newNatureIds[] = new String[natureIds.length - 1];
                    int j = 0;
                    for (int i = 0; i < natureIds.length; i++) {
                        if (natureIds[i].equals(BpProjectNature.NATURE_ID))
                            continue;
                        else
                            newNatureIds[j++] = natureIds[i];
                    }
                    desc.setNatureIds(newNatureIds);
                    project.setDescription(desc, null);
                }
            } catch (CoreException e) {
                BpLog.logError(e);
            }

            BpLog.logTrace("toggle nature done ");
        }
    }

    public void dispose() {
    }

}
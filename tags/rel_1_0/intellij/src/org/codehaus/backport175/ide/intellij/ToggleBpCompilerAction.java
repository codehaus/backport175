/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.ide.intellij;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import org.codehaus.backport175.ide.intellij.BpProjectComponent;

/**
 * Toggle plugin, menu appears in "Build > .."
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class ToggleBpCompilerAction extends AnAction {

    /**
     * Toggle plugin for module, update menu text
     *
     * @param event
     */
    public void actionPerformed(AnActionEvent event) {
        Project currentProject = (Project) event.getDataContext().getData(DataConstants.PROJECT);
        if (currentProject != null && currentProject.hasComponent(BpProjectComponent.class)) {
            BpProjectComponent bpComponent = (BpProjectComponent)currentProject.getComponent(BpProjectComponent.class);
            bpComponent.toggle();
            if (bpComponent.isActivated()) {
                event.getPresentation().setText("Disable Backport175 Annotations");
            } else {
                event.getPresentation().setText("Enable Backport175 Annotations");
            }
        }
    }

    /**
     * We need to sync the menu with the actual state (since persisted in IWS file)
     *
     * @param event
     */
    public void update(AnActionEvent event) {
        Project currentProject = (Project) event.getDataContext().getData(DataConstants.PROJECT);
        if (currentProject != null && currentProject.hasComponent(BpProjectComponent.class)) {
            BpProjectComponent bpComponent = (BpProjectComponent)currentProject.getComponent(BpProjectComponent.class);
            if (bpComponent.isActivated()) {
                event.getPresentation().setText("Disable Backport175 Annotations");
            } else {
                event.getPresentation().setText("Enable Backport175 Annotations");
            }
        }
    }
}

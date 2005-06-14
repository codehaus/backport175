/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.ide.intellij;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.compiler.CompilationStatusListener;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.JDOMExternalizer;
import org.jdom.Element;
import org.codehaus.backport175.ide.intellij.BpCompiler;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Main IDEA component attached to a project.
 * <p/>
 * Handles simple on/off configuration
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class BpProjectComponent implements ProjectComponent, JDOMExternalizable {

    private Project m_project;
    
    private BpCompiler m_compiler;

    private CompilationStatusListener m_compilationStatusListener;

    private boolean m_isActivated = false;

    /**
     * IOC constructor
     * @param project
     */
    public BpProjectComponent(Project project) {
        m_project = project;

        m_compiler = new BpCompiler(m_project);

        m_compilationStatusListener = new CompilationStatusListener() {
            public void compilationFinished(boolean b, int i, int i1) {
                m_compiler.addMarkers();
            }
        };
    }

    public void projectOpened() {
    }

    public void projectClosed() {
        CompilerManager.getInstance(m_project).removeCompiler(m_compiler);
    }

    public String getComponentName() {
        return BpProjectComponent.class.getName().replace('/', '.');
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    /**
     * Read on/off state from IWS
     *
     * @param element
     * @throws InvalidDataException
     */
    public void readExternal(Element element) throws InvalidDataException {
        boolean config = JDOMExternalizer.readBoolean(element, getComponentName()+":isActivated");
	changeTo(config); 
    }

    /**
     * Persists on/off state in IWS
     *
     * @param element
     * @throws WriteExternalException
     */
    public void writeExternal(Element element) throws WriteExternalException {
        JDOMExternalizer.write(element, getComponentName()+":isActivated", m_isActivated);
    }

    public void toggle() {
        changeTo(!m_isActivated);
    }

    private void changeTo(boolean activate) {
	BpLog.info("to " + activate + " for " + m_project + " / " + m_isActivated);
        if (activate != m_isActivated) {
            CompilerManager compilerManager = CompilerManager.getInstance(m_project);
            if (activate) {
                compilerManager.addCompiler(m_compiler);
                compilerManager.addCompilationStatusListener(m_compilationStatusListener);
            } else {
                compilerManager.removeCompilationStatusListener(m_compilationStatusListener);
                compilerManager.removeCompiler(m_compiler);
                m_compiler.removeMarkers();
            }
            m_isActivated = activate;
        }
    }

    public boolean isActivated() {
        return m_isActivated;
    }

}

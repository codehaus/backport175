/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.ide.intellij;

import org.codehaus.backport175.compiler.MessageHandler;
import org.codehaus.backport175.compiler.CompilerException;
import org.codehaus.backport175.compiler.SourceLocation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Message handler
 *
 * Keeps track of errors and accepted annotation in a buffer on a per IDEA VirtualFile
 * basis, so that markers are added outside of the compilation thread to have proper
 * Swing access (IDEA arch.).
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class BufferedMessageHandler implements MessageHandler {

    Project m_project;

    /**
     * The current file for wich we receive messages
     */
    VirtualFile currentFile;

    /**
     * All errors
     */
    public List vfCompilerExceptions = new ArrayList();

    /**
     * All accepted annotations
     */
    public List vfSourceLocations = new ArrayList();

    public BufferedMessageHandler(Project project) {
        m_project = project;
    }

    public void info(String string) {
        BpLog.info(string);
    }

    public void error(CompilerException compilerException) {
        VfCompilerException vf = new VfCompilerException();
        vf.compilerException = compilerException;
        vf.vf = currentFile;
        vfCompilerExceptions.add(vf);
    }

    public void accept(SourceLocation sourceLocation) {
        VfSourceLocation vf = new VfSourceLocation();
        vf.sourceLocation = sourceLocation;
        vf.vf = currentFile;
        vfSourceLocations.add(vf);
    }

    public void flush() {
        vfCompilerExceptions.clear();
        vfSourceLocations.clear();
    }

    /**
     * Return all distinct VirtualFile affected, since the last flush
     *
     * @return
     */
    public Set getVirtualFiles() {
        Set vfs = new HashSet();
        for (Iterator iterator = vfCompilerExceptions.iterator(); iterator.hasNext();) {
            VfCompilerException vfCompilerException = (VfCompilerException) iterator.next();
            vfs.add(vfCompilerException.vf);
        }
        for (Iterator iterator = vfSourceLocations.iterator(); iterator.hasNext();) {
            VfSourceLocation vfSourceLocation = (VfSourceLocation) iterator.next();
            vfs.add(vfSourceLocation.vf);
        }
        return vfs;
    }

    static class VfCompilerException {
        CompilerException compilerException;
        VirtualFile vf;
    }

    static class VfSourceLocation {
        SourceLocation sourceLocation;
        VirtualFile vf;
    }
}

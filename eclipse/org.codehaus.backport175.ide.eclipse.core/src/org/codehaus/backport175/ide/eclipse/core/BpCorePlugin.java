/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.ide.eclipse.core;


import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.ui.plugin.*;
import org.osgi.framework.BundleContext;

import org.codehaus.backport175.compiler.AnnotationC;
import org.codehaus.backport175.ide.eclipse.ui.AnnotationEventHandler;
import org.codehaus.backport175.org.objectweb.asm.ClassAdapter;
import org.codehaus.backport175.org.objectweb.asm.ClassReader;
import org.codehaus.backport175.org.objectweb.asm.ClassVisitor;
import org.codehaus.backport175.org.objectweb.asm.ClassWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Main s_plugin class
 * 
 * @author avasseur
 */
public class BpCorePlugin extends AbstractUIPlugin {

    private static BpCorePlugin s_plugin;

    private ResourceBundle m_resourceBundle;
    
    public BpCorePlugin() {
        super();
        s_plugin = this;
        try {
            m_resourceBundle = ResourceBundle
                    .getBundle("org.codehaus.backport175.ide.eclipse.core.CorePluginResources");
        } catch (MissingResourceException x) {
            m_resourceBundle = null;
        }
    }

    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    public void stop(BundleContext context) throws Exception {
        super.stop(context);
    }

    public static BpCorePlugin getDefault() {
        return s_plugin;
    }
        
    public static String getResourceString(String key) {
        ResourceBundle bundle = BpCorePlugin.getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public ResourceBundle getResourceBundle() {
        return m_resourceBundle;
    }

    /**
     * Build the given project classloader, child of the plugin classloader
     * Note: AW classes will thus be owned by the plugin classloader
     * 
     * @param project
     * @return
     */
    public URLClassLoader getProjectClassLoader(IJavaProject project) {
        List paths = getProjectClassPathURLs(project);
        URL pathUrls[] = (URL[]) paths.toArray(new URL[]{});
        return new URLClassLoader(pathUrls, getClass().getClassLoader());
    }

    /**
     * Build the list of URL for the given project
     * Resolve container (ie JRE jars) and dependancies and project output folder
     * 
     * @param project
     * @return
     */
    public List getProjectClassPathURLs(IJavaProject project) {
        List paths = new ArrayList();
        try {
            // configured classpath
            IClasspathEntry classpath[] = project.getResolvedClasspath(false);
            for (int i = 0; i < classpath.length; i++) {
                IClasspathEntry path = classpath[i];
                URL urlEntry = null;

                if (path.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
            		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
            		Object target = JavaModel.getTarget(workspaceRoot, path.getPath(), false);
            		if (target != null) {
	            		// inside the workspace
	            		if (target instanceof IResource) {
	            		    urlEntry = ((IResource)target).getLocation().toFile().toURL();
	            		} else if (target instanceof File) {
	            		    urlEntry = ((File)target).toURL();
	            		}
            		}
                } else if (path.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                    IPath outPath = path.getOutputLocation();
                    if (outPath != null) {
                        //TODO : don't know if I ll have absolute path here
                        urlEntry = outPath.toFile().toURL();
                    }
                }
                if (urlEntry != null) {
                    paths.add(urlEntry);
                } else {
                    BpLog.logTrace("project loader - ignored " + path.toString());
                }
            }
            // project build output
            IPath location = getProjectLocation(project.getProject());
            IPath outputPath = location.append(project.getOutputLocation().removeFirstSegments(1));
            paths.add(outputPath.toFile().toURL());
        } catch (Exception e) {
            BpLog.logError("Could not build project path", e);
        }
        return paths;
    }
    
    public IPath getProjectLocation(IProject project) {
        if (project.getRawLocation() == null) {
            return project.getLocation();
        } else {
            return project.getRawLocation();
        }
    }

    private byte[] readClassFile(File file) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream in = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        while (in.available() > 0) {
            int length = in.read(buffer);
            if (length == -1) {
                break;
            }
            bos.write(buffer, 0, length);
        }
        in.close();
        bos.close();
        return bos.toByteArray();
    }
    
    public String extractClassNameFromClassFile(File file) throws Exception {
    	byte[] bytecode = readClassFile(file);
    	ClassReader cr = new ClassReader(bytecode);
    	ClassWriter cw = new ClassWriter(true);
    	GetClassNameClassAdapter visitor = new GetClassNameClassAdapter(cw);
    	cr.accept(visitor, true);
    	return visitor.className;
    }
    
    private static class GetClassNameClassAdapter extends ClassAdapter {
    	String className;
    	GetClassNameClassAdapter(ClassVisitor cv) {
    		super(cv);
    	}
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
			className = name;
		}
    }

    public void addBuilderToProject(IProject project, String builderId) {
        BpLog.logInfo("add builder " + builderId + " to " + project.getName());
        try {
            IProjectDescription desc = project.getDescription();
            ICommand[] commands = desc.getBuildSpec();

            //add builders to project
            ICommand builderCommand = desc.newCommand();
            builderCommand.setBuilderName(builderId);

            ICommand[] newCommands = new ICommand[commands.length + 1];
            System.arraycopy(commands, 0, newCommands, 0, commands.length);
            newCommands[newCommands.length - 1] = builderCommand;
            desc.setBuildSpec(newCommands);
            project.setDescription(desc, null);
        } catch (CoreException e) {
            BpLog.logError("Could not add builder " + builderId, e);
        }
    }

    public void removeBuilderFromProject(IProject project, String builderId) {
        BpLog.logInfo("remove builder " + builderId + " to " + project.getName());
        try {
            IProjectDescription description = project.getDescription();

            // look for builder
	        int index = -1;
	        ICommand[] cmds = description.getBuildSpec();
	        for (int j = 0; j < cmds.length; j++) {
	            if (cmds[j].getBuilderName().equals(builderId)) {
	                index = j;
	                break;
	            }
	        }
	        // not found
	        if (index == -1)
	            return;

	        // remove builder from project
	        List newCmds = new ArrayList();
	        newCmds.addAll(Arrays.asList(cmds));
	        newCmds.remove(index);
	        description.setBuildSpec((ICommand[]) newCmds.toArray(new ICommand[0]));
	        project.setDescription(description, null);
        } catch (CoreException e) {
            BpLog.logError("could not remove builder " + builderId, e);
        }
    }
    
    public IResource findSourceForResource(IProject project, IResource resource, final String className) throws CoreException {
        // source file for this resource
        //TODO can we use resource (.class file)
        return MyIResourceProxyVisitor.resourceOf(project, className);
    }

    private static class MyIResourceProxyVisitor implements IResourceProxyVisitor {
        /**
         * the class name we search the source file of
         */
        private String m_className;
        
        /**
         * The outer class name for inner class or itself.
         */
        private String m_outerClassName;
        
        /**
         * derived from m_className, contains name without package info and suffixed with .java,
         * with support for Anonymous class ??
         */
        private String m_sourceSuffix;

        /**
         * the found source file
         */
        private IResource m_source;
        
        /**
         * helper method to find the source file of className within the project
         * 
         * @param project
         * @param className
         * @return - can be null if not found
         * @throws CoreException
         */
        public static IResource resourceOf(IProject project, String className) throws CoreException {
            MyIResourceProxyVisitor v = new MyIResourceProxyVisitor(className);
            project.accept(v, IResource.FILE);
            return v.getResource();
        }
        
        private MyIResourceProxyVisitor(String className) {
            m_className = className;
            if (className.indexOf('$') > 0) {
                // inner class
                m_outerClassName = className.substring(0, className.lastIndexOf('$'));
            } else {
                m_outerClassName = m_className;
            }
            String[] classNameParts = Strings.splitString(m_outerClassName, ".");
	        m_sourceSuffix = classNameParts[classNameParts.length-1] + ".java";
        }
        
        public IResource getResource() {
            return m_source;
        }

        public boolean visit(IResourceProxy resourceProxy) {
            // already found ?
            if (m_source != null) {
                return false;
            }
            // must end with the suffix
            if (!resourceProxy.getName().endsWith(m_sourceSuffix)) {
                return true;
            }
            // must match the outerClassName
            if (resourceProxy.requestFullPath().toString().endsWith(
                    m_outerClassName.replace('.', '/') + ".java")) {
                m_source = resourceProxy.requestResource();
                return false;
            }
            return true;
        }
    }
}
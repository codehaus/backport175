/*
 * Class: AnnotationPropertyPage
 * 
 * Created on May 13, 2005
 * 
 */
package org.codehaus.backport175.ide.eclipse.ui;

import org.codehaus.backport175.ide.eclipse.core.BpCorePlugin;
import org.codehaus.backport175.ide.eclipse.core.BpLog;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;

/**
 * <tt>AnnotationPropertyPage</tt> ...
 */
public class AnnotationPropertyPage extends PropertyPage
{

    private FileFieldEditor file;
    /**
     * Constructs a new <tt>AnnotationPropertyPage</tt>.
     */
    public AnnotationPropertyPage()
    {
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent)
    {
        // set up the props page
        Control c = this.addControl(parent);
        
        // set the current file value, if available
        IScopeContext projectScope = new ProjectScope(this.getProject());
        IEclipsePreferences node = projectScope.getNode(BpCorePlugin.pluginID());
        String value = "";
        if(node != null)
        {
            value = node.get(BpCorePlugin.annotationFileID, "");
        }
        file.setStringValue(value);
        return c;
    }
    
    public boolean performOk()
    {
        try
        {
            IScopeContext projectScope = new ProjectScope(this.getProject());
            IEclipsePreferences node = projectScope.getNode(BpCorePlugin.pluginID());
            node.put(BpCorePlugin.annotationFileID, file.getStringValue());
            node.flush();
        }
        catch (BackingStoreException e)
        {
            IStatus status = new Status(IStatus.ERROR, BpCorePlugin.pluginID(), 44, "Cannot set annotation property file", e);
            ErrorDialog.openError(getShell(), "Error", "Cannot set annotation property file", status);
            BpLog.log(status);
        }
        return true;
    }

    private Control addControl(Composite parent)
    {
        this.noDefaultAndApplyButton();
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        composite.setLayout(layout);
        GridData data = new GridData(GridData.FILL, GridData.FILL, true, true);
        composite.setLayoutData(data);
        
        Font font = parent.getFont();
        file = new FileFieldEditor("org.codehaus.backport175.ide.eclipse.ui.property.file", "Annotation Property file", true, composite);
        file.setFileExtensions(new String[] { "*.properties" } );
        file.setPreferencePage(this);//?? was setPage 
        file.load();
        
        return composite;
    }
    
    private IProject getProject()
    {
        return (IProject) getElement();
    }
}

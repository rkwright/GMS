/*******************************************************************************
 * Copyright (c) 2008 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 *******************************************************************************/

package com.geofx.gms.editor;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import com.geofx.gms.controller.Controller;
import com.geofx.gms.model.ProjectInfo;
import com.geofx.gms.plugin.GMSPlugin;
import com.geofx.gms.viewers.ViewInfo;

/**
 * An example showing how to create a multi-page editor. This example has 3
 * pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to change the font used in page 2
 * <li>page 2 shows the words in page 0 in sorted order
 * </ul>
 */
public class GMSEditor extends FormEditor 
{

	/** The text editor used in last page. */
	private GMSXMLEditor 	xmlEditor;
	private OverviewPage	overviewPage;
	private boolean 		soiled = false;
	private IProject		project;

	// the one and only ProjectInfo object for this instance of the editor
	private ProjectInfo 	projectInfo = new ProjectInfo();
	private ManifestInput	datasetsInput = new ManifestInput(projectInfo);
	private DatasetsPage 	datasetsPage;
	private ViewsPage 		viewsPage;
	private Controller      controller;

	private ArrayList<ViewInfo> 	viewsInfo = new ArrayList<ViewInfo>();

	public IProject getProject()
	{
		return project;
	}

	/**
	 * Creates a multi-page editor example.
	 */
	public GMSEditor()
	{
		super();
	}

	public void dispose()
	{
		if (controller != null)
			controller.dispose();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.forms.editor.FormEditor#createToolkit(org.eclipse.swt.
	 * widgets.Display)
	 */
	protected FormToolkit createToolkit(Display display)
	{
		// Create a toolkit that shares colors between editors.
		return new FormToolkit(GMSPlugin.getDefault().getFormColors(display));
	}

	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException
	{
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid input: Must be IFileEditorInput");
		
		super.init(site, editorInput);
		
		project = ((FileEditorInput) getEditorInput()).getFile().getProject();
		
		GMSPlugin.setProjectName(project.getName());
		
		GMSPlugin.setEditor(this);
		
		System.err.println("Project name: " + project.getName() + "  editor: " + this);
		
		setPartName(project.getName());	
		
		
		FileInputStream stream = null;
		try
		{
			IFile file = ((FileEditorInput) getEditorInput()).getFile();
			stream = (FileInputStream) GMSPlugin.openInputStream(file);
			projectInfo.parse(stream);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
			
		System.out.println("end of editor init Stream = " + stream);
	}

	/**
	 * Creates the pages of the multi-page editor.
	 */
	protected void addPages()
	{
		createOverviewPage();
		createDatasetsPage();
		createViewsPage();
		createXMLPage();
	}

	private void createViewsPage()
	{
		viewsPage = new ViewsPage(this);
		
		try
		{
			int index = addPage(viewsPage);
			viewsPage.setIndex(index);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}	
		
		initViews();
	}

	public void initViews()
	{
		if (controller != null)
		{
			//HACK  TODO
			viewsInfo.clear();
			
			controller.getViews( viewsInfo );
			viewsPage.addViews( viewsInfo );
		}
	}

	private void createDatasetsPage()
	{
		datasetsPage = new DatasetsPage(this);
		
		try
		{
			int index = addPage(datasetsPage);
			datasetsPage.setIndex(index);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}	
		
	}

	private void createOverviewPage()
	{
		overviewPage = new OverviewPage(this);
	
		try
		{
			int index = addPage(overviewPage);
			overviewPage.setIndex(index);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}	
	}

	/**
	 * Creates the XML source page of the GMS editor
	 */
	void createXMLPage()
	{
		try
		{
			xmlEditor = new GMSXMLEditor();
			xmlEditor.initialize(this);
			int index = addPage(xmlEditor, getEditorInput());
			xmlEditor.setIndex(index);
			setPageText(index, xmlEditor.getTitle());
		}
		catch (PartInitException e)
		{
			ErrorDialog.openError(getSite().getShell(), "Error creating nested text editor", null, e.getStatus());
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves the multi-page editor's document.
	 */
	public void doSave(IProgressMonitor monitor)
	{
		if (getActivePage() == 0)
			overviewPage.updateModel();
		else if (getActivePage() == 3)
				xmlEditor.updateModel();
		
		saveProject();
	}

	/**
	 * This saves the current project by serializing the model - as opposed
	 * to saving out the project's file content.
	 * 
	 */
	private void saveProject()
	{
		try
		{
			IFile file = ((FileEditorInput) getEditorInput()).getFile();

			FileOutputStream stream = GMSPlugin.openOutputStream(file);

			projectInfo.serialize(stream);

			soiled = false;
			firePropertyChange(IEditorPart.PROP_DIRTY);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Saves the multi-page editor's document as another file. Also updates the
	 * text for page 0's tab, and updates this multi-page editor's input to
	 * correspond to the nested editor's.
	 */
	public void doSaveAs()
	{
		new RuntimeException("SaveAs not allowed in GMS!");
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker)
	{
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}


	/*
	 * (non-Javadoc) Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed()
	{
		return false;
	}

	/**
	 * Calculates the contents of page 2 when the it is activated.
	 */	
	protected void pageChange(int newPageIndex)
	{
		super.pageChange(newPageIndex);
	}
	
	
	/**
	 * Closes all project files on project close.
	 */
	public void resourceChanged(final IResourceChangeEvent event)
	{
		System.out.println("GMSEditor:resourceChanged: " +  event);
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE)
		{
			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i < pages.length; i++)
					{
						if (((FileEditorInput) xmlEditor.getEditorInput()).getFile().getProject().equals(event.getResource()))
						{
							IEditorPart editorPart = pages[i].findEditor(xmlEditor.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}
			});
		}
		else if (event.getType() == IResourceChangeEvent.POST_CHANGE)
		{
			IResource resource = event.getResource();
			if (resource != null)
				System.out.println("resource changed: " + resource.getName());
		} 
	}
	
	public String serializeToTemp() //throws Exception
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		projectInfo.serialize(stream);
		
		return stream.toString();	
	}
	
	public boolean isDirty()
	{		
		//System.out.println("GMSEditor:isDirty = " + soiled);
		return soiled;
	}
	
	public void setDirty( boolean dirty )
	{
		soiled = dirty;
		//System.out.println("GMSEditor:setDirty " + soiled);	
		firePropertyChange(IEditorPart.PROP_DIRTY);
	}
	
	public void setFocus()
	{
		super.setFocus();
		
		GMSPlugin.setEditor(this);
		System.err.println("Part got focus: " + project.getName() + " this: " + this);
	}
	
	public String getProjectProperty( QualifiedName prop )
	{
		String result = "";
		try
		{
			result = project.getPersistentProperty(prop);
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
		
		return result;
	}
	
	public ProjectInfo getProjectInfo()
	{
		return projectInfo;
	}
	
	public void createComplete()
	{
		//System.err.println("Create complete called");
		
		// now that the model is complete, force a refresh
		overviewPage.setActive(true);
		xmlEditor.reloadModel();
		saveProject();
	}
	
	public ManifestInput getDatasetsInput()
	{
		return datasetsInput;
	}

	public Controller getController()
	{
		return controller;
	}

	public void setController(Controller controller)
	{
		this.controller = controller;
	}

	public void viewsCreated()
	{
		if (controller != null)
			controller.viewsCreated();
	}
}

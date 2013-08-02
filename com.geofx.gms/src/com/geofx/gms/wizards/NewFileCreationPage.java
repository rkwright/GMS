package com.geofx.gms.wizards;

/*******************************************************************************
 * Copyright (c) 2003-2006 Sybase, Inc.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sybase, Inc. - initial API and implementation
 ******************************************************************************/

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import com.geofx.gms.plugin.Strings;

/**
 * This class is the wizard page used to prompt the user to select a project and
 * a file name for the new map.
 * 
 * A single IWizardPageCompleteListener registered with this page will be called
 * for every event to let it know whether or not this page is complete.
 * 
 * The Workspace used for detecting duplicate files may be set by calling
 * setWorkspace(IWorkspace).
 */
public class NewFileCreationPage extends WizardNewFileCreationPage
{
	private static String PAGE_ID = "com.geofx.wizards.NewFileCreationPage";
	
	private IContainer 	selectedContainer = null;   // The parent Workspace of this wizard page
	private IFile 		selectedFile = null;
	private String 		fileExtension; 				// The extension for the new file

	/**
	 * Default constructor
	 */
	public NewFileCreationPage()
	{
		super(PAGE_ID, StructuredSelection.EMPTY);
	}

	/**
	 * Constructor used when a selection is available
	 * 
	 * @param selection
	 * @param fileExt 
	 */
	public NewFileCreationPage( IStructuredSelection selection, String fileExt )
	{
		super(PAGE_ID, selection);
		
		init(fileExt, selection);
	}

	public void init(String fileExtension, ISelection selection)
	{
		setTitle(Strings.getString("NewFileCreationPage.title"));
		setDescription(Strings.getString("NewFileCreationPage.description"));

		this.fileExtension = fileExtension;
		setAllowExistingResources(false);
		if (fileExtension.length() != 0)
			setFileExtension(fileExtension);
		
		if (!selection.isEmpty())
		{
			Object o = ((IStructuredSelection) selection).getFirstElement();
			if (o instanceof IResource)
			{
				IResource res = (IResource) o;
				if (res.getType() == IResource.FOLDER || res.getType() == IResource.PROJECT)
				{
					selectedContainer = (IContainer) res;
				}
				else if (res.getType() == IResource.FILE)
				{
					selectedContainer = res.getParent();
					if (res instanceof IFile)
					{
						selectedFile = (IFile) res;
					}
				}
			}
		}
	}

	/**
	 * Get current value of file name string and append the required
	 * file name extension to it if it does not already have it.
	 * 
	 * @return
	 */
	public String getFullFileName()
	{
		String fileName = getFileName();
		if (fileExtension != null && !fileName.endsWith('.' + fileExtension))
		{
			fileName += '.' + fileExtension;
		}
		return fileName;
	}

	public IFile getOutputFile()
	{
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IContainer container = (IContainer) root.findMember(getContainerFullPath());
		
		return root.getFile(container.getFullPath().append(getFullFileName()));
	}

	/**
	 * Checks whether the user can advance to the next wizard page. In this
	 * case, it just checks whether file name entered by the user is unique in
	 * the current workspace.
	 * 
	 * @return <tt>true</tt> if the user can flip to the next page;
	 *         <tt>false</tt> otherwise
	 */
	public boolean canFlipToNextPage()
	{
		String fileName = getFullFileName();
		// check to see if the system file already exists
		// CR336500 - getFolder() does not work when the container path is the
		// root project folder.
		IResource container;
		IPath localContainerPath = getContainerFullPath();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if (localContainerPath != null)
		{
			if (localContainerPath.segmentCount() == 1)
			{
				// Must use getProject()
				container = root.getProject(localContainerPath.lastSegment());
			}
			else
			{
				// OK to use getFolder()
				container = root.getFolder(getContainerFullPath());
			}
			// CR319958 - The proper way to get the full path for all files
			// (including files in project folders that are not directly in the
			// workspace folder, e.g. imported projects).
			IPath absoluteContainerPath = container.getLocation();
			File systemFile = new File(absoluteContainerPath.toOSString(), fileName);
			if (systemFile.exists())
			{
				// display an error message and disable "Next" button on wizard
				setErrorMessage(Strings
						.getString("NewFileCreationPage.fileExists") + container.getFullPath().append(fileName)); //$NON-NLS-1$
				((NewDataSetWizard)getWizard()).setCanFinish(false);
				return false;
			}
		}
		
		return super.canFlipToNextPage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void createControl(Composite parent)
	{
		super.createControl(parent);
		
		if (selectedContainer != null)
		{
			setContainerFullPath(selectedContainer.getFullPath());
		}
		
		if (selectedFile != null)
		{
			int idx = selectedFile.getName().lastIndexOf('.');

			if (idx == -1)
			{
				setFileName(selectedFile.getName());
			}
			else
			{
				setFileName(selectedFile.getName().substring(0, idx));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.dialogs.WizardNewFileCreationPage#createAdvancedControls
	 * (org.eclipse.swt.widgets.Composite)
	 */
	protected void createAdvancedControls(Composite parent)
	{
		// We don't want any...
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.dialogs.WizardNewFileCreationPage#validateLinkedResource()
	 */
	protected IStatus validateLinkedResource()
	{
		return Status.OK_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.datatools.connectivity.internal.ui.wizards.ISummaryDataSource
	 * #getSummaryData()
	 */
	public List<String[]> getSummaryData()
	{
		List<String[]> summaryData = new ArrayList<String[]>(1);

		summaryData.add(new String[] { Strings.getString("WizardNewFileCreationPage.summary.fileName"),
				getOutputFile().getFullPath().toString() });   //$NON-NLS-1$

		return summaryData;
	}

	protected void createLinkTarget ()
	{
	}
}
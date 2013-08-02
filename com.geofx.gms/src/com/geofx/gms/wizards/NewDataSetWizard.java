/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com>
 *     		- Bug 44162 [Wizards]  Define constants for wizard ids of new.file, new.folder, and new.project
 *******************************************************************************/
package com.geofx.gms.wizards;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;

import com.geofx.gms.datasets.Grid;
import com.geofx.gms.datasets.ZipArray;
import com.geofx.gms.datasets.ClassUtil.ClassType;
import com.geofx.gms.datasets.Dataset.DatasetType;
import com.geofx.gms.editor.GMSEditor;
import com.geofx.gms.plugin.Constants;
import com.geofx.gms.plugin.GMSPlugin;
import com.geofx.gms.plugin.Strings;
/**
 * This class is cloned from the BasicNewFileResourceWizard. That class is not 
 * intended to subclassed.
 *  
 * During the call to <code>open</code>, the wizard dialog is presented to the
 * user. When the user hits Finish, a file resource at the user-specified
 * workspace path is created, the dialog closes, and the call to
 * <code>open</code> returns.
 * </p>
 * 
 */
public class NewDataSetWizard extends Wizard implements IWizard
{

	/**
	 * The wizard id for creating new files in the workspace.
	 * 
	 * @since 3.4
	 */
	public static final String  WIZARD_ID = "com.geofx.wizards.NewDataSetWizard"; //$NON-NLS-1$

	private NewFileCreationPage 	newFileCreationPage;
	private DataSetParmPage			dataSetParmPage;
	private DataSetDimPage			dataSetDimPage;

	private int 					nDims;
	private int[]					dimRay;
	private int 					classType;
	private int 					dataSetType;
	private String 					objectName = new String();
	
	private IWorkbench 				workbench;		// current workbench
	protected IStructuredSelection 	selection;		// The current selection
	protected boolean				finishOK = false;

	protected ZipArray				zipArray;
	protected GMSEditor				editor;
	
	/**
	 * Creates a wizard for creating a new file resource in the workspace.
	 * @param editor 
	 */
	public NewDataSetWizard(GMSEditor editor)
	{
		super();
		
		this.editor = editor;
	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	public void addPages()
	{
		super.addPages();
		newFileCreationPage = new NewFileCreationPage(getSelection(), "gza"); //$NON-NLS-1$
		addPage(newFileCreationPage);
		
		dataSetParmPage = new DataSetParmPage();
		addPage(dataSetParmPage);
		
		dataSetDimPage = new DataSetDimPage();
		addPage(dataSetDimPage);
	}

	/**
	 * The <code>BasicNewResourceWizard</code> implementation of this
	 * <code>IWorkbenchWizard</code> method records the given workbench and
	 * selection, and initializes the default banner image for the pages by
	 * calling <code>initializeDefaultPageImageDescriptor</code>. Subclasses may
	 * extend.
	 */
	public void init(IWorkbench workbench, IStructuredSelection currentSelection)
	{
		this.workbench = workbench;
		this.selection = currentSelection;

		setWindowTitle(Strings.getString("NewFileWizardPage.shellTitle"));
		setNeedsProgressMonitor(true);

		initializeDefaultPageImageDescriptor();

	}

	/**
	 * Initializes the default page image descriptor to an appropriate banner.
	 * By calling <code>setDefaultPageImageDescriptor</code>. The default
	 * implementation of this method uses a generic new wizard image.
	 * <p>
	 * Subclasses may reimplement.
	 * </p>
	 */
	protected void initializeDefaultPageImageDescriptor()
	{
		ImageDescriptor desc = GMSPlugin.getDefault().getImageDescriptor(GMSPlugin.DATASET_IMG);//$NON-NLS-1$
		setDefaultPageImageDescriptor(desc);
	}
	
	
	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	public boolean performFinish()
	{
		IFile file = newFileCreationPage.createNewFile();
		if (file == null)
		{
			return false;
		}

		zipArray = new ZipArray();
 		Grid grid = new Grid(DatasetType.Grid,ClassType.convert(classType), objectName, dimRay, null);
		try
		{
			zipArray.save(file.getLocation(), grid );
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		
		IProject project = file.getProject();
		
		editor.getProjectInfo().addManifestEntry(UUID.randomUUID().toString(), file.getFullPath().toPortableString(), Constants.GAZ_MIMETYPE);
		
		return true;
	}

	public void setNDims(int nDims)
	{
		this.nDims = nDims;		
	}

	public int getNDims()
	{
		return nDims;
	}
	
	public void setClassType(int classType)
	{
		this.classType = classType;
	}

	public void setDataSetType( int dataSetType )
	{
		this.dataSetType = dataSetType;		
	}

	public int getDataSetType()
	{
		return dataSetType;
	}
		
	/**
	 * Returns the selection which was passed to <code>init</code>.
	 * 
	 * @return the selection
	 */
	public IStructuredSelection getSelection()
	{
		return selection;
	}

	/**
	 * Returns the workbench which was passed to <code>init</code>.
	 * 
	 * @return the workbench
	 */
	public IWorkbench getWorkbench()
	{
		return workbench;
	}
	
	public boolean canFinish()
	{
		return finishOK;
	}

	public void setCanFinish(boolean canFinish)
	{
		this.finishOK = canFinish;
	}

	public void setDimRay(int[] dimRay)
	{
		this.dimRay = dimRay;
	}
}

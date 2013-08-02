/*******************************************************************************
 * Copyright (c) 2008 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 ****************************************************************************/

package com.geofx.gms.wizards;

import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import com.geofx.gms.editor.GMSEditor;
import com.geofx.gms.model.ProjectInfo;
import com.geofx.gms.plugin.Constants;
import com.geofx.gms.plugin.GMSPlugin;

/**
 * 
 */

public class NewProjectWizard extends BasicNewProjectResourceWizard  
{
	private MetadataWizardPage 	metadataWizardPage;
	private String				projectName;
	private IPath				projectPath;

	private String				creationDate;
	private UUID				uuid = UUID.randomUUID();
	private static ProjectInfo projectInfo = new ProjectInfo();

	/**
	 * Constructor for NewProjectWizard.
	 */
	public NewProjectWizard()
	{
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages()
	{
		super.addPages();
		
		metadataWizardPage = new MetadataWizardPage();
		addPage(metadataWizardPage);
	
	}

	@Override
	public IWizardPage getNextPage( IWizardPage page )
	{
		//System.out.println("Request to get next page: " + page.getName() + " class:" + page.getClass().getName());
		if (page instanceof org.eclipse.ui.dialogs.WizardNewProjectCreationPage)
		{
			WizardNewProjectCreationPage  page0 = (WizardNewProjectCreationPage)page;
			updateProjectPage(page0);
		}
		
		return super.getNextPage(page);
	}

	private void updateProjectPage(WizardNewProjectCreationPage page0)
	{
		projectName = page0.getProjectName();
		projectPath = page0.getLocationPath();

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		String text = df.format(new Date());
		creationDate = text.substring(0, 22) + ":" + text.substring(22);

		metadataWizardPage.setProjectName(projectName);
		metadataWizardPage.setProjectPath(projectPath);
		metadataWizardPage.setUniqueID(uuid);
		metadataWizardPage.setCreationDate(creationDate);
			
		//System.out.println("projectName: " + projectName + " path: " + projectPath + " uri: " + uri);
	}
	
	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish()
	{
		super.performFinish();
				
		final IProject 	newProject = getNewProject();
		
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException
			{
				
				try
				{
					doFinish(newProject, monitor);
				}
				catch (CoreException e)
				{
					throw new InvocationTargetException(e);
				}
				catch (Exception e )
				{
					e.printStackTrace();
				}
				finally
				{
					monitor.done();
				}
				
			}
		};
		
		try
		{
			getContainer().run(true, false, op);
		}
		catch (InterruptedException e)
		{
			return false;
		}
		catch (InvocationTargetException e)
		{
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		
		return true;
	}

	/**
	 * The worker method. It will find the container, create the file if missing
	 * or just replace its contents, and open the editor on the newly created
	 * file.
	 */
	private void doFinish(final IProject newProject, IProgressMonitor monitor) throws Exception
	{
		projectName = newProject.getName();
	
		monitor.beginTask("Creating " + projectName, 2);
		
		IWorkspaceRoot 	root = ResourcesPlugin.getWorkspace().getRoot();
		IResource 		resource = root.findMember(new Path(projectName));
		
		if (!resource.exists() || !(resource instanceof IContainer))
		{
			throw new Exception("Container \"" + projectName + "\" does not exist.");
		}
		
		IContainer container = (IContainer) resource;
		
		createContainerFile( container, monitor );
		
		final IFile file = createProjectFile( container, monitor );
				
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		
		getShell().getDisplay().asyncExec(new Runnable()
		{
			public void run()
			{
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try
				{					
					GMSEditor gms = (GMSEditor) IDE.openEditor(page, file, true);
					updateProjectInfo( newProject, gms );
				}
				catch (PartInitException e)
				{
					e.printStackTrace();
				}
				catch (CoreException e)
				{
					e.printStackTrace();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		
		monitor.worked(1);
	}
	
	/**
	 * Create the container file, which simply points to the project file
	 * @param container
	 * @param monitor
	 */
	private void createContainerFile( IContainer container, IProgressMonitor monitor )
	{
		String folder_name    = "META-INF";
		
		// create the META-INFee folder
		final IFolder 	folder = container.getFolder(new Path(folder_name));
		
		projectInfo.serializeContainerFile(folder);
	}

	private IFile createProjectFile( IContainer container, IProgressMonitor monitor )
	{	
		IFile file = container.getFile(new Path(Constants.GMS_PROJECT));

		FileOutputStream stream = GMSPlugin.openOutputStream(file);

		projectInfo.serialize(stream);

		return file;     
	}
	
	private void updateProjectInfo( IProject project, GMSEditor gmsEditor ) throws CoreException
	{
		ProjectInfo projectInfo = gmsEditor.getProjectInfo();
		
		// save the unique identifier for the project.  This will be serialized into the .project file for next time
		project.setPersistentProperty(Constants.UUID_PROPERTY_NAME, uuid.toString() );

		// save the creation date for the project.  This will be serialized into the .project file for next time
		project.setPersistentProperty(Constants.DATE_PROPERTY_NAME, creationDate );

		projectInfo.setMetadataItem("title", metadataWizardPage.getProjectTitle());
		projectInfo.setMetadataItem("description", metadataWizardPage.getProjectDescription());
		
		gmsEditor.createComplete();
	}

}
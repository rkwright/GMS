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

package com.geofx.gms.plugin;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.geofx.gms.editor.GMSEditor;

/**
 * The activator class controls the plug-in life cycle
 */
public class GMSPlugin extends AbstractUIPlugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "com.geofx.gms";

	// The shared instance
	private static GMSPlugin plugin;

	// Resource bundle.
	private ResourceBundle 	resourceBundle;
	
	private static String 	projectName;

	private static GMSEditor	editor = null;
	
	private FormColors 		formColors;
	
	public static final String 	ICON_PATH   = "icons/";
	public static final String 	DATASET_IMG = "dataset"; //$NON-NLS-1$
	public static final String  BINARY_ICON = "binary_icon";
	public static final String  START_BUTTON = "Start_Button";
	public static final String  PAUSE_BUTTON = "Pause_Button";
	public static final String  STOP_BUTTON = "Stop_Button";
	public static final String  STOP_BUTTON_DISABLED = "Stop_Button_Disabled";

	/**
	 * The constructor
	 */
	public GMSPlugin()
	{
		plugin = this;
		
		System.err.println("Java library path " + System.getProperty("java.library.path"));
		
		try
		{
			resourceBundle = ResourceBundle.getBundle("com.geofx.xmleditor.resources.XMLEditorPluginResources");
		}
		catch (MissingResourceException x)
		{
			resourceBundle = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
	{
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static GMSPlugin getDefault()
	{
		return plugin;
	}

	public FormColors getFormColors(Display display)
	{
		if (formColors == null)
		{
			formColors = new FormColors(display);
			formColors.markShared();
		}
		return formColors;
	}
	
	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() 
	{
		return resourceBundle;
	}

	/**
	 * Just a convenience function to hide all the shenanigans involved with getting a stream
	 * from Eclipse's abstractions.
	 * 
	 * @param file
	 * @return
	 */
	public static FileOutputStream openOutputStream( IFile file )
	{
		String fullPath = getFullPath(file);
			
		FileOutputStream stream = null;
		try
		{
			stream = new FileOutputStream(fullPath);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		return stream;
	}

	/**
	 * Just a convenience function to hide all the shenanigans involved with getting a stream
	 * from Eclipse's abstractions.
	 * 
	 * @param file
	 * @return
	 */
	public static FileInputStream openInputStream( IFile file )
	{
		String fullPath = getFullPath(file);
			
		FileInputStream stream = null;
		try
		{
			stream = new FileInputStream(fullPath);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		return stream;
	}

	/**
	 * Gets the workspace and computes the full path for the give IFile object
	 * relative to the current container.
	 * 
	 * @param file
	 * @return
	 */
	public static String getFullPath(IFile file)
	{
		IPath path = file.getFullPath().makeAbsolute();
			
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			
		String fullPath = root.getLocation().toPortableString() + path.toPortableString();
		
		//System.out.println("path: " + fullPath);
		
		return fullPath;
	}

	/**
	 * Take a path that is relative to the current project container or JUnit runtime
	 * and return a valid path object that is fully qualified.
	 * 
	 * Note that if one is in the JUnit plugin mode, then there is no ProjectName, so
	 * the path is returned relative to <sandbox>/junit-runtime. And if one is not in JUnit
	 * plugin mode and uses the else section, then the project container will not be accounted
	 * for and the path will be relative to the workspace only.
	 * 
	 * @param subPath
	 * @return
	 */
	public static Path getRelativePath( String subPath )
	{
		IFile 	localFile = null;
		Path	path = null;
		try
		{
			if (projectName != null && projectName.length() > 0)
			{
				localFile = getContainer().getFile(new Path(subPath));
				path = new Path(localFile.getLocation().toPortableString());
			}
			else
			{
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(subPath));
				String fullPath = GMSPlugin.getFullPath(file);
				path = new Path(fullPath);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return path;
	}

	/**
	 * Get the container for this project.  Throws an exception if the project/projectName 
	 * hae not been set.
	 * 
	 * @return
	 * @throws Exception
	 */
	public static IContainer getContainer() throws Exception
	{	
		IWorkspaceRoot 	root = ResourcesPlugin.getWorkspace().getRoot();
		IResource 		resource = root.findMember(new Path(projectName));
	
		if (!resource.exists() || !(resource instanceof IContainer))
		{
			throw new Exception("Container \"" + projectName + "\" does not exist.");
		}
	
		return (IContainer)resource;
	}
	
	public static String getProjectName()
	{
		return projectName;
	}

	public static void setProjectName(String projectName)
	{
		GMSPlugin.projectName = projectName;
	}

	public Image getImage(String key)
	{
		return getImageRegistry().get(key);
	}

	public ImageDescriptor getImageDescriptor(String key)
	{
		return getImageRegistry().getDescriptor(key);
	}

	private ImageDescriptor fetchImage( Bundle bundle, String imageName )
	{
		return ImageDescriptor.createFromURL(FileLocator.find(bundle,new Path(ICON_PATH + imageName ), null));
	}
	
	public void initializeImageRegistry(ImageRegistry imageRegistry)
	{
		super.initializeImageRegistry(imageRegistry);
		
		Bundle bundle = Platform.getBundle(PLUGIN_ID);
		
		imageRegistry.put(BINARY_ICON, fetchImage(bundle, BINARY_ICON + ".gif"));
		imageRegistry.put(DATASET_IMG, fetchImage(bundle, DATASET_IMG + ".gif"));
		
		imageRegistry.put(Constants.RESTART_EN, fetchImage(bundle, Constants.RESTART_EN + ".gif"));
		imageRegistry.put(Constants.RESUME_EN, fetchImage(bundle, Constants.RESUME_EN + ".gif"));
		imageRegistry.put(Constants.SUSPEND_EN, fetchImage(bundle, Constants.SUSPEND_EN + ".gif"));
		imageRegistry.put(Constants.TERMINATE_EN, fetchImage(bundle, Constants.TERMINATE_EN + ".gif"));
		imageRegistry.put(Constants.TERMINATE_DIS, fetchImage(bundle, Constants.TERMINATE_DIS + ".gif"));
		imageRegistry.put(Constants.STEPOVER_EN, fetchImage(bundle, Constants.STEPOVER_EN + ".gif"));
		imageRegistry.put(Constants.STEPOVER_DIS, fetchImage(bundle, Constants.STEPOVER_DIS + ".gif"));

		imageRegistry.put(START_BUTTON, fetchImage(bundle, START_BUTTON + ".gif"));
		imageRegistry.put(PAUSE_BUTTON, fetchImage(bundle, PAUSE_BUTTON + ".gif"));
	}

	public static GMSEditor getEditor()
	{
		return editor;
	}

	public static void setEditor(GMSEditor editor)
	{
		GMSPlugin.editor = editor;
	}


}

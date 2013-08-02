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

package com.geofx.gms.editor;

import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.geofx.gms.plugin.Strings;

public class DatasetsPage extends FormPage
{
	private static String	id = "com.geofx.gms.editor.DatasetsPage";

	private GMSEditor gmsEditor;

	public GMSEditor getGmsEditor()
	{
		return gmsEditor;
	}

	private boolean dirty = false;

	//private boolean stale = true;
	
//	private boolean initComplete = false;
	
	DatasetsMasterBlock block;


	public DatasetsPage(GMSEditor editor)
	{
		super(editor, id, Strings.getString("DatasetsPage.label")); //$NON-NLS-1$ //$NON-NLS-2$

		gmsEditor = editor;
		
		block = new DatasetsMasterBlock(this);
	}

	protected void createFormContent(final IManagedForm managedForm)
	{
		final ScrolledForm form = managedForm.getForm();
		// FormToolkit toolkit = managedForm.getToolkit();
		form.setText(Strings.getString("DatasetsPage.title")); //$NON-NLS-1$
		//form.setBackgroundImage(GMSPlugin.getDefault().getImage(GMSPlugin.IMG_FORM_BG));
		block.createContent(managedForm);
	}
	
	public FormEditor getEditor()
	{
		return gmsEditor;
	}

	private void updateProjectInfo()
	{
		//if (initComplete)
		//	updateModel();

		gmsEditor.setDirty(true);
		dirty = true;
	}

	public boolean isDirty()
	{
		//super.isDirty();
		
		System.out.println("DatasetsPage:isDirty = " + dirty);
		return dirty;
	}
	
	public void setActive(boolean active)
	{
		super.setActive(active);

		System.out.println("DatasetsPage:setActive Called - flag = " + active );

		// we need to save the soiled flag since populating the controls looks like editing
		boolean saveSoiled = dirty;
		
		if (true)  //initComplete)
		{			
			updateControls();
		}
		
		dirty = saveSoiled;
		gmsEditor.setDirty(dirty);
	}

	public void updateControls()
	{
	}

	@Override
	public boolean canLeaveThePage()
	{
		updateModel();
		System.out.println("DatasetsPage: canLeavePage");
		return true;
	}

	public void updateModel()
	{
	}

	/*
	public void setStale(boolean b)
	{
		// TODO Auto-generated method stub
		
	}

	public boolean isInitComplete()
	{
		return initComplete;
	}

	public void setInitComplete(boolean initComplete)
	{
		this.initComplete = initComplete;
	}
	*/
}

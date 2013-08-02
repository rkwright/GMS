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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import com.geofx.gms.plugin.Constants;
import com.geofx.gms.plugin.Strings;

public class OverviewPage extends FormPage
{
	private static String	id = "com.geofx.gms.editor.OverviewPage";

	private Label 	nameText;
	private Label 	pathText;
	private Label 	dateText;
	private Label 	uuidText;
	private Label 	titleLabel;
	private Label 	descriptionLabel;

	private Text 	descriptionText;
	private Text 	titleText;
	
	private GMSEditor gmsEditor;

	private boolean dirty = false;

	//private boolean stale = true;
	
//	private boolean initComplete = false;

	public OverviewPage(GMSEditor editor)
	{
		super(editor, id, Strings.getString("OverviewPage.label")); //$NON-NLS-1$ //$NON-NLS-2$
		gmsEditor = editor;
		
		/*
		FileInputStream stream = null;
		try
		{
			IFile file = ((FileEditorInput) editor.getEditorInput()).getFile();
			stream = (FileInputStream) GMSPlugin.openInputStream(file);
			gmsEditor.getProjectInfo().loadModel(stream);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		System.out.println("Stream = " + stream);
		*/

	}

	protected void createFormContent(IManagedForm managedForm)
	{
		ScrolledForm form = managedForm.getForm();
		form.setText(Strings.getString("OverviewPage.label")); //$NON-NLS-1$

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		form.getBody().setLayout(layout);

		createSection(managedForm.getForm(), managedForm.getToolkit(), "Properties",
				"Overall properties of this project", 1);
	}

	private Composite createSection(final ScrolledForm form, FormToolkit toolkit, String title,
			String desc, int i)
	{
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR
				| Section.DESCRIPTION | Section.EXPANDED);
		section.setText(title);
		//section.setDescription(desc);

		Composite client = toolkit.createComposite(section, SWT.WRAP);

		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 2;
		client.setLayout(layout);

		section.setClient(client);

		toolkit.createLabel(client, "Project:");

		nameText = toolkit.createLabel(client, "", SWT.SINGLE | SWT.READ_ONLY);

		GridData nameData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		nameText.setLayoutData(nameData);

		toolkit.createLabel(client, "UUID:");

		uuidText = toolkit.createLabel(client, "", SWT.SINGLE | SWT.READ_ONLY);
		GridData uuidData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		uuidText.setLayoutData(uuidData);

		toolkit.createLabel(client, "Location:", SWT.NONE);

		pathText = toolkit.createLabel(client, "", SWT.SINGLE | SWT.READ_ONLY);
		GridData pathData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		pathText.setLayoutData(pathData);

		toolkit.createLabel(client, "Created:", SWT.NONE);

		dateText = toolkit.createLabel(client, "", SWT.SINGLE | SWT.READ_ONLY);
		GridData dateData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		dateText.setLayoutData(dateData);

		titleLabel = toolkit.createLabel(client, "Title:", SWT.NONE);
		GridData titleLabelData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		titleLabelData.horizontalSpan = 2;
		titleLabel.setLayoutData(titleLabelData);

		titleText = toolkit.createText(client, "", SWT.SINGLE | SWT.BORDER);
		GridData titleData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		titleData.horizontalSpan = 2;
		titleText.setLayoutData(titleData);

		titleText.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				updateProjectInfo();
			}
		});

		descriptionLabel = toolkit.createLabel(client, "Description:", SWT.NONE);
		GridData descriptionLabelData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		descriptionLabelData.horizontalSpan = 2;
		descriptionLabel.setLayoutData(descriptionLabelData);

		descriptionText = toolkit.createText(client, "", SWT.BORDER | SWT.WRAP | SWT.MULTI);
		GridData descriptionData = new GridData();
		descriptionData.horizontalSpan = 2;
		descriptionData.horizontalAlignment = SWT.FILL;
		descriptionData.grabExcessHorizontalSpace = true;
		descriptionData.verticalAlignment = SWT.FILL;
		descriptionData.grabExcessVerticalSpace = true;
		descriptionText.setLayoutData(descriptionData);

		descriptionText.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				updateProjectInfo();
			}
		});

		GridData sectionData = new GridData(GridData.FILL_BOTH);
		section.setLayoutData(sectionData);

		System.out.println("completed form setup");

		return client;
	}

	private void updateProjectInfo()
	{
//		if (initComplete)
//			updateModel();

		gmsEditor.setDirty(true);
		dirty = true;
	}

	public boolean isDirty()
	{
		//super.isDirty();
		
		//System.out.println("OverviewPage:isDirty = " + dirty);
		return dirty;
	}
	
	public void setActive(boolean active)
	{
		super.setActive(active);

		//System.out.println("OverviewPage:setActive Called - flag = " + active );

		// we need to save the soiled flag since populating the controls looks like editing
		boolean saveDirty = dirty;
		
		//if (true)  //initComplete)
		//{			
			updateControls();
		//}
		
		dirty = saveDirty;
		gmsEditor.setDirty(dirty);
	}

	public void updateControls()
	{
		nameText.setText(gmsEditor.getProject().getName());
		pathText.setText(gmsEditor.getProject().getFullPath().toPortableString());
		
		String uuid = gmsEditor.getProjectProperty(Constants.UUID_PROPERTY_NAME );
		if (uuid != null)
		{
			uuidText.setText(gmsEditor.getProjectProperty(Constants.UUID_PROPERTY_NAME ));
			dateText.setText(gmsEditor.getProjectProperty(Constants.DATE_PROPERTY_NAME));
		}

		String title = gmsEditor.getProjectInfo().getMetadataItem(Constants.TITLE);
		if (title != null)
		{
			titleText.setText(gmsEditor.getProjectInfo().getMetadataItem(Constants.TITLE));
			descriptionText.setText(gmsEditor.getProjectInfo().getMetadataItem(Constants.DESCRIPTION));
		}
	}

	@Override
	public boolean canLeaveThePage()
	{
		if (dirty)
			updateModel();
		
		dirty = false;
		
		//System.out.println("OverviewPage: canLeavePage");
		return true;
	}

	public void updateModel()
	{
		gmsEditor.getProjectInfo().setMetadataItem(Constants.DESCRIPTION, descriptionText.getText());
		gmsEditor.getProjectInfo().setMetadataItem(Constants.TITLE, titleText.getText());
	}

	/*
	public void setStale(boolean b)
	{
		// TODO Auto-generated method stub
		
	}
	*/
	
	/*
	public boolean isInitComplete()
	{
		return initComplete;
	}

	public void setInitComplete(boolean initComplete)
	{
		System.err.println("setInitCOmplete.");
		this.initComplete = initComplete;
	}
	*/
}

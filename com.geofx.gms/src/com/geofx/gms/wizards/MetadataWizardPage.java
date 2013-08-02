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

import java.util.UUID;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * This page allows the user to edit the project info as part of the 
 * project-creation process.
 */
public class MetadataWizardPage extends WizardPage
{
	private Text 		nameText;
	private Text 		titleText;
	private Text		pathText;
	private Text		dateText;
	private Text		descriptionText;
	private Text		uuidText;
	private Label		titleLabel;
	private Label		descriptionLabel;

	private String 		projectName;
	private IPath		projectPath;
	private String		creationDate;
	private UUID		uuid = UUID.randomUUID();
	private String 		projectDescription;
	private String 		projectTitle;
	
	/**
	 * Constructor for ProjectWizardPage.
	 * 
	 * @param pageName
	 */
	public MetadataWizardPage()
	{
		super("MetadataWizardPage");
		setTitle("Set Project Info");
		setDescription("This wizard page allows the user to set the global parameters for the project");
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent)
	{
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		
		new Label(container, SWT.NONE).setText("Project:");
		
		nameText = new Text(container, SWT.SINGLE |  SWT.READ_ONLY);
		GridData nameData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		nameText.setLayoutData(nameData);

		new Label(container, SWT.NONE).setText("UUID:");

		uuidText = new Text(container, SWT.SINGLE | SWT.READ_ONLY);
		GridData uuidData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		uuidText.setLayoutData(uuidData);

		new Label(container, SWT.NONE).setText("Location:");
		
		pathText = new Text(container, SWT.SINGLE |  SWT.READ_ONLY);
		GridData pathData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		pathText.setLayoutData(pathData);

		new Label(container, SWT.NONE).setText("Created:");
		
		dateText = new Text(container, SWT.SINGLE |  SWT.READ_ONLY);
		GridData dateData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		dateText.setLayoutData(dateData);

		titleLabel = new Label(container, SWT.NONE);
		titleLabel.setText("Title:");
		GridData titleLabelData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		titleLabelData.horizontalSpan = 2;
		titleLabel.setLayoutData(titleLabelData);
		
		titleText = new Text(container, SWT.SINGLE | SWT.BORDER);
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

		descriptionLabel = new Label(container, SWT.NONE);
		descriptionLabel.setText("Description:");
		GridData descriptionLabelData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		descriptionLabelData.horizontalSpan = 2;
		descriptionLabel.setLayoutData(descriptionLabelData);
		
		descriptionText = new Text(container, SWT.BORDER | SWT.WRAP | SWT.MULTI);
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

		updateControl();
		
		initialize();
		dialogChanged();
		setControl(container);
	}

	private void updateProjectInfo()
	{
		projectDescription = descriptionText.getText();
		projectTitle = titleText.getText();
		
	}

	private void updateControl()
	{
		nameText.setText(projectName != null ? projectName : "");
		titleText.setText(projectName != null ? projectName : "");
		pathText.setText(projectPath != null ? projectPath.toOSString() : "");
		dateText.setText(creationDate != null ? creationDate : "");
		uuidText.setText(uuid != null ? uuid.toString() : "");	
	}
	
	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */
	private void initialize()
	{

	}

	/**
	 * Ensures that both text fields are set.
	 */
	private void dialogChanged()
	{

		updateStatus(null);
	}

	private void updateStatus(String message)
	{
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public void setProjectName(String projectName)
	{
		this.projectName = projectName;
		nameText.setText(projectName);
		titleText.setText(projectName);
	}

	public void setProjectPath(IPath projectPath)
	{
		this.projectPath = projectPath;
		pathText.setText(projectPath.toOSString());
	}

	public void setCreationDate(String creationDate)
	{
		this.creationDate = creationDate;
		dateText.setText(creationDate);
	}

	public String getProjectDescription()
	{
		return projectDescription;
	}

	public String getProjectTitle()
	{
		return projectTitle;
	}

	public void setUniqueID(UUID uniqueID)
	{
		this.uuid = uniqueID;
		uuidText.setText(uuid.toString());	
	}
}
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

import java.lang.reflect.Array;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.geofx.gms.datasets.ClassUtil;
import com.geofx.gms.datasets.Dataset;
import com.geofx.gms.datasets.ClassUtil.ClassType;
import com.geofx.gms.datasets.Dataset.DatasetType;

/**
 * This page allows the user to edit the project info as part of the 
 * project-creation process.
 */
public class DataSetParmPage extends WizardPage
{
	private static String PAGE_ID = "com.geofx.gms.wizards.DataSetDimPage";
	
	private Combo			classCombo;
	private Combo			typeCombo;
	private Text			dimsText;
	private Composite 		composite;
	private Dataset			dataSet;
	private int[]			dimRay;
	private int				nDims;

	NewDataSetWizard 		wizard;

	/**
	 * Constructor for ProjectWizardPage.
	 * 
	 * @param pageName
	 */
	public DataSetParmPage()
	{
		super(PAGE_ID);	
		
		setTitle("Specify Dataset Dimensions");
		setDescription("This wizard page allows the user to set the dimensions of the dataset array");
		
		setPageComplete(false);
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent)
	{
		this.wizard = (NewDataSetWizard) getWizard();

		composite = new Composite(parent, SWT.NULL);

		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 10;
		layout.numColumns = 2;
		
		composite.setLayout(layout);
		
		Label label = new Label(composite, SWT.RIGHT);
		label.setText("DataSetType:");
		
		typeCombo = new Combo(composite, SWT.READ_ONLY);
	
		for (DatasetType current : DatasetType.values())
		{
			typeCombo.add(current.toString());
		}

		typeCombo.select(0);

		typeCombo.addModifyListener( new ModifyListener ()
		{
			public void modifyText(ModifyEvent arg0)
			{
				validateEntries();
			}
		});

		GridData grid = new GridData();
		grid.minimumWidth = 100;
		grid.grabExcessHorizontalSpace = true;
		typeCombo.setLayoutData(grid);
		
		label = new Label(composite, SWT.RIGHT);
		label.setText("DataType:");
		
		classCombo = new Combo(composite, SWT.READ_ONLY);
		
		for (ClassType current : ClassType.values())
		{
			classCombo.add(current.toString());
		}

		classCombo.select(0);

		classCombo.addModifyListener( new ModifyListener ()
		{
			public void modifyText(ModifyEvent arg0)
			{
				validateEntries();
			}
		});
		
		grid = new GridData();
		grid.minimumWidth = 100;
		grid.grabExcessHorizontalSpace = true;
		classCombo.setLayoutData(grid);
		
		label = new Label(composite, SWT.RIGHT);
		label.setText("Dimensions:");
		
		dimsText = new Text(composite, SWT.SINGLE|SWT.BORDER);

		dimsText.addListener(SWT.Verify, new Listener()
		{
			public void handleEvent(Event e)
			{
				e.doit = "0123456789".indexOf(e.text) >= 0 ; 
			}
		});
		
		dimsText.addModifyListener(new ModifyListener()
		{

			public void modifyText(ModifyEvent arg0)
			{
				validateEntries();
			}
		});
	
		grid = new GridData();
		grid.minimumWidth = 130;
		grid.grabExcessHorizontalSpace = true;
		dimsText.setLayoutData(grid);

		setControl(composite);
	}

	private void validateEntries()
	{
		setPageComplete(false);

		if (dimsText.getText().length() == 0)
			return;
		
		int dims = Integer.parseInt(dimsText.getText());
		if( dims < 1 || dims > 8)
			return;
		
		nDims = dims;
		
		updateWizard();
		
		setPageComplete(true);
	}

	private void updateWizard()
	{
		wizard.setNDims(nDims);	
		wizard.setClassType(classCombo.getSelectionIndex());
		wizard.setDataSetType(typeCombo.getSelectionIndex());
		
		// get the next page and tell it to get ready
		DataSetDimPage nextPage = (DataSetDimPage) getNextPage();
		nextPage.setActive();
		
	}
	
}
/*******************************************************************************
  * Copyright (c) 2009 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 *******************************************************************************/
package com.geofx.gms.editor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.geofx.gms.datasets.ClassUtil.ClassType;
import com.geofx.gms.model.ManifestEntry;

public class DataSetDialog extends Dialog
{
	Text	nameText;
	Combo	dataCombo;
	
	protected DataSetDialog(Shell shell)
	{
		super(shell);		
	}

	protected Composite createDialogArea( Composite parent )
	{
		Composite composite = (Composite) super.createDialogArea(parent);

		 //add controls to composite as necessary
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 10;
		layout.numColumns = 2;
		composite.setLayout(layout);
		
		Label label = new Label(composite, SWT.RIGHT);
		label.setText("Name:");
		
		nameText = new Text(composite, SWT.SINGLE);

		label = new Label(composite, SWT.RIGHT);
		label.setText("DataType:");
		
		dataCombo = new Combo(composite, SWT.READ_ONLY);
	
		for (ClassType current : ClassType.values())
		{
			dataCombo.add(current.toString());
		}

		dataCombo.select(0);
		
		createTable(composite);
		
		return composite;
	}
	
	private void createTable( Composite composite)
	{
		Table table = new Table(composite, SWT.FULL_SELECTION);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 20;
		gd.widthHint = 100;
		table.setLayoutData(gd);
		
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
	
		// 1st column with icon for the mimetype - NOTE: The SWT.CENTER has no effect!!
		TableColumn column = new TableColumn(table, SWT.CENTER, 0);
		column.setText("Dimension");
		column.setWidth(20);
	
		// 1st column with icon for the mimetype - NOTE: The SWT.CENTER has no effect!!
		TableColumn column2 = new TableColumn(table, SWT.CENTER, 1);
		column2.setText("Size");
		column2.setWidth(20);
		
		GridData grid = new GridData(GridData.FILL_BOTH);
		grid.horizontalAlignment = GridData.FILL;
		grid.horizontalSpan = 2;
		table.setLayoutData(grid);
	}

	
	/**
	 * @param id
	 * @param title
	 */
	class MasterContentProvider implements IStructuredContentProvider
	{
		public Object[] getElements(Object inputElement)
		{
			if (inputElement instanceof ManifestInput)
			{
				ManifestInput input = null;  // (DatasetsInput) ((GMSEditor)page.getEditor()).getDatasetsInput();
				return input.getElements(inputElement);
			}
			return new Object[0];
		}

		public void dispose()
		{
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
		{
		}
	}

	class MasterLabelProvider extends LabelProvider implements ITableLabelProvider
	{
		public String getColumnText(Object obj, int index)
		{
			ManifestEntry	 entry = (ManifestEntry)obj;
			if (index == 0)
				return entry.href;
			else if (index == 2)
				return entry.mediaType;
			else if (index == 3)
				return entry.id;
			else return "";
			
		}

		public Image getColumnImage(Object obj, int index)
		{
			if (index == 1)
			{
				ManifestEntry	 entry = (ManifestEntry)obj;
				Image img = null; //getMimeTypeImage(entry.mediaType);
				return img;
			}
		
			return null;
		}
	}
	
	//=========================== Test Main ==================================
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Display display = new Display();
		Shell shell = new Shell(display);


		DataSetDialog dialog = new DataSetDialog(shell);
		dialog.open();

		/*
		 shell.setSize(250, 250);

		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
		*/
	}
}

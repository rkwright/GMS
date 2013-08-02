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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;


/**
 * This page allows the user to edit the project info as part of the 
 * project-creation process.
 */
public class DataSetDimPage extends WizardPage
{
	private int				nDims = 11;
	private int[]			dimRay;
	private Table       	table;
	private DimInputData[] 	dimInputData;
	private TableViewer 	tableViewer;
	private Composite		composite;

	private NewDataSetWizard wizard;

	// Set column names
	private String[] columnNames = new String[] { "Dimension", "Elements" };

	
	/**
	 * Constructor for ProjectWizardPage.
	 * 
	 * @param pageName
	 */
	public DataSetDimPage()
	{
		super("DataSetDimWizardPage");		
		
		setTitle("Specify Dataset Dimensions");
		setDescription("This wizard page allows the user to set the dimensions of the dataset array");
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent)
	{
		this.wizard = (NewDataSetWizard) getWizard();

		composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		layout.numColumns = 2;

		setControl(composite);
	}


	private void createTable( Composite composite)
	{
		
		dimRay = new int[nDims];
		
		System.out.println("Creating table now");
		
	    table = new Table(composite, SWT.FULL_SELECTION|SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 200;
		gd.widthHint = 130;
		table.setLayoutData(gd);
		
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
	
		// 1st column with icon for the mimetype - NOTE: The SWT.CENTER has no effect!!
		TableColumn column1 = new TableColumn(table, SWT.CENTER, 0);
		column1.setText("Dimension");
		column1.setWidth(80);
		column1.setResizable(false);
	
		// 1st column with icon for the mimetype - NOTE: The SWT.CENTER has no effect!!
		TableColumn column2 = new TableColumn(table, SWT.CENTER, 1);
		column2.setText("Elements");
		column2.setWidth(80);
		column2.setResizable(false);
		
		GridData grid = new GridData(GridData.FILL_BOTH);
		grid.horizontalAlignment = GridData.FILL;
		grid.horizontalSpan = 2;
		table.setLayoutData(grid);
		
		createTableViewer(table);
	}

	private void createTableViewer( Table table )
	{
		createInput();
		
		tableViewer = new TableViewer(table);
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent event)
			{
				System.out.println("Selection fired");
			}
		});
		
		createCellEditor( tableViewer );
		
		tableViewer.setContentProvider(new DimContentProvider());
		tableViewer.setLabelProvider(new DimLabelProvider());
		tableViewer.setInput( dimInputData );  // new DimContentProvider());
	}


	protected void createCellEditor ( TableViewer tableViewer )
	{
		// Create the cell editors
		CellEditor[] editors = new CellEditor[Array.getLength(columnNames)];

		tableViewer.setColumnProperties(columnNames);

		// Column 0 : Dimension number
		TextCellEditor textEditor = new TextCellEditor(table);				
		editors[0] = textEditor;

		// Column 1 : Number of dimensions
		textEditor = new TextCellEditor(table);
		
		((Text)textEditor.getControl()).setTextLimit(2);
		
		((Text) textEditor.getControl()).addVerifyListener(	new VerifyListener()
		{
			public void verifyText(VerifyEvent e)
			{
				// Here, we could use a RegExp such as the following
				// if using JRE1.4 such as e.doit = e.text.matches("[\\-0-9]*");
				e.doit = "0123456789".indexOf(e.text) >= 0;
			}
		});
		
		textEditor.addPropertyChangeListener(new IPropertyChangeListener()
		{
			public void propertyChange( PropertyChangeEvent e )
			{
				System.out.println("propChange: propertyname: " + e.getProperty() + "  old: " + (String)e.getOldValue() + "  new: " + (String)e.getNewValue() );
			}
			
		});
		
		editors[1] = textEditor;

		// Assign the cell editors to the viewer
		tableViewer.setCellEditors(editors);
		// Set the cell modifier for the viewer
		tableViewer.setCellModifier(new DimCellModifier(this));	
	}
	
	private void createInput()
	{
		dimInputData = new DimInputData[nDims];
		
		for  ( int i=0; i<nDims; i++ )
		{
			dimInputData[i] = new DimInputData( i, 0);
		}
	}

	class DimLabelProvider extends LabelProvider implements ITableLabelProvider
	{
		public String getColumnText(Object obj, int index)
		{			
			DimInputData dimInput = (DimInputData)obj;

			System.out.println("getColumnText " + String.format(" index: %d", dimInput.index) + String.format(" value: %d", dimInput.value));

			if (index == 0)
				return String.format("%d", dimInput.index);
			else if (index == 1)
				return String.format("%d", dimInput.value);
			else 
				return "";
		}

		public Image getColumnImage(Object obj, int index)
		{
			return null;
		}
	}
	
	class DimInputData
	{
		public int		index;
		public int		value;
		
		public DimInputData ( int index, int value )
		{
			this.index = index;
			this.value = value;
		}
	}
	
	class DimContentProvider implements IStructuredContentProvider
	{

		public Object[] getElements(Object inputElement)
		{
			return dimInputData;
		}

		public void dispose()
		{
			System.out.println("DimContentProvider:dispose");
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
		{
			System.out.println("DimContentProvider:inputChanged");
		}
	
	}

	class DimCellModifier implements ICellModifier
	{
		DataSetDimPage	dataSetPage;
		public DimCellModifier ( DataSetDimPage dataSetDimPage )
		{
			this.dataSetPage = dataSetDimPage;
		}
		
		public boolean canModify( Object element, String columnName )
		{			
			return columnName.equals(columnNames[1]);
		}

		public Object getValue( Object element, String columnName )
		{
			DimInputData dimData = (DimInputData)element;

			System.out.println(" getValue: dimData.value = " + String.format("%d",dimData.value));

			return String.format("%d", dimData.value);
		}

		public void modify(Object element, String columnName, Object value)
		{
			TableItem item = (TableItem) element;
			DimInputData dimData = (DimInputData) item.getData();
			
			if (value == null || ((String)value).length() == 0)
				return;
			
			dimData.value = Integer.parseInt((String) value);
					
			System.out.println(" modify: dimData.value = " + String.format("%d",dimData.value));
			
			dataSetPage.isComplete();
			
			dataSetPage.tableViewer.update(dimData, null);
		}
		
	}

	public void setActive()
	{
		nDims = wizard.getNDims();

		// clear out the old one, if it exists
		if (table != null)
			table.dispose();
		
		// then create the new table and its viewer
		createTable(composite);
		
		composite.getParent().pack();
	}
	
	private void isComplete()
	{
		boolean complete = true;
		
		for ( int i=0;i<Array.getLength(dimInputData); i++ )
		{
			if (dimInputData[i].value == 0)
			{
				complete = false;
				return;
			}
		}

		updateWizard();
		
		((NewDataSetWizard)getWizard()).setCanFinish(complete);

		setPageComplete(complete);
	}
	
	private void updateWizard()
	{
		dimRay = new int[nDims];
		
		for ( int i=0; i<nDims; i++ )
		{
			dimRay[i] = dimInputData[i].value;
		}
		
		wizard.setDimRay(dimRay);
	}
}
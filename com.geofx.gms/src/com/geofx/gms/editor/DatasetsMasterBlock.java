/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.geofx.gms.editor;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.geofx.gms.model.IModelListener;
import com.geofx.gms.model.ManifestEntry;
import com.geofx.gms.plugin.GMSPlugin;
import com.geofx.gms.plugin.Strings;
import com.geofx.gms.wizards.NewDataSetWizard;

/**
 *
 */
public class DatasetsMasterBlock extends MasterDetailsBlock implements IModelListener
{
	private static final String BINARY_MIMETYPE = "application/octet-stream";

	private DatasetsPage 	page;

	protected TableViewer 	viewer;
	
	// Set the table column property names
	private final String ICON_COLUMN = "";
	private final String HREF_COLUMN = "href";
	private final String MIMETYPE_COLUMN = "mimetype";
	private final String ID_COLUMN = "id";

	// Set column names
	// private String[] columnNames = new String[] { ICON_COLUMN, HREF_COLUMN, MIMETYPE_COLUMN, ID_COLUMN };

	public DatasetsMasterBlock(DatasetsPage page)
	{
		this.page = page;
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
				ManifestInput input = (ManifestInput) ((GMSEditor)page.getEditor()).getDatasetsInput();
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
			if (index == 1)
				return entry.href;
			else if (index == 2)
				return entry.mediaType;
			else if (index == 3)
				return entry.id;
			else return "";
			
		}

		public Image getColumnImage(Object obj, int index)
		{
			if (index == 0)
			{
				ManifestEntry	 entry = (ManifestEntry)obj;
				Image img = getMimeTypeImage(entry.mediaType);
				return img;
			}
		
			return null;
		}
	}

	protected void createMasterPart(final IManagedForm managedForm, Composite parent)
	{
		// final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setText(Strings.getString("DatasetMasterBlock.sname")); //$NON-NLS-1$
		section.setDescription(Strings.getString("DatasetMasterBlock.sdesc")); //$NON-NLS-1$
		section.marginWidth = 10;
		section.marginHeight = 5;
		
		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		client.setLayout(layout);
	
		toolkit.paintBordersFor(client);

		Table table = createTable(toolkit, client);
		GridData grid = new GridData(GridData.FILL_BOTH);
		grid.horizontalAlignment = GridData.FILL;
		grid.horizontalSpan = 2;
		table.setLayoutData(grid);
		
		Button browseButton = toolkit.createButton(client, Strings.getString("DatasetMasterBlock.browse"), SWT.PUSH); //$NON-NLS-1$
		grid = new GridData();
		grid.horizontalAlignment = GridData.CENTER;
		grid.widthHint = 100;
		browseButton.setLayoutData(grid);
	
		browseButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
				getNewDatasetWizard();
			}

			public void widgetSelected(SelectionEvent e)
			{
				getNewDatasetWizard();
			}

		});

		 
		Button newButton = toolkit.createButton(client, Strings.getString("DatasetMasterBlock.new"), SWT.PUSH); //$NON-NLS-1$
		grid = new GridData();
		grid.horizontalAlignment = GridData.CENTER;
		grid.widthHint = 100;
		newButton.setLayoutData(grid);
		
	    newButton.addSelectionListener(new SelectionListener() 
	    {
			public void widgetDefaultSelected(SelectionEvent e)
			{	
				getNewDatasetWizard();
			}

			public void widgetSelected(SelectionEvent e)
			{
				getNewDatasetWizard();
			}

	    });

		section.setClient(client);
		
		final SectionPart spart = new SectionPart(section);
		managedForm.addPart(spart);
		
		createTableViewer(managedForm, table, spart);
		
		page.getGmsEditor().getProjectInfo().addModelListener(this);
	}

	private void getNewDatasetWizard()
	{
		try
		{
			StructuredSelection selection = new StructuredSelection(page.getGmsEditor().getProject());
			NewDataSetWizard wizard = new NewDataSetWizard(page.getGmsEditor());
			wizard.init(PlatformUI.getWorkbench(), selection);
			WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), wizard);
			dialog.open();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private Table createTable(FormToolkit toolkit, Composite client)
	{
		Table table = toolkit.createTable(client, SWT.FULL_SELECTION);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 20;
		gd.widthHint = 100;
		table.setLayoutData(gd);
		
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		// 1st column with icon for the mimetype - NOTE: The SWT.CENTER has no effect!!
		TableColumn column = new TableColumn(table, SWT.CENTER, 0);
		column.setText(ICON_COLUMN);
		column.setWidth(20);

		// 1st column with icon for the mimetype - NOTE: The SWT.CENTER has no effect!!
		TableColumn column2 = new TableColumn(table, SWT.CENTER, 1);
		column2.setText(HREF_COLUMN);
		column2.setWidth(120);

		// 1st column with icon for the mimetype - NOTE: The SWT.CENTER has no effect!!
		TableColumn column3 = new TableColumn(table, SWT.CENTER, 2);
		column3.setText(MIMETYPE_COLUMN);
		column3.setWidth(120);

		// 1st column with icon for the mimetype - NOTE: The SWT.CENTER has no effect!!
		TableColumn column4 = new TableColumn(table, SWT.CENTER, 3);
		column4.setText(ID_COLUMN);
		column4.setWidth(50);

		return table;
	}

	private void createTableViewer(final IManagedForm managedForm, Table table, final SectionPart spart)
	{
		viewer = new TableViewer(table);
		viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent event)
			{
				System.out.println("Selection fired");
				managedForm.fireSelectionChanged(spart, event.getSelection());
			}
		});
		
		viewer.setContentProvider(new MasterContentProvider());
		viewer.setLabelProvider(new MasterLabelProvider());
		viewer.setInput(((GMSEditor)page.getEditor()).getDatasetsInput());
	}

	
	protected void createToolBarActions(IManagedForm managedForm)
	{
		/*
		final ScrolledForm form = managedForm.getForm();
		Action haction = new Action("hor", Action.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run()
			{
				sashForm.setOrientation(SWT.HORIZONTAL);
				form.reflow(true);
			}
		};
		
		haction.setChecked(true);
		haction.setToolTipText(Messages.getString("ScrolledPropertiesBlock.horizontal")); //$NON-NLS-1$
		haction.setImageDescriptor(GMSPlugin.getDefault().getImageRegistry()
				.getDescriptor(FormArticlePlugin.IMG_HORIZONTAL));
		Action vaction = new Action("ver", Action.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run()
			{
				sashForm.setOrientation(SWT.VERTICAL);
				form.reflow(true);
			}
		};
		vaction.setChecked(false);
		vaction.setToolTipText(Messages.getString("ScrolledPropertiesBlock.vertical")); //$NON-NLS-1$
		vaction.setImageDescriptor(FormArticlePlugin.getDefault().getImageRegistry().getDescriptor(FormArticlePlugin.IMG_VERTICAL));
		form.getToolBarManager().add(haction);
		form.getToolBarManager().add(vaction);
		*/
	}
	

	protected void registerPages(DetailsPart detailsPart)
	{
		detailsPart.registerPage(ManifestEntry.class, new GzaDetailsPage());
		//detailsPart.registerPage(TypeTwo.class, new TypeTwoDetailsPage());
	}
	
	private Image getMimeTypeImage( String mimetype )
	{
		if (mimetype.compareTo(BINARY_MIMETYPE) == 0)
		{
			return GMSPlugin.getDefault().getImage(GMSPlugin.BINARY_ICON);
		}
		
		return null;
	}

	public void modelChanged(String type)
	{
		viewer.refresh();		
	}
}
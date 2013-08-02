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

package com.geofx.gms.editor;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.geofx.gms.plugin.GMSPlugin;
import com.geofx.gms.plugin.Strings;
import com.geofx.gms.viewers.ViewInfo;
import com.geofx.opengl.view.GLComposite;

/**
 *
 */
public class ViewsPage extends FormPage
{
	private static String	id = "com.geofx.gms.editor.ViewsPage";

	private CTabFolder 				tabFolder;

	private ArrayList<ViewInfo> 	viewsInfo;
	
	/**
	 * @param id
	 * @param title
	 */
	public ViewsPage(FormEditor editor)
	{
		super(editor, id, Strings.getString("ViewsPage.label")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected void createFormContent(IManagedForm managedForm)
	{
		System.out.println("ViewsPage:createForm");
		
		ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
				
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		form.getBody().setLayout(layout);
		
		tabFolder = new CTabFolder(form.getBody(), SWT.BOTTOM);
		tabFolder.setUnselectedCloseVisible(false);
		tabFolder.setSimple(false);

		tabFolder.setBackground(tabFolder.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		tabFolder.setSelectionForeground(tabFolder.getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE));
		tabFolder.setSelectionBackground(new Color[]{ tabFolder.getDisplay().getSystemColor(SWT.COLOR_WHITE),
				                                      tabFolder.getDisplay().getSystemColor(SWT.COLOR_GRAY)},
				                                      new int[] {100},true); 

		toolkit.adapt(tabFolder, true, true);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL|GridData.FILL_VERTICAL);
		gd.horizontalIndent = 0;
		gd.verticalIndent = 0;
		tabFolder.setLayoutData(gd);
		
		toolkit.paintBordersFor(tabFolder);
		
		createTabs(toolkit);
		
		//createText(toolkit, form.getBody());
		
		tabFolder.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				updateSelection();
			}
		});

		tabFolder.setBackground(tabFolder.getDisplay().getSystemColor(SWT.COLOR_GRAY));

		tabFolder.setSelection(0);
		updateSelection();
	}

	private void createTabs(FormToolkit toolkit)
	{
		if (viewsInfo == null)
			return;
		
		for (int i=0; i<viewsInfo.size(); i++ )
		{
			createTab( viewsInfo.get(i) ); //$NON-NLS-1$ //$NON-NLS-2$
		}	
		
		GMSPlugin.getEditor().viewsCreated();
	
	}
	
	public void createTab( ViewInfo viewInfo )
	{
		try
		{
			CTabItem viewItem = getTab(viewInfo.getTab());
			GLComposite glComposite = null;
			if (viewItem == null)
			{
				viewItem = new CTabItem(tabFolder, SWT.NONE);
				glComposite = new GLComposite(tabFolder);
				viewInfo.setView( glComposite.addView(viewInfo.getViewName()) );
				
				viewItem.setControl(glComposite);
				viewItem.setData(viewInfo.getTab());
				viewItem.setText(viewInfo.getLabel());	
			}
			else
			{
				glComposite = (GLComposite) viewItem.getControl();
				viewInfo.setView( glComposite.addView(viewInfo.getViewName()) );
			}

			viewInfo.setComposite( glComposite );
			
			// set some typical values as defaults
			glComposite.getGrip().setOffsets(0, 0, -2.5f);
			glComposite.getGrip().setRotation(0, 0, 0);
	
		}
		catch(Exception e )
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * See if the named tab has already been created
	 * 
	 * @param tab
	 * @return
	 */
	protected CTabItem getTab (String tab)
	{
		CTabItem[] 	tabs = tabFolder.getItems();
		
		for ( int i=0; i<tabs.length; i++)
		{
			if (tab.equals(tabs[i].getData()))
					return tabs[i];
		}
		
		return null;
	}
	
	public void setActive(boolean active)
	{
		System.out.println("ViewsPage:setActive: " + active);
	}

	public void addViews( ArrayList<ViewInfo> viewsInfo )
	{
		this.viewsInfo = viewsInfo;		
	}
	
	private void updateSelection()
	{
		CTabItem item = tabFolder.getSelection();
		CTabItem[] tabs = tabFolder.getItems();

		for ( int i=0; i<tabs.length; i++)
		{
			if (i != tabFolder.indexOf(item))
			{
				System.out.println("ViewsPage:updateSelection");
			}
		}
	}

}
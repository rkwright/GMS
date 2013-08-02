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

import java.io.ByteArrayInputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;

import com.geofx.xmleditor.XMLEditor;

public class GMSXMLEditor extends XMLEditor implements IFormPage,IDocumentListener
{
	private GMSEditor 		gmsEditor;
	private int 			index;
	private static String	id = "com.geofx.gms.GMSXMLEditor";
	private boolean			dirty = false;
	//private boolean			stale = false;


	public boolean canLeaveThePage()
	{
		//System.out.println("GMSXMLEditor:canLeavePage");
		
		updateModel();
		
		return true;
	}

	public void updateModel()
	{
		if (dirty)
		{
			gmsEditor.setDirty(true);
			ByteArrayInputStream stream = new ByteArrayInputStream(getInputDocument().get().getBytes());	
			gmsEditor.getProjectInfo().parse(stream);
			gmsEditor.initViews();
			dirty = false;
		}
	}

	public FormEditor getEditor()
	{
		//System.out.println("GMSXMLEditor:getEditor");
		return gmsEditor;
	}

	public String getId()
	{
		//System.out.println("GMSXMLEditor:getID");
		return id;
	}


	public void setIndex(int index)
	{
		//System.out.println("GMSXMLEditor:setIndex = " + index);
		this.index = index;
	}

	public int getIndex()
	{
		//System.out.println("GMSXMLEditor:getIndex");
		return index;
	}

	public IManagedForm getManagedForm()
	{
		//System.out.println("GMSXMLEditor:getManagedForm");
		return null;
	}

	public Control getPartControl()
	{		
		// System.out.println("GMSXMLEditor:getPartControl");
		return control;
	}

	public void initialize(FormEditor editor)
	{
		//System.out.println("GMSXMLEditor:initialize");
		gmsEditor = (GMSEditor) editor;
	}

	public boolean isActive()
	{
		//System.out.println("GMSXMLEditor:isActive");
		return true;
	}

	public boolean isEditor()
	{
		//System.out.println("GMSXMLEditor:isEditor");
		return true;
	}

	public boolean selectReveal(Object object)
	{
		//System.out.println("GMSXMLEditor:selectReveal: " + object );
		return false;
	}

	@Override
	public void doSave(IProgressMonitor progressMonitor)
	{
		/*
		System.out.println("GMSXMLEditor:doSave");
		super.doSave(progressMonitor);
		soiled = false;
		*/
		
		canLeaveThePage();
		gmsEditor.doSave(progressMonitor);
		dirty = false;
	}

	public void setActive(boolean active)
	{
		if (gmsEditor.isDirty())
			reloadModel();

		dirty = false;
		
		getInputDocument().addDocumentListener(this);
		
		//System.out.println("GMSXMLEditor:setActive = " + active);
	}

	public void reloadModel()
	{
		getInputDocument().set(gmsEditor.serializeToTemp());
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event)
	{
		System.out.println("GMSXMLEdditor - Document about to be changed!");
		
	}

	@Override
	public void documentChanged(DocumentEvent event)
	{
		System.out.println("GMSXMLEdditor - Document changed!");
		gmsEditor.setDirty(true);
		dirty = true;	
		
		if (validateAndMark())
		{
			System.out.println("XML was successfully edited");
			updateModel();
		}
	}

	/*
	public boolean isStale()
	{
		return stale;
	}

	public void setStale(boolean stale)
	{
		this.stale = stale;
	}
	*/
}

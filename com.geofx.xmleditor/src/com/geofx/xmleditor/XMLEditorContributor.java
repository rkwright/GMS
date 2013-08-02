/*******************************************************************************
 * Copyright (c) 2008 Phil Zoio and Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * This code is in part derived from Eclipse wizard code, but also originally 
 * written by Phil Zoio as detailed in the article:
 * http://www.realsolve.co.uk/site/tech/jface-text.php
 * 
 * Contributors:
 * 	   Phil Zoio - 2006 - Original implementation
 *     Ric Wright - 2008-2009 - Bug fixes and tweaks
 *     
 ********************************************************************************/

package com.geofx.xmleditor;

import java.util.ResourceBundle;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

import com.geofx.xmleditor.Activator;


/**
 * Manages the installation and deinstallation of actions for the editor.
 */
public class XMLEditorContributor extends BasicTextEditorActionContributor
{

	protected RetargetTextEditorAction contentAssistProposal;
	protected RetargetTextEditorAction contentAssistTip;
	protected RetargetTextEditorAction formatProposal;

	/**
	 * Constructor for SQLEditorContributor. Creates a new contributor in the
	 * form of adding Content Assist, Conent Format and Assist tip menu items
	 */
	public XMLEditorContributor()
	{
		super();
		ResourceBundle bundle = Activator.getDefault().getResourceBundle();

		contentAssistProposal = new RetargetTextEditorAction(bundle, "ContentAssistProposal.");
		formatProposal = new RetargetTextEditorAction(bundle, "ContentFormatProposal.");
		contentAssistTip = new RetargetTextEditorAction(bundle, "ContentAssistTip.");

	}

	public void contributeToMenu(IMenuManager mm)
	{
		super.contributeToMenu(mm);
		IMenuManager editMenu = mm.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null)
		{
			editMenu.add(new Separator());
			editMenu.add(contentAssistProposal);
			editMenu.add(formatProposal);
			editMenu.add(contentAssistTip);
		}
	}

	/**
	 * Sets the active editor to this contributor. This updates the actions to
	 * reflect the editor.
	 * 
	 * @see EditorActionBarContributor#editorChanged
	 */
	public void setActiveEditor(IEditorPart part)
	{

		super.setActiveEditor(part);

		ITextEditor editor = null;
		if (part instanceof ITextEditor)
			editor = (ITextEditor) part;

		contentAssistProposal.setAction(getAction(editor, "ContentAssistProposal"));
		formatProposal.setAction(getAction(editor, "ContentFormatProposal"));
		contentAssistTip.setAction(getAction(editor, "ContentAssistTip"));

	}

	/**
	 * 
	 * Contributes to the toolbar.
	 * 
	 * @see EditorActionBarContributor#contributeToToolBar
	 */
	public void contributeToToolBar(IToolBarManager tbm)
	{
		super.contributeToToolBar(tbm);
		tbm.add(new Separator());
	}

}
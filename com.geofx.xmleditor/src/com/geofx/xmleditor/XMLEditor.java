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

import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.xml.sax.helpers.LocatorImpl;

import com.geofx.xmleditor.Activator;
import com.geofx.xmleditor.markers.MarkingErrorHandler;
import com.geofx.xmleditor.markers.IErrorListProvider;
import com.geofx.xmleditor.outline.EditorContentOutlinePage;
import com.geofx.xmleditor.xml.XMLParser;
import com.geofx.xmleditor.xml.XMLValidationError;

public class XMLEditor extends TextEditor implements IErrorListProvider
{
	private ColorManager 				colorManager;
	private IEditorInput 				input;
	private EditorContentOutlinePage 	outlinePage;
	protected Control 					control;
	protected MarkingErrorHandler 		markingErrorHandler;

	public XMLEditor()
	{
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new XMLConfiguration(this, colorManager));
		setDocumentProvider(new XMLDocumentProvider());
	}

	public void createPartControl(Composite parent )
	{
		super.createPartControl(parent);
		Control[] children = parent.getChildren();
		control = children[children.length - 1];
	}
	
	public void dispose()
	{
		colorManager.dispose();
		if (outlinePage != null)
			outlinePage.setInput(null);
		super.dispose();
	}

	@Override
	public void doSave(IProgressMonitor progressMonitor)
	{
		super.doSave(progressMonitor);
	}
	
	protected void doSetInput(IEditorInput newInput) throws CoreException
	{
		super.doSetInput(newInput);
		this.input = newInput;

		if (outlinePage != null)
			outlinePage.setInput(input);
		
		validateAndMark();
	}

	protected void editorSaved()
	{
		super.editorSaved();

		System.out.println("XMLEditor:Editor saved called");
		
		if (outlinePage != null)
			outlinePage.update();	
	
		// we validate and mark document here
		validateAndMark();

	}

	protected boolean validateAndMark()
	{
		boolean parseError = true;
		try
		{
			IDocument document = getInputDocument();
			String text = document.get();
			markingErrorHandler = new MarkingErrorHandler(getInputFile(), document);
			markingErrorHandler.setDocumentLocator(new LocatorImpl());
			markingErrorHandler.removeExistingMarkers();
			
			XMLParser parser = new XMLParser();
			parser.setErrorHandler(markingErrorHandler);
			parser.doParse(text);
			parseError = markingErrorHandler.wasFatalError();		
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return parseError == false;
	}

	protected IDocument getInputDocument()
	{
		IDocument document = getDocumentProvider().getDocument(input);
		return document;
	}

	protected IFile getInputFile()
	{
		IFileEditorInput ife = (IFileEditorInput) input;
		IFile file = ife.getFile();
		return file;
	}
	
	
	public IEditorInput getInput()
	{
		return input;
	}
	
	
	/**
	 * Needed for content assistant
	 */
	protected void createActions()
	{
		super.createActions();
		
		ResourceBundle bundle = Activator.getDefault().getResourceBundle();
		
		setAction("ContentFormatProposal", new TextOperationAction(bundle, "ContentFormatProposal.", this, ISourceViewer.FORMAT));
		setAction("ContentAssistProposal", new TextOperationAction(bundle, "ContentAssistProposal.", this, ISourceViewer.CONTENTASSIST_PROPOSALS));
		setAction("ContentAssistTip", new TextOperationAction(bundle, "ContentAssistTip.", this, ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION));
	}
	
	
	public Object getAdapter(Class required)
	{
		// System.out.println("XMLEditor:getAdapter: " + required.getName());
		if (IContentOutlinePage.class.equals(required))
		{
			if (outlinePage == null)
			{
				outlinePage = new EditorContentOutlinePage(this);
				if (getEditorInput() != null)
					outlinePage.setInput(getEditorInput());
			}
			return outlinePage;
		}

		return super.getAdapter(required);
		
	}

	public List<XMLValidationError> getErrorList()
	{
		return markingErrorHandler.getErrorList();
	}
}
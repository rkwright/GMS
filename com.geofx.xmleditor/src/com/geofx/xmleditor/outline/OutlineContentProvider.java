/*******************************************************************************
 * Copyright (c) 2008-09 Phil Zoio and Ric Wright 
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
 * 	   Phil Zoio - 2004 - Original implementation
 *     Ric Wright - 2008-2009 - Bug fixes and tweaks
 *     
 ********************************************************************************/

package com.geofx.xmleditor.outline;

import java.util.List;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.xml.sax.helpers.LocatorImpl;

import com.geofx.xmleditor.xml.XMLElement;
import com.geofx.xmleditor.xml.XMLParser;



/**
 * @author Phil Zoio
 */
public class OutlineContentProvider implements ITreeContentProvider
{

	private XMLElement root = null;
	private IEditorInput input;
	private IDocumentProvider documentProvider;

	protected final static String TAG_POSITIONS = "__tag_positions";
	protected IPositionUpdater positionUpdater = new DefaultPositionUpdater(TAG_POSITIONS);

	public OutlineContentProvider(IDocumentProvider provider)
	{
		super();
		this.documentProvider = provider;
	}

	public Object[] getChildren(Object parentElement)
	{
		if (parentElement == input)
		{
			if (root == null)
				return new Object[0];
			List childrenDTDElements = root.getChildrenDTDElements();
			if (childrenDTDElements != null)
				return childrenDTDElements.toArray();
		}
		else
		{
			XMLElement parent = (XMLElement)parentElement;
			List childrenDTDElements = parent.getChildrenDTDElements();
			if (childrenDTDElements != null)
				return childrenDTDElements.toArray();
		}
		return new Object[0];
	}

	public Object getParent(Object element)
	{
		if (element instanceof XMLElement)
			return ((XMLElement)element).getParent();
		return null;
	}

	public boolean hasChildren(Object element)
	{
		if (element == input) return true;
		else
		{
			return ((XMLElement)element).getChildrenDTDElements().size() > 0;
		}
	}

	public Object[] getElements(Object inputElement)
	{
		if (root == null)
			return new Object[0];
		List childrenDTDElements = root.getChildrenDTDElements();
		if (childrenDTDElements != null)
			return childrenDTDElements.toArray();
		return new Object[0];
	}

	public void dispose()
	{
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{

		if (oldInput != null)
		{
			IDocument document = documentProvider.getDocument(oldInput);
			if (document != null)
			{
				try
				{
					document.removePositionCategory(TAG_POSITIONS);
				}
				catch (BadPositionCategoryException x)
				{
				}
				document.removePositionUpdater(positionUpdater);
			}
		}
		
		input = (IEditorInput) newInput;

		if (newInput != null)
		{
			IDocument document = documentProvider.getDocument(newInput);
			if (document != null)
			{
				document.addPositionCategory(TAG_POSITIONS);
				document.addPositionUpdater(positionUpdater);

				XMLElement rootElement = parseRootElement(document);
				if (rootElement != null)
				{
					root = rootElement;
				}
			}
		}
	}

	private XMLElement parseRootElement(IDocument document)
	{
		String text = document.get();
		XMLElement tagPositions = parseRootElements(text, document);
		return tagPositions;
	}

	private XMLElement parseRootElements(String text, IDocument document)
	{
		try
		{
			XMLParser xmlParser = new XMLParser();
			OutlineContentHandler contentHandler = new OutlineContentHandler();
			contentHandler.setDocument(document);
			contentHandler.setPositionCategory(TAG_POSITIONS);
			contentHandler.setDocumentLocator(new LocatorImpl());
			xmlParser.setContentHandler(contentHandler);
			xmlParser.doParse(text);
			XMLElement root = contentHandler.getRootElement();
			return root;
		}
		catch (Exception e)
		{
			return null;
		}
	}

}
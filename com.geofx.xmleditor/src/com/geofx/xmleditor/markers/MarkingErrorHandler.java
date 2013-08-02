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

package com.geofx.xmleditor.markers;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.xml.sax.SAXParseException;

import com.geofx.xmleditor.xml.XMLValidationError;
import com.geofx.xmleditor.xml.XMLValidationErrorHandler;



/**
 * 
 */
public class MarkingErrorHandler extends XMLValidationErrorHandler
{

	public static final String ERROR_MARKER_ID = "com.geofx.xmleditor.dtderror";

	private IFile 		file;
	private IDocument 	document;

	public MarkingErrorHandler( IFile file, IDocument document )
	{
		super();
		this.file = file;
		this.document = document;
	}

	public void removeExistingMarkers()
	{
		try
		{
			file.deleteMarkers(ERROR_MARKER_ID, true, IResource.DEPTH_ZERO);
		}
		catch (CoreException e1)
		{
			e1.printStackTrace();
		}
	}

	protected XMLValidationError nextError(SAXParseException e, boolean isFatal)
	{

		XMLValidationError validationError = super.nextError(e, isFatal);

		HashMap<String, Comparable<?>> map = new HashMap<String, Comparable<?>>();
		
		int lineNumber = e.getLineNumber();
		int columnNumber = e.getColumnNumber();
		MarkerUtilities.setLineNumber(map, lineNumber);
		MarkerUtilities.setMessage(map, e.getMessage());
		map.put(IMarker.LOCATION, file.getFullPath().toString());

		Integer charStart = getCharStart(lineNumber, columnNumber);
		if (charStart != null)
		{
			map.put(IMarker.CHAR_START, charStart);
			validationError.setLineNumber(getLineNumberForOffset(charStart));
		}

		Integer charEnd = getCharEnd(lineNumber, columnNumber);
		if (charEnd != null)
			map.put(IMarker.CHAR_END, charEnd);

		map.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_ERROR));

		System.out.println("Creating marker, msg: " + e.getMessage());
		
		try
		{
			MarkerUtilities.createMarker(file, map, ERROR_MARKER_ID);
		}
		catch (CoreException ee)
		{
			ee.printStackTrace();
		}

		return validationError;

	}

	private Integer getCharEnd(int lineNumber, int columnNumber)
	{
		try
		{
			return new Integer(document.getLineOffset(lineNumber - 1) + columnNumber);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private Integer getCharStart(int lineNumber, int columnNumber)
	{
		try
		{
			int lineStartChar = document.getLineOffset(lineNumber - 1);
			Integer charEnd = getCharEnd(lineNumber, columnNumber);
			if (charEnd != null)
			{
				ITypedRegion typedRegion = document.getPartition(charEnd.intValue()-2);
				int partitionStartChar = typedRegion.getOffset();
				return new Integer(partitionStartChar);
			}
			else
				return new Integer(lineStartChar);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private Integer getLineNumberForOffset( int charOffset)
	{
		try
		{
			return 	document.getLineOfOffset(charOffset) + 1;
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
			return null;
		}
	}

}
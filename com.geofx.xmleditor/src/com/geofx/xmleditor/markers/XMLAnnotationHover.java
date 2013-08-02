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

package com.geofx.xmleditor.markers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;

import com.geofx.xmleditor.xml.XMLValidationError;

/**
 * @author riwright
 *
 */
public class XMLAnnotationHover implements IAnnotationHover
{

	private IErrorListProvider errorListProvider;

	public XMLAnnotationHover(  IErrorListProvider errorListProvider )
	{
		this.errorListProvider = errorListProvider;	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.IAnnotationHover#getHoverInfo(org.eclipse.jface.text.source.ISourceViewer, int)
	 */
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber)
	{
		ArrayList<String> annots = getAnnotations(sourceViewer, lineNumber);
		String 	annotString = new String();
		
		for ( int i=0; i<annots.size(); i++ )
		{
			if (annotString.length() > 0)
				annotString += "\n";
			
			annotString += annots.get(i);
		}
		
		return annotString;
	}

	private ArrayList<String> getAnnotations(ISourceViewer viewer, int lineNumber) 
	{
		List<XMLValidationError> 	errorList = errorListProvider.getErrorList();
		ArrayList<String> 			annots = new ArrayList<String>();

		for ( int i=0;i<errorList.size(); i++ )
		{
			XMLValidationError error = errorList.get(i);
			if (error.getLineNumber() == (lineNumber+1))
			{
				annots.add(error.getErrorMessage());
			}
		}
	
		return annots;
	}

	/*
	private ArrayList<String> getAnnotationsAtLine(IAnnotationModel model, IDocument document, int lineNumber)
	{
		ArrayList<String> annots = new ArrayList<String>();
		
		Iterator iter = model.getAnnotationIterator();
		
		while (iter.hasNext())
		{
			Annotation annot = (Annotation) iter.next();
			annots.add(annot.getText());
		}
		return annots;
	}
	*/
}

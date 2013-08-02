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

package com.geofx.xmleditor.xml;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @
 */
public class XMLValidationErrorHandler extends DefaultHandler
{

	private List<XMLValidationError> 	errorList = new ArrayList<XMLValidationError>();
	private Locator 					locator;
	private boolean 					fatal = false;

	public XMLValidationErrorHandler()
	{
	}

	public void error(SAXParseException e) throws SAXException
	{

		handleError(e, false);

	}
	
	
	public void setDocumentLocator(Locator locator)
	{
		this.locator = locator;
	}
	
	
	private void handleError(SAXParseException e, boolean isFatal)
	{
		XMLValidationError validationError = nextError(e, isFatal);
		errorList.add(validationError);
		System.out.println(validationError.toString());

	}

	protected XMLValidationError nextError(SAXParseException e, boolean isFatal)
	{
		String errorMessage = e.getMessage();

		int lineNumber = e.getLineNumber();
		int columnNumber = e.getColumnNumber();

		log(this, (isFatal ? "FATAL " : "Non-Fatal") + "Error on line " + lineNumber + ", column " + columnNumber
				+ ": " + errorMessage);

		XMLValidationError validationError = new XMLValidationError();
		validationError.setLineNumber(lineNumber);
		validationError.setColumnNumber(columnNumber);
		validationError.setErrorMessage(errorMessage);
		return validationError;
	}

	private void log(XMLValidationErrorHandler handler, String string)
	{
	}

	public void fatalError(SAXParseException e) throws SAXException
	{
		handleError(e, true);
		fatal = true;
	}

	public List<XMLValidationError> getErrorList()
	{
		return errorList;
	}

	public boolean wasFatalError()
	{
		return fatal;
	}

}


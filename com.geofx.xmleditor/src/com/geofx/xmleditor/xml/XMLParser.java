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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Performs DTD validation on supplied XML document
 */
public class XMLParser
{

	private ErrorHandler 	errorHandler;
	private ContentHandler 	contentHandler;

	public void setErrorHandler(ErrorHandler errorHandler)
	{
		this.errorHandler = errorHandler;
	}

	public void setContentHandler(ContentHandler contentHandler)
	{
		this.contentHandler = contentHandler;
	}

	public static void main(String[] args)
	{
		try
		{
			XMLParser parser = new XMLParser();
			parser.setErrorHandler(new XMLValidationErrorHandler());
			parser.doParse(new File(args[0]));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static final String VALIDATION_FEATURE = "http://xml.org/sax/features/validation";

	/**
	 * Does DTD-based validation on File
	 */
	public void doParse(File xmlFilePath) throws RuntimeException
	{

		InputSource inputSource = null;
		try
		{
			inputSource = new InputSource(new FileReader(xmlFilePath));
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		doParse(inputSource);

	}

	/**
	 * Does DTD-based validation on text
	 */
	public void doParse(String xmlText) throws RuntimeException
	{

		InputSource inputSource = new InputSource(new StringReader(xmlText));
		doParse(inputSource);

	}

	/**
	 * Does DTD-based validation on inputSource
	 */
	public void doParse(InputSource inputSource) throws RuntimeException
	{

		try
		{
			XMLReader reader = new SAXParser();
			reader.setErrorHandler(errorHandler);
			reader.setContentHandler(contentHandler);
			reader.setFeature(VALIDATION_FEATURE, true);
			reader.parse(inputSource);
		}
		catch (org.xml.sax.SAXParseException e)
		{
			// e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}


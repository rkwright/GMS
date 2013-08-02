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


/**
 * 
 * 
 */
public class XMLValidationError
	{
	    private String 	errorMessage;
	    private int 	lineNumber;
	    private int 	columnNumber;

	    public String getErrorMessage()
	    {
	        return errorMessage;
	    }

	    public void setErrorMessage(String errorMessage)
	    {
	        this.errorMessage = errorMessage;
	    }

	    public int getLineNumber()
	    {
	        return lineNumber;
	    }

	    public void setLineNumber(int lineNumber)
	    {
	        this.lineNumber = lineNumber;
	    }

	    public int getColumnNumber()
	    {
	        return columnNumber;
	    }

	    public void setColumnNumber(int columnNumber)
	    {
	        this.columnNumber = columnNumber;
	    }

	    public String toString()
	    {
	        StringBuffer buf = new StringBuffer();
	        buf.append("Error on ")
	            .append(" line ")
	            .append(lineNumber)
	            .append(", column ")
	            .append(columnNumber)
	            .append(": ")
	            .append(errorMessage);
	        return buf.toString();
	    }
	}


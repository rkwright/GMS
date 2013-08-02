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

package com.geofx.xmleditor.format;

import org.eclipse.jface.text.formatter.IFormattingStrategy;


/**
 * 
 */
public class DefaultFormattingStrategy implements IFormattingStrategy
{
	protected static final String lineSeparator = System.getProperty("line.separator");

	public DefaultFormattingStrategy()
	{
		super();
	}

	public void formatterStarts(String initialIndentation)
	{
	}

	public String format(String content, boolean isLineStart, String indentation, int[] positions)
	{
		return "";
	}

	public void formatterStops()
	{
	}

}

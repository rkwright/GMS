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

import org.eclipse.swt.graphics.RGB;

public interface IXMLColorConstants
{

	RGB XML_COMMENT = new RGB(128, 0, 0);
	RGB PROC_INSTR = new RGB(200, 20, 200);
	RGB DOCTYPE = new RGB(0, 150, 150);
	RGB STRING = new RGB(0, 128, 0);
	RGB DEFAULT = new RGB(0, 0, 0);
	RGB TAG = new RGB(0, 0, 128);

	//enhancements
	RGB ESCAPED_CHAR = new RGB(128, 128, 0);
	RGB CDATA = new RGB(0, 128, 128);
	RGB CDATA_TEXT = new RGB(255, 0, 0);
}
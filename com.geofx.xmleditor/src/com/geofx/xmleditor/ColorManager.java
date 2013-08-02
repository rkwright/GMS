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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorManager
{

	protected Map<RGB, Color> fColorTable = new HashMap<RGB, Color>(10);

	public void dispose()
	{
		Iterator e = fColorTable.values().iterator();
		while (e.hasNext())
			((Color) e.next()).dispose();
	}

	public Color getColor(RGB rgb)
	{
		Color color = (Color) fColorTable.get(rgb);
		if (color == null)
		{
			color = new Color(Display.getCurrent(), rgb);
			fColorTable.put(rgb, color);
		}
		return color;
	}
}

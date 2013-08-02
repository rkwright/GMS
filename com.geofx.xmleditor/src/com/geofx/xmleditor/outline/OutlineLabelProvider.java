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

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import com.geofx.xmleditor.xml.XMLElement;



/**
 * 
 */
public class OutlineLabelProvider implements ILabelProvider
{

	public OutlineLabelProvider()
	{
		super();
	}

	public Image getImage(Object element)
	{
		return null;
	}

	public String getText(Object element)
	{
		if (element instanceof XMLElement)
		{
			XMLElement dtdElement = (XMLElement) element;
			String textToShow = dtdElement.getName();

			String nameAttribute = dtdElement.getAttributeValue("name");
			if (nameAttribute != null)
				textToShow += " " + nameAttribute;

			return textToShow;
		}
		return null;
	}

	public void addListener(ILabelProviderListener listener)
	{
	}

	public void dispose()
	{
	}

	public boolean isLabelProperty(Object element, String property)
	{
		return false;
	}

	public void removeListener(ILabelProviderListener listener)
	{
	}

}
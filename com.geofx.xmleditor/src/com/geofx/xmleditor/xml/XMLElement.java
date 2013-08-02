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
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.Position;

/**
 * @author Phil Zoio
 */
public class XMLElement
{

	private List<XMLElement> elementChildren = new ArrayList<XMLElement>();
	private List<XMLAttribute> attributeChildren = new ArrayList<XMLAttribute>();

	private String name;
	private XMLElement parent;
	private Position position;

	public XMLElement(String name)
	{
		super();
		this.name = name;
	}

	public List getChildrenDTDElements()
	{
		return elementChildren;
	}

	public XMLElement addChildElement(XMLElement element)
	{
		elementChildren.add(element);
		element.setParent(this);
		return this;
	}

	public void setParent(XMLElement element)
	{
		this.parent = element;
	}

	public XMLElement getParent()
	{
		return parent;
	}

	public XMLElement addChildAttribute(XMLAttribute attribute)
	{
		attributeChildren.add(attribute);
		return this;
	}

	public String getName()
	{
		return name;
	}
	
	public String getAttributeValue(String localName)
	{
		for (Iterator iter = attributeChildren.iterator(); iter.hasNext();)
		{
			XMLAttribute attribute = (XMLAttribute) iter.next();
			if (attribute.getName().equals(localName)) return attribute.getValue();
		}
		return null;
	}

	public void clear()
	{
		elementChildren.clear();
		attributeChildren.clear();
	}

	public void setPosition(Position position)
	{
		this.position = position;
	}

	public Position getPosition()
	{
		return position;
	}
}
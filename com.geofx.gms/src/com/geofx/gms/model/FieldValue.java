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

package com.geofx.gms.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xml.sax.Attributes;

import com.geofx.gms.datasets.ClassUtil;
import com.geofx.gms.plugin.Constants;

public class FieldValue
{
	protected static final List<String> primitives =  Arrays.asList( "boolean", "byte", "char", "short", "int", "long", "float", "double", "string", "object" );

	protected String field;
	protected String type;
	protected ArrayList<String> values = new ArrayList<String>();

	public FieldValue( Attributes attributes, ArrayList<String> itemValues )
	{
		this.field = attributes.getValue("", Constants.FIELD);
		this.type  = attributes.getValue("", Constants.TYPE);
		addValues( attributes, itemValues );
		
		normalizeType();
	}

	public void addValues( Attributes attributes, ArrayList<String> itemValues )
	{
		String value = attributes.getValue("", Constants.VALUE);
		if (value != null)
			values.add(value);
		else
			values.addAll(itemValues);
	}
	
	protected void normalizeType()
	{
		int index = primitives.indexOf(type);
		if (index != -1)
			type = ClassUtil.ClassType.convert(index).toString();
	}

}

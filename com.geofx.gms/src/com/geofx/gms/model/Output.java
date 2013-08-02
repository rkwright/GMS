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

import org.xml.sax.Attributes;

import com.geofx.gms.datasets.Dataset;
import com.geofx.gms.plugin.Constants;

public class Output
{
	protected String 	field;
	protected String 	type;
	protected String 	id;
	protected Dataset	dataset;		

	public Output ()
	{	
	}
	
	public Output( Attributes attributes )
	{
		this.field = attributes.getValue("", Constants.FIELD);
		this.type = attributes.getValue("", Constants.TYPE);
		this.id = attributes.getValue("", Constants.ID);
	}

	public String getType()
	{
		return type;
	}

	public String getId()
	{
		return id;
	}

	public void setDataset( Dataset dataset )
	{
		this.dataset = dataset;
	}
}

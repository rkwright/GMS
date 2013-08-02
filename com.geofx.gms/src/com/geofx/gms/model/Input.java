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

public class Input
{
	protected String 	field;
	protected String 	type;
	protected String 	href;
	protected String 	idref;
	protected String 	iterator;
	protected Dataset	dataset;
	protected Module	module;
	protected String 	filePath;
	
	public Input( Attributes attributes )
	{
		this.field = attributes.getValue("", Constants.FIELD);
		this.type = attributes.getValue("", Constants.TYPE);
		this.href = attributes.getValue("", Constants.HREF);
		this.idref = attributes.getValue("", Constants.IDREF);
		this.iterator = attributes.getValue("", Constants.ITERATOR);
	}

	public void setDataset(Dataset dataset)
	{
		this.dataset = dataset;
	}

	public void setModule(Module module)
	{
		this.module = module;
	}

	public String getType()
	{
		return type;
	}

	public String getHRef()
	{
		return href;
	}

	public String getFilePath()
	{
		return filePath;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}

	public String getIDRef()
	{
		return idref;
	}

	public Module getModule()
	{
		return module;
	}

	public Dataset getDataset()
	{
		return dataset;
	}

	public String getIterator()
	{
		return iterator;
	}

	public void setIterator(String iterator)
	{
		this.iterator = iterator;
	}
}

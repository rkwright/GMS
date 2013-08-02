/******************************************************************************
 * Copyright (c) 2008-9 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 *******************************************************************************/

package com.geofx.gms.datasets;

import java.util.UUID;

import com.geofx.gms.datasets.ClassUtil.ClassType;

/**
 *  Abstract class DataSet, which is the basis for all the datasets.
 *
 */
public /*abstract*/ class Dataset
{
	public enum DatasetType 
	{ 
		Scalar, Grid, TIN, GML, RollingGrid, UserDefined;
		
	public static DatasetType convert(int i)
	{
		for (DatasetType current : DatasetType.values())
		{
			if (current.ordinal() == i)
			{
				return current;
			}
		}
		
		return null;
	}
};

	protected String		id = UUID.randomUUID().toString();
	protected DatasetType	datasetType = DatasetType.Grid;

	protected ClassType		classType;
	protected String		objectName;

	// time fields, all ms since 1 Jan 1970
	protected double		created;
	protected double		lastAccessed;
	protected double		lastModified;

	protected Object		object;

	public Dataset()
	{	
	}
		
	public Dataset( DatasetType type )
	{
		this.datasetType = type;
		created = lastModified = System.currentTimeMillis();
	}
	
	/**
	 * Performs a deep copy of this object
	 * 
	 * @return
	 */
	public void copy ( Dataset dataset )
	{		
		this.id = new String(dataset.id);
		this.datasetType = dataset.datasetType;
		this.classType = dataset.classType;
		this.objectName = dataset.objectName == null ? null : new String(dataset.objectName);
		this.created = dataset.created;
		this.lastAccessed = dataset.lastAccessed;
		this.lastModified = dataset.lastModified;
	}

	/**
	 * JUst a method to allow subclasses to reset themselves, if appropriate and needed
	 */
	protected void reset()
	{
		
	}
	public String getID()
	{
		return id;
	}

	public DatasetType getType()
	{
		return datasetType;
	}

	public void setType(DatasetType type)
	{
		this.datasetType = type;
	}

	public DatasetType getDataSetType()
	{
		return datasetType;
	}

	public void setDataSetType(DatasetType dataSetType)
	{
		this.datasetType = dataSetType;
	}

	public double getCreated()
	{
		return created;
	}

	public void setCreated(double created)
	{
		this.created = created;
	}

	public double getLastAccessed()
	{
		return lastAccessed;
	}

	public void setLastAccessed(double lastAccessed)
	{
		this.lastAccessed = lastAccessed;
	}

	public double getLastModified()
	{
		return lastModified;
	}

	public void setLastModified(double lastModified)
	{
		this.lastModified = lastModified;
	}

	public void setId(String id)
	{
		this.id = id;
	}
	
	/*
	public int[] getDims()
	{
		return dims;
	}

	public void setDims(int[] dims)
	{
		this.dims = dims;
	}
	*/
	
	public Object getObject()
	{
		return object;
	}

	public void setObject(Object object)
	{
		this.object = object;
	}
}

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

package com.geofx.gms.datasets;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.vecmath.Matrix4d;

import com.geofx.gms.datasets.ClassUtil.ClassType;


/**
 * @author riwright
 *
 */
public class RollingGrid extends Dataset
{

	protected ClassType				classType;
	protected String				objectName;
	protected ArrayList<Object>		array = new ArrayList<Object>();
	protected Matrix4d				ctm = new Matrix4d();
	protected int					size;

	/**
	 * @param type
	 */
	public RollingGrid()
	{
		super(DatasetType.RollingGrid);
	}

	/**
	 * Append a single object on the grid. Trim the first 
	 * object off of the list if it exceeds the specified size
	 * of the grid
	 *  
	 * @param object
	 */
	public void add( Dataset dataset )
	{
		Class params[] = { dataset.getClass() };
		Object paramsObj[] = { dataset };
		Dataset newDataset = null;
		try
		{
			// if now too big, remove first element
			if (array.size() >= size)
				newDataset = (Dataset) array.remove(0);
			else
				// create the new object.  Use newInstance so it creates the superclass, not a Dataset object
				newDataset = dataset.getClass().newInstance();

			// have to use reflection here, otherwise we end up calling the base class copy method
			
			// get the method
			Method method = newDataset.getClass().getDeclaredMethod("copy", params);
			// call the method
			method.invoke(newDataset, paramsObj);

			// add the new object
			array.add(newDataset);
			
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}

	}
	
	protected void reset()
	{
		array.clear();
	}
	
	public Matrix4d getCtm()
	{
		return ctm;
	}

	public void setCtm(Matrix4d ctm)
	{
		this.ctm = ctm;
	}

	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}
	
	public ArrayList<Object> getArray()
	{
		return array;
	}
}

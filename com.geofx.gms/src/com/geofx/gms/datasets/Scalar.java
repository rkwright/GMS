/*******************************************************************************
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

import com.geofx.gms.datasets.ClassUtil.ClassType;

/**
 *  Class which implements support for i.e. those data sets that consist
 *  of rectangular grids.  Can be of any dimension, though NxM (two-dimensional) is most
 *  common. In contrast to UniformGrids, a grid need not be uniform (and usually isn't).  
 *  In consequence, the grid's objects either are non-spatial (depending on an associated 
 *  UniformGrid for location information, or the objects themselves contain the coordinate info.
 */
public class Scalar extends Dataset
{
	protected ClassType		classType;

	public Scalar()
	{
		super(DatasetType.Scalar);		
	}

	public Scalar( ClassType classType, Object object )
	{
		super(DatasetType.Scalar);
		
		init(classType, object);
	}

	public Scalar ( boolean boolVal )
	{
		super(DatasetType.Scalar);
	
		init( ClassType.Byte, Boolean.valueOf(boolVal) );
	}

	public Scalar ( byte byteVal )
	{
		super(DatasetType.Scalar);
	
		init( ClassType.Byte, Byte.valueOf(byteVal) );
	}

	public Scalar (char charVal )
	{
		super(DatasetType.Scalar);
	
		init( ClassType.Char, Character.valueOf(charVal) );
	}

	public Scalar ( short shortVal )
	{
		super(DatasetType.Scalar);
	
		init( ClassType.Short, Short.valueOf(shortVal) );
	}

	public Scalar ( int intVal )
	{
		super(DatasetType.Scalar);
	
		init( ClassType.Int, Integer.valueOf(intVal) );
	}

	public Scalar ( long longVal )
	{
		super(DatasetType.Scalar);
	
		init( ClassType.Long, Long.valueOf(longVal) );
	}

	public Scalar ( float floatVal )
	{
		super(DatasetType.Scalar);
	
		init( ClassType.Float, Float.valueOf(floatVal) );
	}

	public Scalar ( double doubleVal )
	{
		super(DatasetType.Scalar);
	
		init( ClassType.Double, Double.valueOf(doubleVal) );
	}

	public Scalar( String objectName )
	{
		super(DatasetType.Scalar);
		
		init( ClassType.Object, ClassUtil.constructObject(objectName) );
	}

	protected void init(ClassType classType, Object object) 
	{	
		this.classType = classType;
		this.object = object;
	}


	//====================== Getters and Setters ==============================
	
	public ClassType getClassType()
	{
		return classType;
	}

	public void setClassType(ClassType classType)
	{
		this.classType = classType;
	}
}

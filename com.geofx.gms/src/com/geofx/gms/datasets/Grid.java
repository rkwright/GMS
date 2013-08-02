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

import java.io.IOException;
import java.lang.reflect.Array;

import javax.vecmath.Matrix4d;

import org.eclipse.core.runtime.Path;

import com.geofx.gms.datasets.ClassUtil.ClassType;
import com.geofx.gms.plugin.GMSPlugin;

/**
 *  Class which implements support for grids, i.e. those data sets that consist
 *  of rectangular grids.  Can be of any dimension, though NxM (two-dimensional) is most
 *  common. In contrast to UniformGrids, a grid need not be uniform (and usually isn't).  
 *  In consequence, the grid's objects either are non-spatial (depending on an associated 
 *  UniformGrid for location information, or the objects themselves contain the coordinate info.
 */
public class Grid extends Dataset
{
	protected Object		array;
	protected Matrix4d		ctm = new Matrix4d();
	protected int[] 		dims;

	//protected ZipArray		zipArray;
	
	public Grid()
	{
		super(DatasetType.Grid);		
	}

	public Grid( DatasetType datasetType, ClassType classType, int[] dimRay, Object array )
	{
		super(datasetType);
		
		init(classType, null, dimRay, array);
	}

	public Grid( DatasetType datasetType, String objectName, int[] dimRay, Object array )
	{
		super(datasetType);
		
		init(null, objectName, dimRay, array);
	}

	public Grid( DatasetType datasetType, ClassType classType, String objectName, int[] dimRay, Object array )
	{
		super(datasetType);
		
		init(classType, objectName, dimRay, array);
	}

	public Grid( DatasetType datasetType )
	{
		super(datasetType);
	}
	
	public Grid ( ClassType classType, int dim, Object array )
	{
		super(DatasetType.Grid);
	
		int[] dimRay = new int[1];
		
		dimRay[0] = dim;
		
		init( classType, null, dimRay, array );
	}

	public Grid ( String objectName, int dim, Object array )
	{
		super(DatasetType.Grid);
	
		int[] dimRay = new int[1];
		
		dimRay[0] = dim;
		
		init( null, objectName, dimRay, array );
	}

	public Grid ( ClassType classType, int xDim, int yDim, Object array )
	{
		super(DatasetType.Grid);
	
		int[] dimRay = new int[2];
		
		dimRay[0] = xDim;
		dimRay[1] = yDim;
		
		init( classType, null, dimRay, array );
	}

	public Grid ( String objectName, int xDim, int yDim, Object array )
	{
		super(DatasetType.Grid);
	
		int[] dimRay = new int[2];
		
		dimRay[0] = xDim;
		dimRay[1] = yDim;
		
		init( null, objectName, dimRay, array );
	}

	public Grid ( ClassType classType, int xDim, int yDim, int zDim, Object array )
	{
		super(DatasetType.Grid);
	
		int[] dimRay = new int[3];
		
		dimRay[0] = xDim;
		dimRay[1] = yDim;
		dimRay[2] = zDim;
		
		init( classType, null, dimRay, array );
	}

	public Grid ( String objectName, int xDim, int yDim, int zDim, Object array )
	{
		super(DatasetType.Grid);
	
		int[] dimRay = new int[3];
		
		dimRay[0] = xDim;
		dimRay[1] = yDim;
		dimRay[2] = zDim;
		
		init( null, objectName, dimRay, array );
	}

	/**
	 * Performs a deep copy of this object
	 * 
	 * @return
	 */
	public void copy ( Grid grid )
	{
		super.copy(grid);
		
		// TODO - implement me!
		
	}
	
	protected void init(ClassType classType, String objectName, int[] dimRay, Object array ) throws RuntimeException, IllegalArgumentException
	{
		//if (zipArray == null)
		ZipArray zipArray = new ZipArray();
		
		this.classType = classType;
		this.objectName = objectName;
		
		if (classType == null || classType == ClassType.Object)
			this.array = zipArray.create(ClassType.Object, objectName, dimRay, array);
		else
		{
			this.objectName = classType.toString();
			this.array =  zipArray.create(classType, null, dimRay, array);
		}
		
		dims = new int[Array.getLength(dimRay)];
		
		for ( int i=0; i<Array.getLength(dimRay); i++ )
		{
			this.dims[i] = dimRay[i];
		}
		
		ctm.setIdentity();
	}

	public void save(String path)
	{
		ZipArray zipArray = new ZipArray();

		try
		{
			//IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
			zipArray.save(new Path(path), this);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static Grid load( String path )
	{
		ZipArray zipArray = new ZipArray();
		Grid grid = null;
		
		try
		{
			//IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
			//String fullPath = GMSPlugin.getFullPath(file);
			
			//IFile localFile = GMSPlugin.getContainer().getFile(new Path(path));
			//String localPath = localFile.getLocation().toPortableString();
			
			Path relativePath = GMSPlugin.getRelativePath(path);
			
			grid = zipArray.load(relativePath);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return grid;
	}
	
	public boolean equals ( Grid grid )
	{
		boolean equality = false;
		
		if (this.id.compareTo(grid.getID()) == 0 &&
			this.classType.compareTo(grid.getClassType()) == 0 &&
			this.created == grid.getCreated() && this.lastAccessed == grid.getLastAccessed() &&
			this.lastModified == grid.getLastModified() &&
			this.objectName.compareTo(grid.getObjectName())== 0 &&
			this.datasetType.equals(grid.getDataSetType()))
		{
			equality = true;
		}
		
		if (!equality)
			return false;
		
		if (Array.getLength(this.dims) == Array.getLength(grid.getDims()))
		{
			for ( int i=0; i<Array.getLength(this.dims); i++ )
			{
				if (this.dims[i] != grid.dims[i])
					return false;
			}
		}
		
		if (compareCtms(grid) == false)
			return false;
		
		return compareArrays(this.array, grid.getArray(), this.classType);
	}
	
	private int[] getDims()
	{
		return dims;
	}

	private boolean compareCtms( Grid grid )
	{
		for (int i=0; i<4; i++ )
			for ( int j=0; j<4; j++ )
				if (this.ctm.getElement(i,j) != grid.getCtm().getElement(i,j))
					return false;

		return true;
	}

	private boolean compareArrays(Object vec1, Object vec2, ClassType classType)
	{
		int k = 0;
		while (vec1.getClass().isArray() && k < Array.getLength(vec1))
		{
				Object row1 = Array.get(vec1, k);
				Object row2 = Array.get(vec2, k);
				if (row1.getClass().isArray() && row2.getClass().isArray())
				{
					if (!compareArrays(row1, row2, classType))
						return false;
				}
				else
				{
					if (classType.equals(ClassType.Boolean) || classType.equals(ClassType.Byte))
					{
						for (int i=0; i<Array.getLength(vec1); i++ )
						{
							if (Array.getByte(vec1,i) != Array.getByte(vec2,i))
								return false;
						}
					}
					else if (classType.equals(ClassType.Char))
					{
						for (int i=0; i<Array.getLength(vec1); i++ )
						{
							if (Array.getChar(vec1,i) != Array.getChar(vec2,i))
								return false;
						}
					}
					else if (classType.equals(ClassType.Short))
					{
						for (int i=0; i<Array.getLength(vec1); i++ )
						{
							if (Array.getShort(vec1,i) != Array.getShort(vec2,i))
								return false;
						}
					}
					else if (classType.equals(ClassType.Int))
					{
						for (int i=0; i<Array.getLength(vec1); i++ )
						{
							if (Array.getInt(vec1,i) != Array.getInt(vec2,i))
								return false;
						}
					}
					else if (classType.equals(ClassType.Long))
					{
						for (int i=0; i<Array.getLength(vec1); i++ )
						{
							if (Array.getLong(vec1,i) != Array.getLong(vec2,i))
								return false;
						}
					}
					else if (classType.equals(ClassType.Float))
					{
						for (int i=0; i<Array.getLength(vec1); i++ )
						{
							if (Array.getFloat(vec1,i) != Array.getFloat(vec2,i))
								return false;
						}
					}
					else if (classType.equals(ClassType.Double))
					{
						for (int i=0; i<Array.getLength(vec1); i++ )
						{
							if (Array.getDouble(vec1,i) != Array.getDouble(vec2,i))
								return false;
						}
					}
					else if (classType.equals(ClassType.Object))
					{
						for ( int j=0; j<Array.getLength(vec1); j++ )
						{
							IGMSSerialize hydro1 = (IGMSSerialize)Array.get(vec1, j);
							IGMSSerialize hydro2 = (IGMSSerialize)Array.get(vec2, j);
							if (!hydro1.compareTo(hydro2))
							//		if (!((IGMSSerialize)Array.get(vec1, j)).equals((IGMSSerialize)Array.get(vec2, j)))
								return false;
						}
					}

					return true;
				}
				k++;
			}
		
		return true;
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

	public String getObjectName()
	{
		return objectName;
	}

	public void setObjectName(String objectName)
	{
		this.objectName = objectName;
	}

	public Object getArray()
	{
		return array;
	}
	
	public Object getObject()
	{
		return array;
	}

	public void setArray(Object array)
	{
		this.array = array;
	}

	public Matrix4d getCtm()
	{
		return ctm;
	}

	public void setCtm(Matrix4d ctm)
	{
		this.ctm = ctm;
	}

	public void setDims(int[] dims)
	{
		this.dims = dims;
	}
}

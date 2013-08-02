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

import java.lang.reflect.Field;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.geofx.gms.datasets.ClassUtil.ClassType;
import com.geofx.gms.datasets.Dataset.DatasetType;

public class GridTest extends TestCase
{
	private static final long SLEEPY_TIME = 20;
	
	protected double[] dblRay = new double[10];
	protected double[] dblPtr = new double[10];

	/** 
	 * Default ctor for this class
	 * 
	 * @param name
	 */

	public GridTest( String name )
	{
		super(name);
	}
	
	/**
	 * setUp() method that initializes common objects
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * tearDown() method that cleans up the common objects
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public static Test suite()
	{
		TestSuite suite = new TestSuite(GridTest.class);
		return suite;
	}
	
	public void testGridCTor()
	{
		Field field, fieldPtr;
		ClassType classType;
		
		Grid grid2 = new Grid(ClassType.Double, 10, null);
		assertTrue(grid2 != null);

		try
		{
			field = getClass().getDeclaredField("dblRay");
			fieldPtr = getClass().getDeclaredField("dblPtr");
			//classType = ClassType.valueOf("double");
			
			fieldPtr.set(this, grid2.getArray());
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		
		Grid grid = new Grid(DatasetType.Grid);
		assertTrue(grid != null);

		grid = new Grid(ClassType.Boolean, 10, null);
		assertTrue(grid != null);

		grid = new Grid(ClassType.Byte, 10, null);
		assertTrue(grid != null);

		grid = new Grid(ClassType.Char, 10, null);
		assertTrue(grid != null);

		grid = new Grid(ClassType.Short, 10, null);
		assertTrue(grid != null);

		grid = new Grid(ClassType.Int, 10, null);
		assertTrue(grid != null);

		grid = new Grid(ClassType.Long, 10, null);
		assertTrue(grid != null);

		grid = new Grid(ClassType.Float, 10, null);
		assertTrue(grid != null);

		grid = new Grid(ClassType.Double, 10, null);
		assertTrue(grid != null);

		grid = new Grid(ClassType.Double, 10, null);
		assertTrue(grid != null);

		grid = new Grid(ClassType.Double, 10, 5, null);
		assertTrue(grid != null);

		grid = new Grid(ClassType.Double, 10, 12, 15, null);
		assertTrue(grid != null);

		grid = new Grid("com.geofx.gms.datasets.Hydro", 10, null);
		assertTrue(grid != null);

		int[] dims = {10, 10 };
		grid = new Grid(DatasetType.Grid, ClassType.Double, dims, null);
		assertTrue(grid != null);

		grid = new Grid(DatasetType.Grid, ClassType.Object, "com.geofx.gms.datasets.Hydro",  dims, null);
		assertTrue(grid != null);

		int[]    dims2 = { 10 };
		double[] depth  = { 0, 0.5, 1.0, 1.5, 2.0, 3.0, 4.0, 5.0, 7.5, 10.0 };

		grid = new Grid(DatasetType.Grid, ClassType.Double, dims2, depth);
		assertTrue(grid != null);

	}
	
	public void testGridCreateLoadSave()
	{
		int[] dims = { 5, 10, 15 };
		
		testGridCreateLoadSave(ClassType.Boolean, null, dims, null);

		testGridCreateLoadSave(ClassType.Byte, null, dims, null);

		testGridCreateLoadSave(ClassType.Boolean, null, dims, null);
		
		testGridCreateLoadSave(ClassType.Char, null, dims, null);
		
		testGridCreateLoadSave(ClassType.Short, null, dims, null);
		
		testGridCreateLoadSave(ClassType.Int, null, dims, null);
		
		testGridCreateLoadSave(ClassType.Long, null, dims, null);
		
		testGridCreateLoadSave(ClassType.Float, null, dims, null);
		
		testGridCreateLoadSave(ClassType.Double, null, dims, null);

		testGridCreateLoadSave(ClassType.Object, "com.geofx.gms.datasets.Hydro", dims, null);
		
		int[]    dims2 = { 10 };
		double[] depth  = { 0, 0.5, 1.0, 1.5, 2.0, 3.0, 4.0, 5.0, 7.5, 10.0 };

		testGridCreateLoadSave(ClassType.Double, null, dims2, depth);

	}
	
	public void testGridCreateLoadSave( ClassType classType, String objectType, int[] dims, Object array )
	{
		try
		{
			Grid grid = new Grid(DatasetType.Grid, classType, objectType, dims, array);
			assertTrue(grid != null);
			
			String	path = "./test/data/array.gza";
	
			grid.save(path);

			// we have to pause a little as the Windows Java implementation seems to have a bug 
			// in that if you don't wait it can't load the zipentry from the file it just created.  
			// Some OS locking/threading issue, presumably.  Or perhaps in the Zip implementation.
			Thread.sleep(SLEEPY_TIME);

			Grid newGrid = grid.load(path);
	
			assertTrue(newGrid != null);
	
			assertTrue(grid.equals(newGrid));
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}

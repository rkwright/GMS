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

import java.io.IOException;

import org.eclipse.core.runtime.Path;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.geofx.gms.datasets.ClassUtil.ClassType;

public class ZipArrayTest extends TestCase
{
	private ZipArray	zipArray;

	/** 
	 * Default ctor for this class
	 * 
	 * @param name
	 */
	public ZipArrayTest( String name )
	{
		super(name);
	}
	
	/**
	 * setUp() method that initializes common objects
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
		zipArray = new ZipArray();
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
		TestSuite suite = new TestSuite(ZipArrayTest.class);
		return suite;
	}
	
	public void testGridCreateSerializeLoad()
	{
		// this implicitly tests create - already covered in GridTestCtor
		Grid grid = new Grid(ClassType.Boolean, 10, null);
		assertTrue(grid != null);
		
		Path path = new Path(".");
		try
		{
			zipArray.save(path, grid);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}
}

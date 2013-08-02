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

import java.lang.reflect.Array;

import com.geofx.gms.datasets.ClassUtil.ClassType;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ClassUtilTest extends TestCase
{
	public ClassUtilTest( String name )
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
		TestSuite suite = new TestSuite(ClassUtilTest.class);
		//suite.addTest(new TestClassUtil("testClassType"));
		return suite;
	}

	public void testGetClassType()
	{
		boolean[] boolVec = new boolean[5];
		ClassType type = ClassType.valueOf(boolVec);
		assertTrue(type == ClassType.Boolean);
		
		type = ClassType.valueOf("Boolean");
		assertTrue(type == ClassType.Boolean);
		type = ClassType.valueOf("Byte");
		assertTrue(type == ClassType.Byte);
		type = ClassType.valueOf("Char");
		assertTrue(type == ClassType.Char);
		type = ClassType.valueOf("Short");
		assertTrue(type == ClassType.Short);
		type = ClassType.valueOf("Int");
		assertTrue(type == ClassType.Int);
		type = ClassType.valueOf("Float");
		assertTrue(type == ClassType.Float);
		type = ClassType.valueOf("Long");
		assertTrue(type == ClassType.Long);
		type = ClassType.valueOf("Double");
		assertTrue(type == ClassType.Double);
		type = ClassType.valueOf("Object");
		assertTrue(type == ClassType.Object);
	}

	public void testGetClass()
	{
		Class<?> classe = ClassType.getClass("Boolean");
		assertTrue(classe == boolean.class);
		classe = ClassType.getClass("Byte");
		assertTrue(classe == byte.class);
		classe = ClassType.getClass("Char");
		assertTrue(classe == char.class);
		classe = ClassType.getClass("Short");
		assertTrue(classe == short.class);
		classe = ClassType.getClass("Int");
		assertTrue(classe == int.class);
		classe = ClassType.getClass("Float");
		assertTrue(classe == float.class);
		classe = ClassType.getClass("Long");
		assertTrue(classe == long.class);
		classe = ClassType.getClass("Double");
		assertTrue(classe == double.class);
		classe = ClassType.getClass("Object");
		assertTrue(classe == Object.class);
	}

	public void testGetClassTypeFromObject()
	{
		int[] dims = { 5 };
		
		Object obj = ClassUtil.constructObjectArray(boolean.class,dims);	
		ClassType type = ClassType.getClassType(obj);		
		assertTrue(type == ClassType.Boolean);
		
		obj = ClassUtil.constructObjectArray(byte.class,dims);
		type = ClassType.getClassType(obj);		
		assertTrue(type == ClassType.Byte);

		obj = ClassUtil.constructObjectArray(char.class,dims);
		type = ClassType.getClassType(obj);		
		assertTrue(type == ClassType.Char);

		obj = ClassUtil.constructObjectArray(short.class,dims);
		type = ClassType.getClassType(obj);		
		assertTrue(type == ClassType.Short);

		obj = ClassUtil.constructObjectArray(int.class,dims);
		type = ClassType.getClassType(obj);		
		assertTrue(type == ClassType.Int);

		obj = ClassUtil.constructObjectArray(float.class,dims);
		type = ClassType.getClassType(obj);		
		assertTrue(type == ClassType.Float);

		obj = ClassUtil.constructObjectArray(long.class,dims);
		type = ClassType.getClassType(obj);		
		assertTrue(type == ClassType.Long);

		obj = ClassUtil.constructObjectArray(double.class,dims);
		type = ClassType.getClassType(obj);		
		assertTrue(type == ClassType.Double);

		obj = ClassUtil.constructObjectArray(Object.class,dims);
		for (int i=0; i<Array.getLength(obj); i++ )
		{
			Object object = new Object();
			Array.set(obj, i, object);
		}

		type = ClassType.getClassType(obj);		
		assertTrue(type == ClassType.Object);
	}
	
	public void testConvertToClassType( int i )
	{
		
		ClassType type = ClassType.convert(0);
		assertTrue(type == ClassType.Boolean);
		
		type = ClassType.convert(1);
		assertTrue(type == ClassType.Byte);

		type = ClassUtil.ClassType.convert(2);
		assertTrue(type == ClassType.Char);

		type = ClassUtil.ClassType.convert(3);
		assertTrue(type == ClassType.Short);

		type = ClassUtil.ClassType.convert(4);
		assertTrue(type == ClassType.Int);

		type = ClassUtil.ClassType.convert(5);
		assertTrue(type == ClassType.Float);

		type = ClassUtil.ClassType.convert(6);
		assertTrue(type == ClassType.Long);

		type = ClassUtil.ClassType.convert(7);
		assertTrue(type == ClassType.Double);

		type = ClassUtil.ClassType.convert(8);
		assertTrue(type == ClassType.Object);
	}
	
	public void testConstructObject()
	{
		Object obj = ClassUtil.constructObject("java.lang.Object");
		assertTrue(obj != null);
	}
	
	public void testConstructObjectArrayFromName()
	{
		int[] vec = {10,10,10};
		Object obj = ClassUtil.constructObjectArray("java.lang.Object", vec);
		assertTrue(obj != null);
	}

	public void testConstructObjectArrayFromClass()
	{
		int[] vec = {10,10,10};
		Object obj = ClassUtil.constructObjectArray(double.class, vec);
		assertTrue(obj != null);
	}

	
}

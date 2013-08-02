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
import java.lang.reflect.InvocationTargetException;


public class ClassUtil
{
	public static final int ClassSize[] = { 1, 1, 2, 2, 4, 8, 4, 8, 0, 0 };

	public static Class<?> Classe[] = { boolean.class, byte.class, char.class, short.class, int.class,
			long.class, float.class, double.class, String.class, Object.class };

	/**
	 *  Class type enum and helper methods
	 */
	public enum ClassType 
	{	
		Boolean, Byte, Char, Short, Int, Long, Float, Double, String, Object;
		
		public static ClassType convert(int i)
		{
			for (ClassType current : ClassType.values())
			{
				if (current.ordinal() == i)
				{
					return current;
				}
			}
			
			return null;
		}
		
		public static ClassType valueOf ( Object obj )
		{
			if (obj instanceof String)
				return valueOf((String)obj);
			else
				return getClassType(obj);			
		}
		
		public static ClassType getClassType ( Object  obj )
		{
			if (obj.getClass().isArray() && Array.get(obj, 0).getClass().isArray())
				return getClassType( Array.get(obj, 0) );
			
			Class 	classe = obj.getClass();
			Class 	component = classe.getComponentType();
			if (component != null)
				classe = component;

			for ( int i=0; i<Array.getLength(Classe)-1; i++ )
			{
				if (Classe[i] == classe)
					return convert(i);
			}
			
			return Object;
		}

		/**
		 * Helper routine to get the class from the String
		 * 
		 * @param typeStr
		 * @return
		 */
		public static Class<?> getClass(String typeStr)
		{
			ClassType type = valueOf(typeStr);

			if (type == null)
				return null;
			
			return Classe[type.ordinal()];
		}

	};


	
	/**
	 * Helper function to create a new object to facilitate the extraction
	 * process.
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Object constructObject( String name )
	{
		Object 			object = null;
		Class<Object> 	classe;
	
		try
		{
			classe = (Class<Object>) Class.forName(name);
			
			object = classe.getConstructor(null).newInstance();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
			
		return object;
	}
	
	/**
	 * Helper function to create a new array of objects given the class
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Object constructObjectArray( String name, int[] dimensions )
	{
		Object 			object = null;
		Class<Object> 	classe;
			
		try
		{
			classe = (Class<Object>) Class.forName(name);
			
			object = constructObjectArray( classe, dimensions);
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		
		return object;
	}
	
	/**
	 * Helper function to create a new array of objects from the class component 
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Object constructObjectArray( Class classe, int[] dimensions )
	{
		Object 			object = null;
	
		try
		{			
			object = java.lang.reflect.Array.newInstance(classe, dimensions);
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return object;
	}
	

	/*
	private static Map nameToPrimitiveClass = new HashMap();
	static
	{
		nameToPrimitiveClass.put("boolean", Boolean.TYPE);
		nameToPrimitiveClass.put("byte", Byte.TYPE);
		nameToPrimitiveClass.put("char", Character.TYPE);
		nameToPrimitiveClass.put("short", Short.TYPE);
		nameToPrimitiveClass.put("int", Integer.TYPE);
		nameToPrimitiveClass.put("long", Long.TYPE);
		nameToPrimitiveClass.put("float", Float.TYPE);
		nameToPrimitiveClass.put("double", Double.TYPE);
	}

	protected Class findClass(String name) throws ClassNotFoundException
	{
		Class c = (Class) nameToPrimitiveClass.get(name);
		if (c == null)
			throw new ClassNotFoundException(name);
		return c;
	}
 	*/

}

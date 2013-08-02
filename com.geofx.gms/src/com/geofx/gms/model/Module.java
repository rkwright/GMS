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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.vecmath.Matrix4d;

import org.xml.sax.Attributes;

import com.geofx.gms.controller.Clock;
import com.geofx.gms.controller.Controller;
import com.geofx.gms.datasets.ClassUtil;
import com.geofx.gms.datasets.Dataset;
import com.geofx.gms.datasets.ClassUtil.ClassType;
import com.geofx.gms.plugin.Constants;
import com.geofx.gms.viewers.ViewInfo;
import com.geofx.opengl.view.IGLView;

public abstract class Module
{
	protected Matrix4d 				ctm = new Matrix4d();
	protected Clock 				clock;

	protected String 				objectName;
	protected String				tab;
	protected ArrayList<FieldValue> fieldValues = new ArrayList<FieldValue>();
	protected ArrayList<Input> 		inputs = new ArrayList<Input>();
	protected ArrayList<Output> 	outputs = new ArrayList<Output>();

	protected Controller 			controller;
	
	protected IGLView				view;

	protected ViewInfo				viewInfo = null;
	protected boolean				firstView = false;

	/**
	 * Default ctor for the automated construction via reflection
	 */
	public Module()
	{
		ctm.setIdentity();
	}

	/**
	 * Set all the values
	 * 
	 * @param attributes
	 * @param inputs
	 * @param outputs
	 * @param fieldValues
	 */
	public void setValues(Attributes attributes, ArrayList<Input> inputs, ArrayList<Output> outputs,
			ArrayList<FieldValue> fieldValues)
	{
		this.objectName = attributes.getValue(attributes.getIndex(Constants.OBJECT));
		int index = attributes.getIndex(Constants.TAB);
		if (index >= 0)
			this.tab = attributes.getValue(attributes.getIndex(Constants.TAB));

		this.inputs.addAll(inputs);

		this.outputs.addAll(outputs);
		
		this.fieldValues.addAll(fieldValues);
	}
	
	public void initDatasets()
	{	
		setFields();
		
		for ( int i=0; i<inputs.size(); i++ )
		{
			Input input = inputs.get(i);
				
			//if (input.getType().endsWith("Grid"))
			//{
				if (input.getHRef() != null && input.getHRef().length() > 0)
				{
					controller.resolveDataset(input);
				}
				else if (input.getIDRef() != null && input.getIDRef().length() > 0)
				{
					input.filePath = controller.resolveManifestItem(input.getIDRef());
				}
			//}
		}
	}

	protected void setFields()
	{
		try
		{
			for (int i = 0; i < fieldValues.size(); i++)
			{
				FieldValue fv = fieldValues.get(i);
				if (fv.values.size() == 1)
					setField(fv.field, fv.type, fv.values.get(0));
				else
				{
					setArrayField(fv.field, fv.type, fv.values);
				}
			}
		}
		catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * THis is called at initialization time for each value in fieldValues to set the actual values
	 * in the class based on this abstract class.
	 * 
	 * @param fieldName
	 * @param type
	 * @param value
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public void setField(String fieldName, String type, String value) throws NoSuchFieldException, IllegalAccessException
	{
		Field field = getClass().getDeclaredField(fieldName);
		ClassType classType = ClassType.valueOf(type);

		switch (classType)
		{
			case Boolean:
				field.setBoolean(this, Boolean.parseBoolean(value));
				break;

			case Byte:
				field.setByte(this, Byte.parseByte(value));
				break;

			case Char:
				field.setChar(this, Character.valueOf(value.charAt(0)));
				break;

			case Short:
				field.setShort(this, Short.parseShort(value));
				break;

			case Int:
				if (value.charAt(0) == '#')
					field.setInt(this, Integer.parseInt(value.substring(1),16));
				else
					field.setInt(this, Integer.parseInt(value));					
				break;

			case Long:
				field.setLong(this, Long.parseLong(value));
				break;

			case Float:
				field.setFloat(this, Float.parseFloat(value));
				break;

			case Double:
				field.setDouble(this, Double.parseDouble(value));
				break;
			case String:
				field.set(this, value);
				break;
		}
	}

	/**
	 * Sets the values of an array from the field values. Only supports one-dimensional arrays for
	 * now. Will need to implement recursion support to support n-dimensional arrays.
	 * 
	 * @param fieldValue
	 * @param type
	 * @param array
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public void setArrayField( String fieldName,  String type, ArrayList<String> values ) throws NoSuchFieldException, IllegalAccessException
	{
		Field field = getClass().getDeclaredField(fieldName);
		//Object array = field.get(this);
		
		Object array = Array.newInstance(ClassUtil.ClassType.getClass(type),values.size());
		
		field.set(this, array);

		ClassType classType = ClassType.valueOf(type);

		for (int n = 0; n < values.size(); n++)
		{
			String value = values.get(n);

			switch (classType)
			{
				case Boolean:
					Array.setBoolean(array, n, Boolean.parseBoolean(value));
					break;

				case Byte:
					Array.setByte(array, n, Byte.parseByte(value));
					break;

				case Char:
					Array.setChar(array, n, Character.valueOf(value.charAt(0)));
					break;

				case Short:
					Array.setShort(array, n, Short.parseShort(value));
					break;

				case Int:
					if (value.charAt(0) == '#')
						Array.setInt(array, n, Integer.parseInt(value.substring(1),16));
					else
						Array.setInt(array, n, Integer.parseInt(value));
					break;

				case Long:
					Array.setLong(array, n, Long.parseLong(value));
					break;

				case Float:
					Array.setFloat(array, n, Float.parseFloat(value));
					break;

				case Double:
					Array.setDouble(array, n, Double.parseDouble(value));
					break;
			}
		}
	}

	/**
	 * Method allows implementing classes to perform whatever initialization is appropriate. 
	 * 
	 * @param clock
	 */
	public void init()
	{
	}


	/**
	 * Provided to subclasses can provide whatever reset actions are appropriate, such
	 * as resetting the model to the starting parameters.
	 */
	public  void reset()
	{
	}

	/**
	 * Request the current volume whose dimensions bound the model's view
	 */
	public Matrix4d getView(String viewID)
	{
		return ctm;
	}

	/**
	 * Give the module access to the controller. Also provides the current model clock 
	 * which is already initialized. This clock is effectively the master clock for the 
	 * whole time-dependent model process.
	 * 
	 * @param controller
	 * @param clock
	 */
	public void setController( Controller controller, Clock clock )
	{
		this.controller = controller;
		this.clock = clock;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Dataset getDataset( String id )
	{
		for ( int i=0; i<outputs.size(); i++ )
		{
			Output output = outputs.get(i);
			
			if (output.getId().equals(id))
				return output.dataset;
		}

		return null;
	}

	/**
	 * Request to the model to update it's internal model, if needed. 
	 */
	public boolean update()
	{
		// first we need to call our "ancestors" to see if they need updating.
		for ( int i=0; i<inputs.size(); i++ )
		{
			Module module = inputs.get(i).module;
			if (module != null)
				module.update();
		}

		// then we need to check if this module needs to update itself
		double inputTime = 0;
		for ( int i=0; i<inputs.size(); i++ )
		{
			double modTime = inputs.get(0).dataset.getLastModified();
			inputTime = Math.max(inputTime, modTime);
		}

		for ( int i=0; i<outputs.size(); i++ )
		{	
			if (inputTime > outputs.get(i).dataset.getLastModified())
				return true;
		}

		return false;
	}

	/**
	 * Subclasses that are view modules should over-ride this.
	 * 
	 * @return
	 */
	public ViewInfo getViewInfo()
	{
		return null;
	}

	public boolean isFirstView()
	{
		return firstView;
	}

	public void setFirstView(boolean firstView)
	{
		this.firstView = firstView;
	}

	public boolean initView()
	{	
		return false;
	}
}

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

package com.geofx.gms.controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.eclipse.core.runtime.Path;
import org.xml.sax.helpers.AttributesImpl;

import com.geofx.gms.model.ProjectInfo;

import junit.framework.TestCase;

/**
 * @author riwright
 *
 */
public class ControllerTest extends TestCase
{
	Controller		controller;
	ProjectInfo		projectInfo = new ProjectInfo();
	AttributesImpl	attributes = new AttributesImpl();
	
	static final String filePath = "./test/data/tempwave.gms";
	static final String outFilePath = "./test/data/outTempwave.gms";

	/**
	 * @param name
	 */
	public ControllerTest(String name)
	{
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
			
		// constructAttributes();		
	}

	/*
	private void constructAttributes()
	{
		attributes.addAttribute("", "begin", "begin", "CDATA", "0s" );
		attributes.addAttribute("", "end", "end", "CDATA", "2y" );
		attributes.addAttribute("", "step", "step", "CDATA", "3600s" );
		attributes.addAttribute("", "rate", "rate", "CDATA", "1d" );
	}
	*/
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	/**
	 * Test method for {@link com.geofx.gms.controller.Controller#Controller(org.xml.sax.Attributes, com.geofx.gms.model.ProjectInfo)}.
	 */
	public void testController()
	{
		initProjectInfo();

		controller = projectInfo.getController();
		
		controller.restart();
	}

	/**
	 * Test method for {@link com.geofx.gms.controller.Controller#Controller(org.xml.sax.Attributes, com.geofx.gms.model.ProjectInfo)}.
	 */
	public void testSerialization()
	{
		initProjectInfo();

		controller = projectInfo.getController();
		
		serializeProjectInfo();
	}

	public void initProjectInfo()
	{
		Path path = new Path(filePath);
	
		FileInputStream stream;  
		
		try
		{
			stream = new FileInputStream(path.toPortableString());

			System.out.println("Stream = " + stream);

			projectInfo.parse(stream);

			assertTrue(projectInfo.modules.size() > 0);

			System.out.println("Parse complete");
		}
		catch (FileNotFoundException e)
		{
			assertTrue(false);
			e.printStackTrace();
		}		
		catch (Exception e)
		{
			assertTrue(false);
			e.printStackTrace();
		}		
	}
	
	public void serializeProjectInfo()
	{
		Path path = new Path(outFilePath);
	
		FileOutputStream stream;  
		
		try
		{
			stream = new FileOutputStream(path.toPortableString());

			System.out.println("Stream = " + stream);

			projectInfo.serialize(stream);

			assertTrue(true);

			System.out.println("Parse complete");
		}
		catch (FileNotFoundException e)
		{
			assertTrue(false);
			e.printStackTrace();
		}		
		catch (Exception e)
		{
			assertTrue(false);
			e.printStackTrace();
		}		
	}
	/**
	 * Test method for {@link com.geofx.gms.controller.Controller#convertToSeconds(java.lang.String)}.
	 */
	public void testConvertToSeconds()
	{
	}

	/**
	 * Test method for {@link com.geofx.gms.controller.Controller#start()}.
	 */
	public void testStart()
	{
	}

	/**
	 * Test method for {@link com.geofx.gms.controller.Controller#getProjectInfo()}.
	 */
	public void testGetProjectInfo()
	{
	}

	/**
	 * Test method for {@link com.geofx.gms.controller.Controller#modelLoop()}.
	 */
	public void testModelLoop()
	{
	}

}

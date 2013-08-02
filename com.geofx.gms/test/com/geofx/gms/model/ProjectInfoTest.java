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

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import junit.framework.TestCase;

import org.eclipse.core.runtime.Path;

import com.geofx.gms.model.ProjectInfo;

public class ProjectInfoTest extends TestCase
{
	static final String filePath = "./test/data/tempwave.gms";

	private ProjectInfo projectInfo = new ProjectInfo();
	
	/** 
	 * Default ctor for this class
	 * 
	 * @param name
	 */
	public ProjectInfoTest ( String name )
	{
		super(name);
	}
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public void testParse()
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
}

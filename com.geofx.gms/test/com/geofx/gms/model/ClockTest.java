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

import com.geofx.gms.controller.Clock;

import junit.framework.TestCase;

public class ClockTest extends TestCase
{

	private static final int MAX_LOOP = 20;
	private static final long SLEEPY_TIME = 50;

	private Clock	clock = new Clock();
		
	public ClockTest(String name)
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

	public void testTime()
	{
		try
		{
			clock.restart();
			
			for ( int i=0; i<MAX_LOOP; i++ )
			{
				Thread.sleep(SLEEPY_TIME);	
				clock.time();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}

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

import com.geofx.gms.controller.Clock.ClockPrecision;
import com.geofx.gms.controller.Clock.ClockType;

import junit.framework.TestCase;

/**
 * @author riwright
 *
 */
public class ClockTest extends TestCase
{
	private static final int NUM_DUMPS = 10;
	Clock	clock;
	/**
	 * @param name
	 */
	public ClockTest(String name)
	{
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	/**
	 * Test method for {@link com.geofx.gms.controller.Clock#Clock()}.
	 */
	public void testClock()
	{
		clock = new Clock();
		
		assertTrue(clock!= null);
		assertTrue(clock.getClockType() == ClockType.RELATIVE);
		assertTrue(clock.getPrecision() == ClockPrecision.NANO);
		assertTrue(clock.isPaused());
	}

	/**
	 * Test method for {@link com.geofx.gms.controller.Clock#Clock(com.geofx.gms.controller.Clock.ClockType, double, double, double)}.
	 */
	public void testClockTypeWithParms()
	{
		clock = new Clock( ClockType.RELATIVE, 1000.0, 10000.0, 500.0);
		
		assertTrue(clock!= null);
		assertTrue(clock.getClockType() == ClockType.RELATIVE);
		assertTrue(clock.getPrecision() == ClockPrecision.NANO);
		
		dumpTimes();
		
		clock.restart();
		
		dumpTimes();
	}

	protected void dumpTimes()
	{
		double	times[] = new double[NUM_DUMPS];
		try
		{
			for (int i = 0; i < NUM_DUMPS; i++)
			{
				times[i] = clock.time();
				Thread.sleep(100);
			}

			for (int i = 0; i < NUM_DUMPS; i++)
			{
				System.out.println(String.format("%2d ", i) + String.format("%6.2f s", times[i]));
				Thread.sleep(100);
			}

			System.out.println();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Test method for {@link com.geofx.gms.controller.Clock#pause()}.
	 */
	public void testPause()
	{
		try
		{

			clock = new Clock();
			System.out.println(String.format("%6.4f s", clock.time()));

			clock.pause();
			Thread.sleep(1000);
			double t = clock.time();
			
			System.out.println(String.format("Time now: %6.4f s", t));
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Test method for {@link com.geofx.gms.controller.Clock#isPaused()}.
	 */
	public void testIsPaused()
	{
	}

	/**
	 * Test method for {@link com.geofx.gms.controller.Clock#getClockType()}.
	 */
	public void testGetClockType()
	{
	}

	/**
	 * Test method for {@link com.geofx.gms.controller.Clock#setClockType(com.geofx.gms.controller.Clock.ClockType)}.
	 */
	public void testSetClockType()
	{
	}

	/**
	 * Test method for {@link com.geofx.gms.controller.Clock#getPrecision()}.
	 */
	public void testGetPrecision()
	{
	}

	/**
	 * Test method for {@link com.geofx.gms.controller.Clock#setPrecision(com.geofx.gms.controller.Clock.ClockPrecision)}.
	 */
	public void testSetPrecision()
	{
	}

}

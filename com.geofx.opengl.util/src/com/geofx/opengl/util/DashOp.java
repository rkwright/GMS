/*******************************************************************************
 * Copyright (c) 2008 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 *******************************************************************************/

package com.geofx.opengl.util;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.ListIterator;

import com.geofx.opengl.util.Constants;
import com.geofx.opengl.util.Dash;
import com.geofx.opengl.util.DashOp;
import com.geofx.opengl.util.Pool;


public class DashOp
{

	private LinkedList<Dash> 		dashes = new LinkedList<Dash>();
	private ListIterator<Dash>		iter;
	private double 					dashOffset = 0.0;
	private double 					currentPos;
	private Dash					maxDash = new Dash( Constants.MAX_FLOAT, false );
	private Pool					pool;

	
	public DashOp( Pool pool )
	{
		this.pool = pool;
	}

	/**
	 *   Copy ctor
	 */
	public DashOp( Pool pool, DashOp dashOp )
	{
		this.pool = pool;
		set(dashOp);
	}
	
	public double getDashOffset()
	{
		return dashOffset;
	}

	public LinkedList<Dash> getDash()
	{
		return dashes;
	}

	public void setDash(double[] dashArray, double dashOffset)
	{
		dashes.clear();
		int		nDash = Array.getLength(dashArray);
		double	seqLen = 0;
		for (int i = 0; i < nDash; i++)
		{
			if (dashArray[i] > 0)
			{
				dashes.add(pool.newDash(dashArray[i], ((i & 1)) == 1));
				seqLen += dashArray[i];
			}
		}

		// if there is nothing there or all zeroes, just clear the dashes
		if (seqLen == 0 || nDash == 0)
			dashes.clear();
		
		// if the dash array has an even number of dashes, duplicate
		// it, inverting the gaps.  This is per spec and behaviour of PS
		if ((nDash & 1) == 1 && dashes.size() > 0)
		{
			for ( int i=0; i<nDash; i++ )
			{
				Dash dash = dashes.get(i);
				dashes.add(pool.newDash(dash.length,!dash.gap));
			}
		}
		
		this.dashOffset = (dashOffset >= 0) ? dashOffset : 0;
	}

	public void set( DashOp dashOp )
	{
		this.dashOffset = dashOp.dashOffset;
		
		dashes.clear();
		
		iter = dashOp.dashes.listIterator();
		while (iter.hasNext())
		{
			this.dashes.add(iter.next());
		}
	}
	
	public Dash getFirst()
	{
		if (dashes.size() == 0)
			return maxDash;
		
		iter = dashes.listIterator();
		currentPos = 0;
		while (true)
		{
			if (!iter.hasNext())
			{
				throw new RuntimeException("Failed to find dash!");				
			}
			
			Dash dash = iter.next();
			if ((currentPos + dash.length) > dashOffset)
			{
				currentPos = currentPos + dash.length - dashOffset;
				return pool.newDash(dash.length-dashOffset, dash.gap);
			}
			
			currentPos += dash.length;
		}
	}
	
	public Dash next()
	{
		if (!iter.hasNext())
		{
			iter = dashes.listIterator();				
		}

		return pool.newDash(iter.next());
	}
}

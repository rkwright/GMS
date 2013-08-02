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

package com.geofx.science.tempwave;

import java.lang.reflect.Array;

import javax.vecmath.Point2d;

import com.geofx.opengl.util.DataIteratorXY;

public class TempwaveIter implements DataIteratorXY
{
	private int 			index = 0;
	private TempwaveData	tempwaveData;

	public TempwaveIter( TempwaveData tempwaveData )
	{
		this.tempwaveData = tempwaveData;
	}

	public boolean hasNext()
	{
		return index < Array.getLength(tempwaveData.temps);
	}

	public double getX()
	{
		return tempwaveData.time;
	}

	public double getY()
	{
		return tempwaveData.temps[index];
	}

	public void reset()
	{
		index = 0;
	}

	public void next()
	{
		index++;
	}

	Point2d point = new Point2d();
	
	public Point2d getXY(int index)
	{
		point.x = tempwaveData.time;
		point.y = tempwaveData.temps[index];
		
		return point;
	}

	public int size()
	{
		return Array.getLength(tempwaveData.temps);
	}

}

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

import com.geofx.gms.datasets.Dataset;
import com.geofx.opengl.util.DataIterator;
import com.geofx.opengl.util.DataIteratorXY;

/**
 * @author riwright
 *
 */
public class TempwaveData extends Dataset implements DataIteratorXY, DataIterator
{
		public double[] 	temps;
		public double 		time;
		private int 		index = 0;

		public TempwaveData()
		{
			super();
		}
		
		public TempwaveData ( int nTemps )
		{
			super(DatasetType.UserDefined);		
			
			temps = new double[nTemps];
		}	

		/**
		 * Performs a deep copy of this object
		 * 
		 * @return
		 */
		public void copy ( TempwaveData tempwaveData )
		{
			super.copy(tempwaveData);
			
			this.temps = new double[Array.getLength(tempwaveData.temps)];
			
			for (int i=0; i<Array.getLength(tempwaveData.temps); i++ )
			{
				this.temps[i] = tempwaveData.temps[i];
			}
			
			this.time = tempwaveData.time;
		}
		
		public boolean hasNext()
		{
			return index < Array.getLength(this.temps);
		}

		public double getX()
		{
			return this.time;
		}

		public double getY()
		{
			return this.temps[index];
		}

		public void reset()
		{
			index = 0;
		}

		public void next()
		{
			index++;
		}

		public Point2d getXY(int index)
		{
			Point2d point = new Point2d();
			point.x = this.time;
			point.y = this.temps[index];
			
			return point;
		}

		public int size()
		{
			return Array.getLength(this.temps);
		}

		public double get()
		{
			return this.temps[index];
		}

		/**
		 * Override the super class method to return the vector
		 */
		public Object getObject()
		{
			return this.temps;
		}

}

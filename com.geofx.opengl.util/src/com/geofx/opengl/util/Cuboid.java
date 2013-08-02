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

import javax.vecmath.Point3d;

public class Cuboid
{
	private	Point3d		ll;
	private	Point3d		ur;
	
	public Cuboid( Point3d l, Point3d u )
	{
		this.ll = l;
		this.ur = u;
	}
	
	public Cuboid ( double llx, double lly, double llz, double urx, double ury, double urz )
	{
		ll.x = llx;
		ll.y = lly;
		ll.z = llz;
		ur.x = urx;
		ur.y = ury;
		ur.z = urz;
	}

	public Point3d getLL()
	{
		return ll;
	}

	public void setLL(Point3d ll)
	{
		this.ll = ll;
	}

	public Point3d getUR()
	{
		return this.ur;
	}

	public void setUR(Point3d ur)
	{
		this.ur = ur;
	}
	
	public double getURX()
	{
		return this.ur.x;
	}

	public void setURX( double x)
	{
		this.ur.x = x;
	}

	public double getURY()
	{
		return this.ur.y;
	}

	public void setURY( double y)
	{
		this.ur.y = y;
	}

	public double getURZ()
	{
		return this.ur.z;
	}

	public void setURZ( double z )
	{
		this.ur.z = z;
	}
	public double getLLX()
	{
		return this.ll.x;
	}

	public void setLL( double x)
	{
		this.ll.x = x;
	}

	public double getLLY()
	{
		return this.ll.y;
	}

	public void setLLY( double y)
	{
		this.ll.y = y;
	}

	public double getLLZ()
	{
		return this.ll.z;
	}

	public void setLLZ( double z )
	{
		this.ll.z = z;
	}
}

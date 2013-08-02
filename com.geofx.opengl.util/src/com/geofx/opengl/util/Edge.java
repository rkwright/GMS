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

import com.geofx.opengl.util.Constants;
import com.geofx.opengl.util.Edge;
import com.geofx.opengl.util.PathElm;
import com.geofx.opengl.util.PathElm.PathType;

/**
 * This class encapsulate the necessary functionality for a single edge, which is 
 * composed of two PathElms.
 * 
 * @author riwright
 *
 */
public class Edge
{
	public PathElm	p0;
	public PathElm 	p1;
	public double	angle = 0.0;
	
	// intersection vars
	PathElm    	u = new PathElm();
	PathElm    	v = new PathElm();
	PathElm    	w = new PathElm();
	PathElm 	w2 = new PathElm();

	public Edge()
	{	
		this.p0 = new PathElm();
		this.p1 = new PathElm();
	}
	
	/**
	 * Constructor that takes a pair of PathElms
	 * 
	 * @param p0
	 * @param p1
	 */
	public Edge ( PathElm p0, PathElm p1 )
	{
		set( p0, p1 );
	}

	/**
	 * This is a copy ctor which makes a deep copy of the edge
	 * @param edge
	 */
	public Edge ( Edge edge )
	{
		this.p0 = new PathElm(edge.getP0().x, edge.getP0().y, edge.getP0().type);
		this.p1 = new PathElm(edge.getP1().x, edge.getP1().y, edge.getP1().type);
		setAngle();
	}

	/**
	 * Simple routine that just sets the path elms and updates the angle
	 * @param p0
	 * @param p1
	 */
	public void set( PathElm p0, PathElm p1 )
	{
		this.p0.set(p0);
		this.p1.set(p1);
		setAngle();
	}

	public void set( Edge edge )
	{
		set(edge.p0, edge.p1);
	}

	public void clear()
	{
		this.p0.clear();
		this.p1.clear();
		this.angle = 0.0;		
	}

	public boolean isClear()
	{
		return (this.p0.isClear() && this.p1.isClear());		
	}

	/**
	 * Compute the angle of the segment described by the four 
	 * points of the PathElms
	 * 
	 * @param elm
	 * @param nextElm
	 * @return
	 */
	public void setAngle ()
	{
		if (p0 != null && p1 != null)
			angle = Math.atan2(p1.y-p0.y, p1.x-p0.x);
	}

	/**
	 * Computes whether the angle formed by this edge and the specified edge 
	 * is obtuse.  Note that this implies that the two edges share a point,
	 * i.e. p1 of this edge is p0 of the supplied edge.  If this is not
	 * true the routine throws and exception.  The points do not have to 
	 * be the same object, but they do have to be very very close in space.
	 * This is in terms of the left-hand of the edges, i.e. going from 
	 * p0..p1 of this and p0..p1 of the supplied angle.  
	 * 
	 * @param e2
	 * @return
	 */
	public boolean isObtuse ( Edge e1 )
	{		
		double inclAng = getIncludeAngle( e1);
		
		return inclAng < Math.PI;
	}

	/**
	 * This func accepts 2 REAL arguments and returns a REAL which is 
	 * the difference (in counter-clockwise direction) between the 
	 * arctangents of the lines described by the angles.
	 * 
	 * @param atan1
	 * @param atan2
	 * @return
	 */
	private double getIncludeAngle ( double atan1, double atan2 )
	{         
	  if (atan1 < atan2 )  
		  return atan1 - atan2 + Constants.TWO_PI;
	  else
	      return atan1 - atan2;
	  }

	public double getIncludeAngle ( Edge e1 )
	{     
		if ( !this.p1.equals(e1.p0) )
			throw new RuntimeException("Points p1 and p0 of the edges are not the same!");
		
		return getIncludeAngle(angle, e1.getAngle());
	  }

/*
 * Routine to calculate the dot product for 2D vectors
 */
public double dot (PathElm u, PathElm v )
{
	return   u.x * v.x + u.y * v.y; 
}

/*
 * Routine to calculate the "cross product" for 2D vectors
 */
public double perp (PathElm u,PathElm v)
{
	return u.x * v.y - u.y * v.x;
}

/**
 * Routine to find the intersection between this object-edge and the specified edge,
 * if any.  Handles all possible cases, including parallel edges, edges which intersect 
 * only at their tips, where the edge is simple a point and so on.
 * 
 * Algorithm derived from code in Graphics Gems IV.
 * 
 * Ported to Java by Ric Wright, July 2008
 */
public boolean intersect( Edge edge2, PathElm intersect )
{
	final double 		SMALL_NUM = 0.0000001;
	PathElm.PathType	undefined = PathElm.PathType.undefined;
	
	// should allocate these once.  FIXME
	u.set(this.p1.x - this.p0.x, this.p1.y - this.p0.y, undefined);
	v.set(edge2.p1.x - edge2.p0.x, edge2.p1.y - edge2.p0.y, undefined);
	w.set(this.p0.x - edge2.p0.x, this.p0.y - edge2.p0.y, undefined);
	double    	D = perp(u,v);
	
	intersect.clear();
	
	// test if they are parallel (includes either being a point)
	if (Math.abs(D) < SMALL_NUM) 	// this edge and edge2 are parallel
	{        
		if (perp(u,w) != 0 || perp(v,w) != 0) // they are NOT collinear
			return false;           	
		
		// they are collinear or degenerate, so  check if they are degenerate points
		double du = dot(u,u);
		double dv = dot(v,v);
		if (Math.abs(du) < Constants.FP_TOLERANCE && Math.abs(dv) < Constants.FP_TOLERANCE)            // both segments are points
		{
			if (!this.p0.equals(edge2.p0))        // they are distinct points
				return false;
			
			 // they are the same point
			intersect.set(this.p0.x, this.p0.y, PathType.moveto);   
			return true;
		}
		
		if (Math.abs(du) < Constants.FP_TOLERANCE) // this edge is a single point
		{                    
			if (inEdge(this.p0, edge2) == 0)  // but is not in edge2
				return false;
			
			intersect.set(this.p0.x, this.p0.y, PathType.moveto); 
			return true;
		}
		
		if (Math.abs(dv) < Constants.FP_TOLERANCE)   // edge2 a single point
		{                   
			if (inEdge(edge2.p0, this) == 0)  // but is not in this edge
				return false;
			
			intersect.set(edge2.p0.x, edge2.p0.y, PathType.moveto);
			return true;
		}
		
		// they are collinear segments - get overlap, if it exists
		double 		t0, t1;                   // endpoints of this edge in eqn for edge2
		w2.set(this.p1.x - edge2.p0.x, this.p1.y - edge2.p0.y, undefined);
		
		if (v.x != 0) 
		{
			t0 = w.x / v.x;
			t1 = w2.x / v.x;
		}
		else 
		{
			t0 = w.y / v.y;
			t1 = w2.y / v.y;
		}
		
		if (t0 > t1)     // must have t0 smaller than t1 - swap 'em if not
		{   
			double t=t0; 
			t0=t1; 
			t1=t;    
		}
		
		// Edges are colinear but do not overlap
		if (t0 > 1 || t1 < 0) 
		{
			return false;     // NO overlap
		}
		
		t0 = t0 < 0 ? 0 : t0; 	// clip to min 0
		t1 = t1 > 1 ? 1 : t1;   // clip to max 1
		
		if (t0 == t1) 		// intersection is a point
		{ 
			intersect.set((edge2.p0.x + t0 * v.x), (edge2.p0.y + t0 * v.y), PathType.moveto); 
			return true;
		}

	  // they overlap in a valid subsegment so the intersection is a line segment
	 // PathElm intersection2 = new PathElm(edge2.p0.x + t0 * v.x, edge2.p0.x + t0 * v.x, undefined);
		
	  // but we return only one object.
		intersect.set((edge2.p0.x + t1 * v.x), (edge2.p0.y + t1 * v.y), PathType.moveto);
		return true;
	}

	// the segments are skewed and may intersect in a point
	// get the intersect parameter for this edge
	double     sI = perp(v,w) / D;
	if (sI < 0 || sI > 1)               // no intersection with this edge
	{
		return false;
	}
	
	// get the intersect parameter for S2
	double     tI = perp(u,w) / D;
	if (tI < 0 || tI > 1)               // no intersection with this edge
	{
		return false;
	}
	
	intersect.set((this.p0.x + sI * u.x),( this.p0.y + sI * u.y), PathType.moveto);
	return true;
}
//===================================================================

/**
 * inSegment(): determine if a point is inside a segment. 
 * Input:  a point P, and a collinear segment S
 *  Return: 1 = P is inside S
 *       0 = P is not inside S
 */
	public int inEdge(PathElm P, Edge S)
	{
		if (S.p0.x != S.p1.x)  // S is not vertical
		{ 
			if (S.p0.x <= P.x && P.x <= S.p1.x)
				return 1;
			if (S.p0.x >= P.x && P.x >= S.p1.x)
				return 1;
		}
		else  // S is vertical, so test y coordinate
		{ 
			if (S.p0.y <= P.y && P.y <= S.p1.y)
				return 1;
			if (S.p0.y >= P.y && P.y >= S.p1.y)
				return 1;
		}
		return 0;
	}
//======================================================================
//------------------------ getters and setters -------------------------
	protected double getAngle()
	{
		return angle;
	}

	public PathElm getP0()
	{
		return p0;
	}

	public void setP0(PathElm p0)
	{
		this.p0.set(p0);
		setAngle();
	}

	public PathElm getP1()
	{
		return p1;
	}

	public void setP1(PathElm p1)
	{
		this.p1.set(p1);
		setAngle();
	}

	@Override
	public String toString()
	{
		return "p0: " + this.p0 +  " p1: " + this.p1 + " " ;
	}

}

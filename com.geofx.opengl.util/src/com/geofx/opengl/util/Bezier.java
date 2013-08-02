//----------------------------------------------------------------------------
// Anti-Grain Geometry (AGG) - Version 2.5
// A high quality rendering engine for C++
// Copyright (C) 2002-2006 Maxim Shemanarev
// Contact: mcseem@antigrain.com
//          mcseemagg@yahoo.com
//          http://antigrain.com
// 
// AGG is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
// 
// AGG is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
//----------------------------------------------------------------------------

package com.geofx.opengl.util;

import java.util.ListIterator;

import com.geofx.opengl.util.CubicElm;
import com.geofx.opengl.util.PathElm;
import com.geofx.opengl.util.Pool;
import com.geofx.opengl.util.QuadElm;
import com.geofx.opengl.util.PathElm.PathType;


public class Bezier
{
	
    static double 	CURVE_DISTANCE_EPSILON     		= 1e-30;
    static double 	CURVE_COLINEARITY_EPSILON       = 1e-30;
    static double 	CURVE_ANGLE_TOLERANCE_EPSILON   = 0.01;
    static int    	CURVE_RECURSION_LIMIT 			= 32;
    
   	private double 					approximationScale = 1.0;
	private double 					distanceToleranceSq = 0.5;
	private double 					angleTolerance = 0.25;
	private double 					cuspLimit = 0.1;

    private double					flatness = 0.1;
    private QuadFlattener			quadFlattener = new QuadFlattener();
    private CubicFlattener			cubicFlattener = new CubicFlattener();
    private ListIterator<PathElm> 	iter = null;
	private Pool 					pool;
     
	/**
	 * Simple constructor.  Doesn't actually do anything but call init
	 * so flatness dependent values are updated.
	 */
    public Bezier( Pool pool )
    {    	
    	this.pool = pool;
    	init();
    }
    
    /**
     * This is the main entrance point for the flattener.  It assumes that the
     * elm passed in IS a curve element.  Else it throws an exception.
     * 
     * @param iter
     * @param curPt
     * @param curvElm
     * @return
     */
	public PathElm flattenCurve( ListIterator<PathElm> iter, PathElm curPt, PathElm  curvElm )
	{
		this.iter = iter;
		
		if (curvElm.type == PathType.cubicto)
			return cubicFlattener.flatten(curPt, (CubicElm) curvElm );
		else
			if (curvElm.type == PathType.quadto)
				return quadFlattener.flatten(curPt, (QuadElm) curvElm );
			else
				throw new RuntimeException("Element passed to flattener in not a curve - it is of type " + curvElm.type + " !!");
		
	}

	/**
	 *  All this does is set the squared tolerance value
	 */
	private void init() 
	{
		distanceToleranceSq = Math.pow(flatness / approximationScale, 2.0);
 	}
	
	public void setFlatness(double flatness)
	{
		this.flatness = flatness;
		// reset the flatness dependent values
		init();
	}

	public PathElm insertLineTo(double x, double y)
	{
		//System.out.println("Adding lineto: " + String.format("%6.2f",x) + " " + String.format("%6.2f",y) + " len: " + Math.hypot(x,y));
		PathElm elm = pool.newPathElm(x,y, PathType.lineto);
		iter.add(elm);
		
		return elm;
	}
	
	public void setApproximationScale(double s)
	{
		approximationScale = s;
	}

	public double getApproximationScale()
	{
		return approximationScale;
	}

	public void setAngleTolerance(double a)
	{
		angleTolerance = a;
	}

	public double getAngleTolerance()
	{
		return angleTolerance;
	}

	public void setCuspLimit(double v)
	{
		cuspLimit = (v == 0.0) ? 0.0 : Math.PI - v;
	}

	public double getCuspLimit()
	{
		return (cuspLimit == 0.0) ? 0.0 : Math.PI - cuspLimit;
	}

	  /*
	   * Catmull-Rom conversion to Bezier in place
	   */
    public static void CatRomToBezier( PathElm elm, CubicElm cubic )						
    {
        // Trans. matrix Catmull-Rom to Bezier
        //
        //  0       1       0       0
        //  -1/6    1       1/6     0
        //  0       1/6     1       -1/6
        //  0       0       1       0
        //
    	CubicElm 	c = new CubicElm();
    	PathElm 	e = new PathElm();
    	
        e.x = cubic.x;
        e.y = cubic.y;
        c.x = (-elm.x + 6*cubic.x + cubic.x1) / 6;
        c.y = (-elm.y + 6*cubic.y + cubic.y1) / 6;
        c.x1 = ( cubic.x + 6*cubic.x1 - cubic.x2) / 6;
        c.y1 = ( cubic.y + 6*cubic.y1 - cubic.y2) / 6;
        c.x2 = cubic.x2;
        c.x2 = cubic.y2;
        
        elm.set(e);
        cubic.set(c);
    }

    /**
     *  UBSPline to Bezier conversion in place
     */
    public static void UBSplineToBezier( PathElm elm, CubicElm cubic )
    {
        // Trans. matrix Uniform BSpline to Bezier
        //
        //  1/6     4/6     1/6     0
        //  0       4/6     2/6     0
        //  0       2/6     4/6     0
        //  0       1/6     4/6     1/6
        //
            
      	CubicElm 	c = new CubicElm();
    	PathElm 	e = new PathElm();
  
        e.x = (elm.x + 4*cubic.x + cubic.x1) / 6;
        e.x = (elm.y + 4*cubic.y + cubic.y1) / 6;
        c.x = (4 * cubic.x + 2 * cubic.x1) / 6;
        c.y = (4 * cubic.y + 2 * cubic.y1) / 6;
        c.x1 = (2 * cubic.x + 4 * cubic.x1) / 6;
        c.y1 = (2 * cubic.y + 4 * cubic.y1) / 6;
        c.x2 = (cubic.x + 4*cubic.x1 + cubic.x2) / 6;
        c.y2 = (cubic.y + 4*cubic.y1 + cubic.y2) / 6;
    }

    /**
     * Hermite to Bezier conversion in place
     */
    public static void HermiteToBezier( PathElm elm, CubicElm cubic )
    {
        // Trans. matrix Hermite to Bezier
        //
        //  1       0       0       0
        //  1       0       1/3     0
        //  0       1       0       -1/3
        //  0       1       0       0
        //
     	CubicElm 	c = new CubicElm();
    	PathElm 	e = new PathElm();
  
        e.x = elm.x;
        e.x = elm.y;
        c.x = (3 * elm.x + cubic.x1) / 3;
        c.y = (3 * elm.y + cubic.y1) / 3;
        c.x1 = (3 * cubic.x - cubic.x2) / 3;
        c.y1 = (3 * cubic.y - cubic.y2) / 3;
        c.x2 = cubic.x;
        c.y2 = cubic.y;
    }
	/**
	 *   Simple convenience routine
	 */
	private double getSquaredDistance(double x2, double y2, double x1, double y1)
	{
		return Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2);
	}


	/**
	 *  Class derived from AGG curve3::div that flattens quadratic beziers.
	 */
    private class QuadFlattener
    {
		/**
		 *  This is the entry point for this class.  All it does is call the recursive routine
		 */	
    	public PathElm flatten(PathElm curPt, QuadElm curvElm)
    	{
    		//insert_lineto(x1, y1);
    		recursive_flatten(curPt.x, curPt.y, curvElm.x, curvElm.y, curvElm.x1, curvElm.y1, 0);
    		return insertLineTo(curvElm.x1, curvElm.y1);
    	}
    	
     	/**
    	 * Here's where all the heavy lifting gets done.
     	 */
    	void recursive_flatten(double x1, double y1, double x2, double y2, double x3, double y3, int level)
    	{
    		if (level > CURVE_RECURSION_LIMIT)
    		{
    			return;
    		}

    		// Calculate all the mid-points of the line segments
    		//----------------------
    		double x12 = (x1 + x2) / 2;
    		double y12 = (y1 + y2) / 2;
    		double x23 = (x2 + x3) / 2;
    		double y23 = (y2 + y3) / 2;
    		double x123 = (x12 + x23) / 2;
    		double y123 = (y12 + y23) / 2;

    		double dx = x3 - x1;
    		double dy = y3 - y1;
    		double d = Math.abs(((x2 - x3) * dy - (y2 - y3) * dx));
    		double da;

    		if (d > CURVE_COLINEARITY_EPSILON)
    		{
    			// Regular case
    			//-----------------
    			if (d * d <= distanceToleranceSq * (dx * dx + dy * dy))
    			{
    				// If the curvature doesn't exceed the distance_tolerance value
    				// we tend to finish subdivisions.
    				//----------------------
    				if (angleTolerance < CURVE_ANGLE_TOLERANCE_EPSILON)
    				{
    					insertLineTo(x123, y123);
    					return;
    				}

    				// Angle & Cusp Condition
    				//----------------------
    				da = Math.abs(Math.atan2(y3 - y2, x3 - x2) - Math.atan2(y2 - y1, x2 - x1));
    				if (da >= Math.PI)
    					da = 2 * Math.PI - da;

    				if (da < angleTolerance)
    				{
    					// Finally we can stop the recursion
    					//----------------------
    					insertLineTo(x123, y123);
    					return;
    				}
    			}
    		}
    		else
    		{
    			// Collinear case
    			//------------------
    			da = dx * dx + dy * dy;
    			if (da == 0)
    			{
    				d = getSquaredDistance(x1, y1, x2, y2);
    			}
    			else
    			{
    				d = ((x2 - x1) * dx + (y2 - y1) * dy) / da;
    				if (d > 0 && d < 1)
    				{
    					// Simple collinear case, 1---2---3
    					// We can leave just two endpoints
    					return;
    				}
    				if (d <= 0)
    					d = getSquaredDistance(x2, y2, x1, y1);
    				else if (d >= 1)
    					d = getSquaredDistance(x2, y2, x3, y3);
    				else
    					d = getSquaredDistance(x2, y2, x1 + d * dx, y1 + d * dy);
    			}
    			if (d < distanceToleranceSq)
    			{
    				insertLineTo(x2, y2);
    				return;
    			}
    		}

    		// Continue subdivision
    		//----------------------
    		recursive_flatten(x1, y1, x12, y12, x123, y123, level + 1);
    		recursive_flatten(x123, y123, x23, y23, x3, y3, level + 1);
    	}
    }

    /**
     * Class derived from AGG curve4::div that flattens cubic beziers.
     */
    public class CubicFlattener
    {
       	/**
    	 * This is the entry point for this class.  Just calls the recursive routine.
    	 */
    	private PathElm flatten( PathElm curPt, CubicElm curvElm )
    	{
     		recursive_flatten(curPt.x, curPt.y, curvElm.x, curvElm.y, curvElm.x1, curvElm.y1, curvElm.x2, curvElm.y2, 0);
    		return insertLineTo(curvElm.x2, curvElm.y2);
    	}
    	
    	/**
    	 * Here's where the real heavy=lifting gets done.
      	 */
    	void recursive_flatten(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4,
    			int level)
    	{
    		if (level > CURVE_RECURSION_LIMIT)
    		{
    			return;
    		}

    		// Calculate all the mid-points of the line segments
    		//----------------------
    		double x12 = (x1 + x2) / 2;
    		double y12 = (y1 + y2) / 2;
    		double x23 = (x2 + x3) / 2;
    		double y23 = (y2 + y3) / 2;
    		double x34 = (x3 + x4) / 2;
    		double y34 = (y3 + y4) / 2;
    		double x123 = (x12 + x23) / 2;
    		double y123 = (y12 + y23) / 2;
    		double x234 = (x23 + x34) / 2;
    		double y234 = (y23 + y34) / 2;
    		double x1234 = (x123 + x234) / 2;
    		double y1234 = (y123 + y234) / 2;

    		// Try to approximate the full cubic curve by a single straight line
    		//------------------
    		double dx = x4 - x1;
    		double dy = y4 - y1;

    		double d2 = Math.abs(((x2 - x4) * dy - (y2 - y4) * dx));
    		double d3 = Math.abs(((x3 - x4) * dy - (y3 - y4) * dx));
    		double da1, da2, k;

    		int d2Val = d2 > CURVE_COLINEARITY_EPSILON ? 2 : 0;
    		int d3Val = d3 > CURVE_COLINEARITY_EPSILON ? 1 : 0;

    		switch (d2Val + d3Val)
    		{
    			case 0:
    				// All collinear OR p1==p4
    				//----------------------
    				k = dx * dx + dy * dy;
    				if (k == 0)
    				{
    					d2 = calc_sq_distance(x1, y1, x2, y2);
    					d3 = calc_sq_distance(x4, y4, x3, y3);
    				}
    				else
    				{
    					k = 1 / k;
    					da1 = x2 - x1;
    					da2 = y2 - y1;
    					d2 = k * (da1 * dx + da2 * dy);
    					da1 = x3 - x1;
    					da2 = y3 - y1;
    					d3 = k * (da1 * dx + da2 * dy);
    					if (d2 > 0 && d2 < 1 && d3 > 0 && d3 < 1)
    					{
    						// Simple collinear case, 1---2---3---4
    						// We can leave just two endpoints
    						return;
    					}
    					if (d2 <= 0)
    						d2 = calc_sq_distance(x2, y2, x1, y1);
    					else if (d2 >= 1)
    						d2 = calc_sq_distance(x2, y2, x4, y4);
    					else
    						d2 = calc_sq_distance(x2, y2, x1 + d2 * dx, y1 + d2 * dy);

    					if (d3 <= 0)
    						d3 = calc_sq_distance(x3, y3, x1, y1);
    					else if (d3 >= 1)
    						d3 = calc_sq_distance(x3, y3, x4, y4);
    					else
    						d3 = calc_sq_distance(x3, y3, x1 + d3 * dx, y1 + d3 * dy);
    				}
    				if (d2 > d3)
    				{
    					if (d2 < distanceToleranceSq)
    					{
    						insertLineTo(x2, y2);
    						return;
    					}
    				}
    				else
    				{
    					if (d3 < distanceToleranceSq)
    					{
    						insertLineTo(x3, y3);
    						return;
    					}
    				}
    				break;

    			case 1:
    				// p1,p2,p4 are collinear, p3 is significant
    				//----------------------
    				if (d3 * d3 <= distanceToleranceSq * (dx * dx + dy * dy))
    				{
    					if (angleTolerance < CURVE_ANGLE_TOLERANCE_EPSILON)
    					{
    						insertLineTo(x23, y23);
    						return;
    					}

    					// Angle Condition
    					//----------------------
    					da1 = Math.abs(Math.atan2(y4 - y3, x4 - x3) - Math.atan2(y3 - y2, x3 - x2));
    					if (da1 >= Math.PI)
    						da1 = 2 * Math.PI - da1;

    					if (da1 < angleTolerance)
    					{
    						insertLineTo(x2, y2);
    						insertLineTo(x3, y3);
    						return;
    					}

    					if (cuspLimit != 0.0)
    					{
    						if (da1 > cuspLimit)
    						{
    							insertLineTo(x3, y3);
    							return;
    						}
    					}
    				}
    				break;

    			case 2:
    				// p1,p3,p4 are collinear, p2 is significant
    				//----------------------
    				if (d2 * d2 <= distanceToleranceSq * (dx * dx + dy * dy))
    				{
    					if (angleTolerance < CURVE_ANGLE_TOLERANCE_EPSILON)
    					{
    						insertLineTo(x23, y23);
    						return;
    					}

    					// Angle Condition
    					//----------------------
    					da1 = Math.abs(Math.atan2(y3 - y2, x3 - x2) - Math.atan2(y2 - y1, x2 - x1));
    					if (da1 >= Math.PI)
    						da1 = 2 * Math.PI - da1;

    					if (da1 < angleTolerance)
    					{
    						insertLineTo(x2, y2);
    						insertLineTo(x3, y3);
    						return;
    					}

    					if (cuspLimit != 0.0)
    					{
    						if (da1 > cuspLimit)
    						{
    							insertLineTo(x2, y2);
    							return;
    						}
    					}
    				}
    				break;

    			case 3:
    				// Regular case
    				//-----------------
    				if ((d2 + d3) * (d2 + d3) <= distanceToleranceSq * (dx * dx + dy * dy))
    				{
    					// If the curvature doesn't exceed the distance_tolerance value
    					// we tend to finish subdivisions.
    					//----------------------
    					if (angleTolerance < CURVE_ANGLE_TOLERANCE_EPSILON)
    					{
    						insertLineTo(x23, y23);
    						return;
    					}

    					// Angle & Cusp Condition
    					//----------------------
    					k = Math.atan2(y3 - y2, x3 - x2);
    					da1 = Math.abs(k - Math.atan2(y2 - y1, x2 - x1));
    					da2 = Math.abs(Math.atan2(y4 - y3, x4 - x3) - k);
    					if (da1 >= Math.PI)
    						da1 = 2 * Math.PI - da1;
    					if (da2 >= Math.PI)
    						da2 = 2 * Math.PI - da2;

    					if (da1 + da2 < angleTolerance)
    					{
    						// Finally we can stop the recursion
    						//----------------------
    						insertLineTo(x23, y23);
    						return;
    					}

    					if (cuspLimit != 0.0)
    					{
    						if (da1 > cuspLimit)
    						{
    							insertLineTo(x2, y2);
    							return;
    						}

    						if (da2 > cuspLimit)
    						{
    							insertLineTo(x3, y3);
    							return;
    						}
    					}
    				}
    				break;
    		}

    		// Continue subdivision
    		//----------------------
    		recursive_flatten(x1, y1, x12, y12, x123, y123, x1234, y1234, level + 1);
    		recursive_flatten(x1234, y1234, x234, y234, x34, y34, x4, y4, level + 1);
    	}

    	private double calc_sq_distance(double x2, double y2, double x1, double y1)
    	{
    		return Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2);
    	}
    }
    
  
}

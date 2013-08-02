/*******************************************************************************
 * Copyright (c) 2008 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 ****************************************************************************/

package com.geofx.opengl.util;

import com.geofx.opengl.util.Constants;
import com.geofx.opengl.util.PathElm;


public class PathElm
{
	public enum  PathType { moveto, lineto, cubicto, quadto, closepath, undefined };
	
    public double       x = Constants.MAX_FLOAT;  
    public double		y = Constants.MAX_FLOAT;
    public PathType     type = PathType.undefined;
    
    public PathElm ()
    {
    }
  
    public PathElm (double x, double y, PathType type)
    {	
    	this.x = x;
    	this.y = y;
    	this.type = type;
    }
    
    public PathElm (float x, float y, PathType type)
    {      
       	this.x = (double) x;
    	this.y = (double) y;
    	this.type = type;
    }
  
    public PathElm ( PathElm p )
    {        
       	this.x = p.x;
    	this.y = p.y;
    	this.type = p.type;
    }
 
    public void set ( PathElm p )
    {
    	this.x = p.x;
    	this.y = p.y;
    	this.type = p.type;
    }

    public void set(double x, double y, PathType type)
    {
    	this.x = x;
    	this.y = y;
       	this.type = type;
    }

    public void set(float x, float y, PathType type)
    {
    	this.x = (double)x;
    	this.y = (double)y;
       	this.type = type;
    }

    public void clear()
    {
    	this.x = Constants.MAX_FLOAT;
    	this.y = Constants.MAX_FLOAT;
       	this.type = PathType.undefined;
    }

    public boolean isClear()
    {
    	return this.type == PathType.undefined;
    }

    public static PathType convert( int i ) 
    {
    	for ( PathType current : PathType.values() ) 
    	{
	    	if ( current.ordinal() == i ) 
	    	{
	    		return current;
	    	}
    	}
    	return PathType.undefined;
    }
    
    public static String getLabel ( PathType type )
    {   	
   		for ( PathType current : PathType.values() ) 
   		{
    		if ( current == type ) 
    		{
    			return current.toString();
    		}
    	}
    	return PathType.undefined.toString();
    }

    public static PathType getType ( String str )
    {   	
   		for ( PathType current : PathType.values() ) 
   		{
    		if ( current.toString().compareTo(str) == 0 ) 
    		{
    			return current;
    		}
    	}
    	return PathType.undefined;
    }

    public boolean equals ( PathElm p )
    {
    	return (Math.abs(this.x - p.x) < Constants.FP_TOLERANCE) && ( Math.abs(this.y - p.y) < Constants.PATH_TOLERANCE);
    }

    public boolean equals ( float x, float y )
    {
    	return (Math.abs(this.x - x) < Constants.FP_TOLERANCE) && ( Math.abs(this.y - y) < Constants.PATH_TOLERANCE);
    }

    public boolean equals ( double x, double y )
    {
    	return (Math.abs(this.x - x) < Constants.FP_TOLERANCE) && ( Math.abs(this.y - y) < Constants.PATH_TOLERANCE);
    }
   
    @Override
    public String toString()
    {
    	return " x,y: " + String.format("%8.4f",x) + " " + String.format("%8.4f",y) + " " + type;
    }
 }

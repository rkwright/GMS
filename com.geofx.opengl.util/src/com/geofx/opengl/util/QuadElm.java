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
import com.geofx.opengl.util.QuadElm;



public class QuadElm extends PathElm
{
    public double      	x1;  
    public double		y1;
  
    public QuadElm ()
    {
    	clear();
    }
  
    public QuadElm (double x, double y, double x1, double y1, PathType type)
    {
    	this.x  = x;
    	this.y  = y;
    	this.x1 = x1;
    	this.y1 = y1;
    	this.type = type;
    }
  
    public QuadElm ( QuadElm p )
    {
    	this.x  = p.x;
    	this.y  = p.y;
       	this.x1 = p.x1;
    	this.y1 = p.y1;
    	this.type = p.type;
    }
 
    public void set ( QuadElm p )
    {
    	this.x  = p.x;
    	this.y  = p.y;
      	this.x1 = p.x1;
    	this.y1 = p.y1;
    	this.type = p.type;
    }

    public void set(double x, double y, double x1, double y1, PathType type)
    {
    	this.x  = x;
    	this.y  = y;
       	this.x1 = x1;
    	this.y1 = y1;
        	this.type = type;
    }

    public void clear()
    {
    	this.x  = Constants.MAX_FLOAT;
    	this.y  = Constants.MAX_FLOAT;
       	this.x1 = Constants.MAX_FLOAT;
    	this.y1 = Constants.MAX_FLOAT;
      	this.type = PathType.undefined;
    }

    public boolean isClear()
    {
    	return (Math.abs(this.x - Constants.MAX_FLOAT) < Constants.FP_TOLERANCE) && 
    	       (Math.abs(this.y - Constants.MAX_FLOAT) < Constants.FP_TOLERANCE) &&
    	       (Math.abs(this.x1 - Constants.MAX_FLOAT) < Constants.FP_TOLERANCE) && 
    	       (Math.abs(this.y1 - Constants.MAX_FLOAT) < Constants.FP_TOLERANCE) &&
     	       this.type == PathType.undefined;
    }

 
    public boolean equals ( QuadElm p )
    {
    	return (Math.abs(this.x - p.x) < Constants.FP_TOLERANCE) && ( Math.abs(this.y - p.y) < Constants.FP_TOLERANCE &&
    			Math.abs(this.x1 - p.x1) < Constants.FP_TOLERANCE) && ( Math.abs(this.y1 - p.y1) < Constants.FP_TOLERANCE );
    }
    
    @Override
    public String toString()
    {
    
    	return " x,y: " + String.format("%6.2f",x) + " " + String.format("%6.2f",y) + 
    			" x1,y1: " + String.format("%6.2f",x1) + " " + String.format("%6.2f",y1);
    }
}

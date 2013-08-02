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

public class LineJoin
{
	public enum JoinType { Bevel, Miter, Round };
	
 	private double 		miterLimit = Math.PI - Math.toRadians(15.0);
	public JoinType     type = JoinType.Bevel;
    
    public static JoinType convert( int i ) 
    {
    	for ( JoinType current : JoinType.values() ) 
    	{
	    	if ( current.ordinal() == i ) 
	    	{
	    		return current;
	    	}
    	}
    	return JoinType.Bevel;
    }
    
    public static String getLabel ( JoinType type )
    {   	
   		for ( JoinType current : JoinType.values() ) 
   		{
    		if ( current == type ) 
    		{
    			return current.toString();
    		}
    	}
    	return JoinType.Bevel.toString();
    }

    /*
     * Note that internally we store miterLimit as PI - miterLimit
     * This is because that is how it is used.  See MiterJoin in PSGraphics
     */
	public double getMiterLimit()
	{
		return miterLimit + Math.PI;
	}

	public void setMiterLimit(double miterLimit)
	{
		this.miterLimit = Math.PI - miterLimit;
	}

	public JoinType getType()
	{
		return type;
	}

	public void setType(JoinType type)
	{
		this.type = type;
	}
}

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


public class LineCap
{
	public enum CapType { Butt, Project, Round };
	
    public CapType     type = CapType.Butt;
    
    public static CapType convert( int i ) 
    {
    	for ( CapType current : CapType.values() ) 
    	{
	    	if ( current.ordinal() == i ) 
	    	{
	    		return current;
	    	}
    	}
    	return CapType.Butt;
    }
    
    public static String getLabel ( CapType type )
    {   	
   		for ( CapType current : CapType.values() ) 
   		{
    		if ( current == type ) 
    		{
    			return current.toString();
    		}
    	}
    	return CapType.Butt.toString();
    }

	public CapType getType()
	{
		return type;
	}

	public void setType(CapType type)
	{
		this.type = type;
	}
}

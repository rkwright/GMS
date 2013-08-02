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

import com.geofx.opengl.util.Dash;


public class Dash
{
	public double	length;		// length of this dash element
	public boolean	gap;		// true if this is a gap, not a rendered dash

	public Dash ( double length, boolean gap )
	{
		this.length = length;
		this.gap = gap;
	}

	/**
	 *  Copy constructor 
	 */
	public Dash(Dash dash)
	{
		this.length= dash.length;
		this.gap = dash.gap;
	}
	
	@Override
	public String toString()
	{
		return "length: " + this.length + " gap: " + this.gap ;
	}
}

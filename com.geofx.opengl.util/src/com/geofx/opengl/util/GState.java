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

import java.util.LinkedList;

import com.geofx.opengl.util.CTM;
import com.geofx.opengl.util.DashOp;
import com.geofx.opengl.util.LineCap;
import com.geofx.opengl.util.LineJoin;
import com.geofx.opengl.util.PSGraphics;
import com.geofx.opengl.util.PathElm;

public class GState
{
	public double			lineWidth = 0.0;
	public LineCap			lineCap = new LineCap();
	public LineJoin			lineJoin = new LineJoin();
	public DashOp			dashOp = null;
	public CTM				ctm = null;
	
	public LinkedList<PathElm> 	currentPath = null;
	public PathElm				currentPoint = new PathElm();

	public GState ( PSGraphics ps )
	{
		this.lineWidth = ps.currentlinewidth();
		this.lineCap.type = ps.currentlinecap();
		this.lineJoin.type = ps.currentlinejoin();
		dashOp = new DashOp( ps.getPool(), ps.currentdash());
		this.ctm = ps.currentmatrix();
	}
}

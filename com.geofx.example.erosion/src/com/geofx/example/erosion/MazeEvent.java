/*
 * Copyright (c) 2013 Ric Wright 
 *
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 */

package com.geofx.example.erosion;

/**
 * @author rkwright
 *
 */
public interface MazeEvent
{
	public void report ( String description, int posy, int posx, int msy, int msx, int stackDepth, boolean bSac);

}

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

public interface DataIteratorXYZ
{
	/**
	 * 
	 */
	public boolean 	hasNext();
	
	/**
	 * 
	 */
	public double 	getX();

	/**
	 * 
	 */
	public double 	getY();

	/**
	 * 
	 */
	public double 	getZ();

	/**
	 * 
	 */
	public void 	reset();
	
	/**
	 *
	 */
	public void 	next();
}

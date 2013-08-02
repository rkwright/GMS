/*******************************************************************************
 * Copyright (c) 2009 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 *******************************************************************************/

package com.geofx.gms.datasets;

import com.geofx.opengl.util.Cuboid;

public interface IGMSModel
{
	/**
	 * Request the current cuboid whose dimenstions bound the model's view
	 */
	public Cuboid 	getView( String viewID );
	
	/**
	 * Request to the model to update it's internal model, if needed.  For static
	 * models this does nothing.
	 */
	public double 	update();


	/**
	 * Requests that the model be reset to the starting parameters.
	 */
	public void 	reset();
	
	/**
	 *
	 */
	public void next();
}

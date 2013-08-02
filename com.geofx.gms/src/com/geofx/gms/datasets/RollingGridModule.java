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

import com.geofx.gms.model.Module;

/**
 * @author riwright
 *
 */
public class RollingGridModule extends Module
{	
	protected RollingGrid 	rollingGrid = new RollingGrid();
	// these have to be public because they are accessed at runtime by reflection
	public int				size;
	public double			xScale;
	
	/**
	 * @see com.geofx.gms.model.Module#reset()
	 */
	@Override
	public void reset()
	{
		rollingGrid.reset();
		rollingGrid.setLastModified(clock.peekTime());
	}

	/** 
	 * @see com.geofx.gms.model.Module#update()
	 */
	@Override
	public boolean update()
	{
		if (super.update())
		{
			//System.out.println("Updating RollingGridModule");

			rollingGrid.add(inputs.get(0).getDataset());
			rollingGrid.setLastModified(clock.peekTime());

			return true;
		}
		else
			return false;
	}

	@Override
	public void initDatasets()
	{	
		super.initDatasets();
		
		rollingGrid.setSize(size);

		rollingGrid.setLastModified(0.0);
		
		outputs.get(0).setDataset(rollingGrid);
	}

}

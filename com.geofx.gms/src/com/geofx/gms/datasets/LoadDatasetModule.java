/*******************************************************************************
 * Copyright (c) 2008-9 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 *******************************************************************************/
package com.geofx.gms.datasets;

import java.util.ArrayList;

import javax.vecmath.Matrix4d;

import com.geofx.gms.controller.Clock;
import com.geofx.gms.datasets.Dataset.DatasetType;
import com.geofx.gms.model.Input;
import com.geofx.gms.model.Module;
import com.geofx.gms.model.Output;

/**
 *  Class which implements support for grids, i.e. those data sets that consist
 *  of rectangular grids.  Can be of any dimension, though NxM (two-dimensional) is most
 *  common. In contrast to UniformGrids, a grid need not be uniform (and usually isn't).  
 *  In consequence, the grid's objects either are non-spatial (depending on an associated 
 *  UniformGrid for location information, or the objects themselves contain the coordinate info.
 */
public class LoadDatasetModule extends Module
{
	
	public LoadDatasetModule()
	{
		super();
	}
	
	public ArrayList<Dataset> createOutputs()
	{
		Dataset grid = new Grid();
		ArrayList<Dataset> datasets = new ArrayList<Dataset>();
		
		datasets.add(grid);
		
		return datasets;
	}
	
	@Override
	public Matrix4d getView(String viewID)
	{
		return ctm;
	}
	
	@Override
	public void init()
	{
	}
	
	@Override
	public void reset()
	{
		initDatasets();
	}
	
	@Override
	public boolean update()
	{
		return super.update();
	}

	@Override
	public void initDatasets()
	{
		super.initDatasets();
		
		Input  	input = inputs.get(0);
		Output	output = outputs.get(0);
		
		if (input.getType().equals(DatasetType.Grid.toString()))
		{
			// load the dataset from the file
			Grid grid = Grid.load(input.getFilePath());
			if (grid == null)
			{
				System.err.println("Could not open " + input.getFilePath());
				return;
			}
			
			grid.setLastModified(clock.peekTime());
			
			// then set it as the output dataset for this module
			output.setDataset(grid);
			
			// set the clock to the far future so the first pass gets updated
			grid.setLastModified(Double.MAX_VALUE);

			// this modules is special in that its input is effectively the same as its output
			input.setDataset(grid);
			// and it has no ancestors, so set the module to null
			input.setModule(null);
		}
	}
}

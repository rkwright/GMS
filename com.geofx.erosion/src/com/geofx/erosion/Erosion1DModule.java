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

package com.geofx.erosion;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import com.geofx.gms.datasets.ClassUtil.ClassType;
import com.geofx.gms.datasets.Grid;
import com.geofx.gms.model.Input;
import com.geofx.gms.model.Module;

/**
 * @author rkwright
 *
 */
public class Erosion1DModule extends Module
{
	public double		erosivity = 0.01;
	public double[]		cells;
	public double[]		edges;
	public double[]		position;
	protected Grid		cellGrid;
	protected Grid		posGrid;
	protected int 		nCells;
	private double 		tot0;
	private double 		totLen;

	@Override
	public void reset()
	{
		initDatasets();
	}

	@Override
	public boolean update()
	{
		super.update();
		
		{
			// System.out.println("Updating TempWaveModule");
			updateCells();
			cellGrid.setLastModified(clock.peekTime());

			return true;
		}
	//	else
	//		return false;
	}

	/**
	 * 
	 */
	private void updateCells()
	{
		erodeGrid(cells, edges);
		
		tot0 += cells[0];
		totLen += cells[cells.length-1];

		cells[0] = 0.0;
		cells[cells.length-1] = 0.0;
		
		//dumpState(cells, (int) clock.peekTime());
	}

	@Override
	public void initDatasets()
	{
		super.initDatasets();
		
		Input	input = inputs.get(0);
		cellGrid = (Grid) input.getDataset();
		if (cellGrid == null)
		{
			System.err.println("Unable to load input dataset: " + inputs.get(0).getHRef());
			return;
		}

		// set the clock to the far future so the first pass gets updated
		cellGrid.setLastModified(0.0);

		cells = (double[]) cellGrid.getArray();
		nCells = Array.getLength(cells) - 1;
		edges = new double[nCells + 1];
		
		posGrid = new Grid( ClassType.Double, nCells+1, null);
		position = (double[]) posGrid.getArray();
		for ( int i=0; i<nCells+1; i++ )
			position[i] = i;
		
		Field fieldPtr;
		try
		{
			fieldPtr = getClass().getDeclaredField("cells");
			fieldPtr.set(this, cellGrid.getArray());
			
			outputs.get(0).setDataset(cellGrid);
			outputs.get(1).setDataset(posGrid);
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param cells
	 */
	private  void erodeGrid( double[] cells, double[] edges )
	{	
		for ( int n=0; n<nCells; n++ )
		{
			erodeCell(cells, edges, n);
		}
		
		// Sum up the initial pass of the RK cycle.  cells[*][0] holds the current real value
		for ( int n=0; n<nCells; n++ )
		{
			routErosion(cells, edges, n);
		}
	}

	/**
	 * 
	 * @param cells
	 * @param n
	 * @param rkStep
	 */
	private  void erodeCell( double[] cells, double[] edges, int n )
	{		
		double y = cells[n];
		double ny = cells[n+1];

		double k1 = eDeriv(y, ny, 1.0);
		double k2 = eDeriv(y + k1, ny, 2.0);
		double k3 = eDeriv(y + k2, ny, 2.0);
		double k4 = eDeriv(y + k3, ny, 1.0);

		edges[n] = k1/6.0 + k2/3.0 + k3/3.0 + k4/6.0;
	}

	private  void routErosion(double[] cells, double[] edges, int n)
	{
		cells[n] += edges[n];
		cells[n+1] -= edges[n];
	}


	private  double eDeriv(double cur, double next, double prop)
	{
		return ((next-cur) * erosivity) / prop;
	}
	
	private  void dumpState(double[] cells, int i)
	{
		System.out.printf("%3d: ", i );

		System.out.printf("%8.4f ", tot0);

		for ( int n=1; n<cells.length-1; n++ )
		{
			System.out.printf("%8.4f ", cells[n]);
		}

		System.out.printf("%8.4f ", totLen);

		System.out.printf("\n");
	}

}

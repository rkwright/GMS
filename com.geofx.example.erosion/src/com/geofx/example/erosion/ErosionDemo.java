package com.geofx.example.erosion;
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

public class ErosionDemo
{
	private static final double EROSION_PROP = 0.01;
	private static double west;
	private static double east;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		final int 	NCELLS = 5;
		int 		nStep = 10000;
		GeoCell[]	cells = new GeoCell[NCELLS];

		if (args.length > 0) {}
			
		for (int i=0; i<cells.length; i++ )
		{
			double volume = ( i == 0 || i == (cells.length-1) ) ? 0.0 : 1.0;
			
			cells[i] = new GeoCell( volume, (i == 0) ? null : cells[i-1], null );
		}
				
		for (int i=0; i<nStep; i++)
		{
			dumpState(cells, i);

			erodeGrid( cells );
			
			sumCells( cells );	
		}
		
		dumpFinalState(cells);
	}

	private static void sumCells( GeoCell[] cells )
	{
		west += cells[0].volume;
		east += cells[cells.length-1].volume;

		cells[0].volume = 0.0;
		cells[cells.length-1].volume = 0.0;
	}

	/**
	 * 
	 * @param cells
	 */
	private static void erodeGrid(  GeoCell[] cells )
	{	
		for ( int n=0; n<cells.length; n++ )
		{
			erodeCell(cells[n]);
		}
		
		// Sum up the initial pass of the RK cycle.  cells[*][0] holds the current real value
		for ( int n=0; n<cells.length; n++ )
		{
			routErosion(cells[n]);
		}
	}

	/**
	 * 
	 * @param cells
	 * @param n
	 * @param rkStep
	 */
	private static void erodeCell( GeoCell cell )
	{		
		if (cell.neighbor[GeoCell.EAST] == null)
			return;
		
		double y = cell.volume;
		double ny = cell.neighbor[GeoCell.EAST].volume;

		double k1 = eDeriv(y, ny, 1.0);
		double k2 = eDeriv(y + k1, ny, 2.0);
		double k3 = eDeriv(y + k2, ny, 2.0);
		double k4 = eDeriv(y + k3, ny, 1.0);

		cell.ewFlux = k1/6.0 + k2/3.0 + k3/3.0 + k4/6.0;
	}

	private static void routErosion( GeoCell cell )
	{
		if (cell.neighbor[GeoCell.EAST] == null)
			return;

		cell.volume += cell.ewFlux;
		cell.neighbor[GeoCell.EAST].volume -= cell.ewFlux;
	}

	private static double eDeriv(double cur, double next, double prop)
	{
		return ((next-cur) * EROSION_PROP) / prop;
	}

	private static void dumpState(GeoCell[] cells, int i)
	{
		System.out.printf("%3d: ", i );

		System.out.printf("%8.4f ", west);

		for ( int n=1; n<cells.length-1; n++ )
		{
			System.out.printf("%8.4f ", cells[n].volume);
		}

		System.out.printf("%8.4f ", east);

		System.out.printf("\n");
	}

	private static void dumpFinalState( GeoCell[] cells )
	{
		double nonEroded = 0;
		for ( int n=1; n<cells.length-1; n++ )
		{
			nonEroded += cells[n].volume;
		}

		System.out.printf("nonEroded = %9.5f, totalSystem = %9.5f", nonEroded, nonEroded+west+east );

		System.out.printf("\n");
	}
}

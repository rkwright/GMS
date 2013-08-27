package com.geofx.example.erosion;

public class ErosionDemo2D
{
	private static final double EROSION_PROP = 0.01;
	private static double north[];
	private static double south[];
	private static double east[];
	private static double west[];

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		final int 	NCELLS = 10;
		int 		nStep = 10000;
		GeoCell[][]	cells = null;

		if (args.length > 0) {}
			
		cells = new GeoCell[NCELLS][NCELLS];
		
		north = new double[NCELLS];
		south = new double[NCELLS];
		east = new double[NCELLS];
		west = new double[NCELLS];

		for (int j=0; j<cells[0].length; j++ )
			for (int i=0; i<cells.length; i++ )
			{
				double volume = ( i == 0 || j== 0 || j == cells[0].length-1 || i == (cells[0].length-1) ) ? 0.0 : 1.0;
			
				cells[j][i] = new GeoCell( volume, (i == 0) ? null : cells[j][i-1], (j == 0) ? null : cells[j-1][i] );
			}
				
		for (int i=0; i<nStep; i++)
		{
			dumpState( cells, i);
			
			erodeGrid( cells );
			
			sumCells( cells );		
		}
		
		dumpFinalState(cells);
	}

	private static void sumCells(GeoCell[][] cells)
	{
		for ( int n=0; n<cells.length; n++ )
		{
			north[n] += cells[n][0].volume;
			south[n] += cells[n][cells.length-1].volume;
		}

		for ( int n=0; n<cells.length; n++ )
		{
			west[n] += cells[0][n].volume;
			east[n] += cells[cells.length-1][n].volume;
		}
		
		for ( int n=0; n<cells.length; n++ )
		{
			cells[n][0].volume = 0.0;
			cells[n][cells.length-1].volume = 0.0;
		}

		for ( int n=0; n<cells.length; n++ )
		{
			cells[0][n].volume = 0.0;
			cells[cells.length-1][n].volume = 0.0;	
		}
	}

	/**
	 * 
	 * @param cells
	 */
	private static void erodeGrid( GeoCell[][] cells )
	{	
		for ( int j=0; j<cells.length; j++ )
			for ( int i=0; i<cells.length; i++ )
			{
				erodeCell( cells[j][i] );
			}

		// Sum up the initial pass of the RK cycle.  cells[*][0] holds the current real value
		for ( int j=0; j<cells.length-1; j++ )
			for ( int i=0; i<cells.length-1; i++ )
			{
				routErosion(cells[j][i]);
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
		double v = cell.volume;
		
		if (cell.neighbor[GeoCell.EAST] != null)
		{
			double nxv = cell.neighbor[GeoCell.EAST].volume;
		
			cell.ewFlux = calcRK(v, nxv);
		}
		
		if (cell.neighbor[GeoCell.SOUTH] != null)
		{
			double nxv = cell.neighbor[GeoCell.SOUTH].volume;
		
			cell.nsFlux = calcRK(v, nxv);
		}
	}

	private static double calcRK( double v, double nv )
	{
		double k1 = eDeriv(v, nv, 1.0);
		double k2 = eDeriv(v + k1, nv, 2.0);
		double k3 = eDeriv(v + k2, nv, 2.0);
		double k4 = eDeriv(v + k3, nv, 1.0);

		return  k1/6.0 + k2/3.0 + k3/3.0 + k4/6.0;
	}

	private static void routErosion( GeoCell cell )
	{
		
		if (cell.neighbor[GeoCell.EAST] != null)
		{
			cell.volume += cell.ewFlux;
		    cell.neighbor[GeoCell.EAST].volume -= cell.ewFlux;
		}
		
		if (cell.neighbor[GeoCell.SOUTH] != null)
		{
			cell.volume += cell.nsFlux;
		    cell.neighbor[GeoCell.SOUTH].volume -= cell.nsFlux;
		}
	}


	private static double eDeriv(double cur, double next, double prop)
	{
		return ((next-cur) * EROSION_PROP) / prop;
	}

	private static void dumpState( GeoCell[][] cells, int i)
	{
		System.out.printf("\n%3d: \n", i );

		for ( int n=0; n<cells.length; n++ )
			System.out.printf("%8.4f ", north[n]);
		System.out.printf("\n\n");

		for ( int n=1; n<cells.length-1; n++ )
		{
			System.out.printf("%8.4f   ", west[n]);

			for ( int m=1; m<cells.length-1; m++ )
			{
				System.out.printf("%8.4f ", cells[n][m].volume);
			}

			System.out.printf("  %8.4f ", east[n]);

			System.out.printf("\n");
		}

		System.out.printf("\n");
		for ( int n=0; n<cells.length; n++ )
			System.out.printf("%8.4f ", south[n]);
		System.out.printf("\n");
	}

	
	private static void dumpFinalState(GeoCell[][] cells)
	{
		double nonEroded = 0;
		for ( int j=1; j<cells[0].length-1; j++ )
			for ( int i=1; i<cells.length-1; i++ )
			{
				nonEroded += cells[j][i].volume;
			}

		double n=0,e=0,s=0,w=0;
		for ( int i=0; i<cells.length; i++ )
		{
			n += north[i];
			s += south[i];
		}

		for ( int i=0; i<cells.length; i++ )
		{
			w += west[i];
			e += east[i];
		}

		System.out.printf("n: %8.4f  s: %8.4f  w: %8.4f,  e: %8.4f\n", n,s,w,e );
		System.out.printf("nonEroded = %9.5f, totalSystem = %9.5f\n", nonEroded, nonEroded+n+w+e+s );
	}
	
}

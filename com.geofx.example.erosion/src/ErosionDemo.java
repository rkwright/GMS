
public class ErosionDemo
{
	private static final double EROSION_PROP = 0.01;
	private static double tot0;
	private static double totLen;


	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		final int 	NCELLS = 15;
		int 		nStep = 10000;
		int			nCells = NCELLS;
		double[]	cells = null;
		double[]	edges = null;

		if (args.length > 0)
		{
			
		}
			
		cells = new double[nCells+2];
		
		for ( int n=1; n<cells.length-1; n++ )
		{
			cells[n] = 1.0;
		}
		
		edges = new double[nCells+1];
		
		for (int i=0; i<nStep; i++)
		{
			erodeGrid(cells, edges);
			
			tot0 += cells[0];
			totLen += cells[cells.length-1];

			cells[0] = 0.0;
			cells[cells.length-1] = 0.0;
			
			System.out.printf("%3d: ", i );
			for ( int n=0; n<cells.length; n++ )
			{
				System.out.printf("%8.4f ", cells[n]);
			}
			System.out.printf("\n");
		}
	}

	/**
	 * 
	 * @param cells
	 */
	private static void erodeGrid( double[] cells, double[] edges )
	{	
		for ( int n=0; n<cells.length-1; n++ )
		{
			erodeCell(cells, edges, n);
		}
		
		// Sum up the initial pass of the RK cycle.  cells[*][0] holds the current real value
		for ( int n=0; n<cells.length-1; n++ )
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
	private static void erodeCell( double[] cells, double[] edges, int n )
	{		
		double y = cells[n];
		double ny = cells[n+1];

		double k1 = eDeriv(y, ny, 1.0);
		double k2 = eDeriv(y + k1, ny, 2.0);
		double k3 = eDeriv(y + k2, ny, 2.0);
		double k4 = eDeriv(y + k3, ny, 1.0);

		edges[n] = k1/6.0 + k2/3.0 + k3/3.0 + k4/6.0;
	}

	private static void routErosion(double[] cells, double[] edges, int n)
	{
		cells[n] += edges[n];
		cells[n+1] -= edges[n];
	}


	private static double eDeriv(double cur, double next, double prop)
	{
		return ((next-cur) * EROSION_PROP) / prop;
	}

	private static void dumpState(double[] cells, int i)
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

	private static void dumpFinalState(double[] cells)
	{
		double nonEroded = 0;
		for ( int n=1; n<cells.length-1; n++ )
		{
			nonEroded += cells[n];
		}

		System.out.printf("nonEroded = %9.5f, totalSystem = %9.5f", nonEroded, nonEroded+tot0+totLen );

		System.out.printf("\n");
	}
}

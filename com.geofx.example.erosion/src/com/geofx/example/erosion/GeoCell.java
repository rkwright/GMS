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

public class GeoCell
{
	public static String[] 		EdgeStr      =  {   "S", "W", "N", "E" };
	public static int[] 		EdgeBit      =  {   GeoCell.SOUTH_BIT,  GeoCell.WEST_BIT,  GeoCell.NORTH_BIT,  GeoCell.EAST_BIT };
	public static int[]			OppEdgeBit   =  {   GeoCell.NORTH_BIT,  GeoCell.EAST_BIT,  GeoCell.SOUTH_BIT,  GeoCell.WEST_BIT };
	public static int[]			XEdge        =  {   0, -1,  0,  1 };
	public static int[]			YEdge        =  {  -1,  0,  1,  0 };
	public static int[][]		EdgeIndx     =  { {  -1,  0,  -1 }, 
												  {   1, -1,   3 },
	                                              {  -1,  2,  -1 }  };

	public static int[]    		DirShift = { GeoCell.SOUTH_SHIFT, GeoCell.WEST_SHIFT, GeoCell.NORTH_SHIFT, GeoCell.EAST_SHIFT };
	public static int[]   		DirMask  = { GeoCell.SOUTH_MASK,  GeoCell.WEST_MASK,  GeoCell.NORTH_MASK,  GeoCell.EAST_MASK };

	public static final int     SOUTH = 0;              // the cardinal directions 
	public static final int		WEST  = 1;
	public static final int		NORTH = 2;
	public static final int		EAST  = 3;

	public static final int 	SOUTH_BIT = 1;          // 1 << (cardinal_direction) 
	public static final int 	WEST_BIT  = 2;
	public static final int 	NORTH_BIT = 4;
	public static final int 	EAST_BIT  = 8;

	public static final int 	SOUTH_MASK = 0xff; 	    // 0xff << (cardinal_shift) 
	public static final int 	WEST_MASK  = 0xff00;
	public static final int 	NORTH_MASK = 0xff0000;
	public static final int 	EAST_MASK  = 0xff000000;

	public static final int 	SOUTH_SHIFT = 0; 	    // (log2 (cardinal_direction)) << 3 
	public static final int 	WEST_SHIFT  = 8;
	public static final int 	NORTH_SHIFT = 16;
	public static final int 	EAST_SHIFT  = 24;

	public double				volume;						// volume or potential of the cell
	public double				ewFlux;						// flux from east to west
	public double				nsFlux;						// flux from north to south
	GeoCell[]					neighbor = new GeoCell[4];	// flux out of the edges of cell - cardinal dir encoded
		
	public GeoCell( double volume, GeoCell westCell, GeoCell northCell )
	{
		this.volume = volume;
		
		if (westCell != null)
		{
			neighbor[WEST] = westCell;
			westCell.neighbor[EAST] = this;
		}
		
		if (northCell != null)
		{
			neighbor[NORTH] = northCell;
			northCell.neighbor[SOUTH] = this;
		}	
	}
}

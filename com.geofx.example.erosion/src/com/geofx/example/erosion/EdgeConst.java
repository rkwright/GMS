package com.geofx.example.erosion;


public class EdgeConst
{

	public  final int 	SOUTH = 0; // the cardinal directions 

	public static final int 	WEST  = 1;

	public static final int 	NORTH = 2;

	public static final int 	EAST  = 3;

	// public static final int   E_W_EXIT(arg)	=	((arg) & WEST);

	public static final int 	SOUTH_BIT = 1; // 1 << (cardinal_direction) 

	public static final int 	WEST_BIT = 2;

	public static final int 	NORTH_BIT = 4;

	public static final int 	EAST_BIT = 8;

	public static final int 	SOUTH_MASK = 0xff; // 0xff << (cardinal_shift) 

	public static final int 	WEST_MASK = 0xff00;

	public static final int 	NORTH_MASK = 0xff0000;

	public static final int 	EAST_MASK = 0xff000000;

	public static final int 	SOUTH_SHIFT = 0; // (log2 (cardinal_direction)) << 3 

	public static final int 	WEST_SHIFT = 8;

	public static final int 	NORTH_SHIFT = 16;

	public static final int 	EAST_SHIFT = 24;
}

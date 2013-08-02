package com.geofx.map.util;
import java.util.Vector;


	// 
	// This class defines the upper and lower limits of the contours for each
	// cell, where
	//
	// for a given cell, whose actual data point
	// is represented by the '*'
	// l +------+ limb = 1 is the left-side of the cell,
	// ^ i | | limb = 0 is the bottom of the cell
	// | m | |
	// incr b | |
	// Y 1 *------+
	// limb = 0
	//
	// --> increasing x
	// 
	//
	// 
	// Note on the slope parameter: the parm represents the sign of the
	// slope along the direction of travel for the vector, where it
	// it intersects the edge:
	// not edge = 0,
	// higher elev to right = +1 -> clockwise
	// higher elev to left = -1 -> counterclockwise
	// 
	// hence if one goes clockwise, the higher elev will be encircled.

	public class ContourVector
	{
		// the coordinate array
		public Vector<Double> 	x = new Vector<Double>(); 
		public Vector<Double> 	y = new Vector<Double>(); 

		protected int 		elmKnt; 		// number of elms in the coordinate array
		protected short 	stCW; 			// sign of slope of start edge limb intersected by vector
		protected short 	finCW; 			// sign of slope of finish edge limb intersected by vector
		protected int 		stEdge; 		// edge number for start
		protected int 		finEdge; 		// edge number for finish
		
		public int			strokeIndex = -1;
	};

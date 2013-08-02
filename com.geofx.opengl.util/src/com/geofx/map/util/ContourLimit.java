package com.geofx.map.util;

import com.geofx.map.util.Contour;
/**
 * 
 * @author riwright
 *
 */
public class ContourLimit
{
	protected byte 	top0 = 0;	 	// Contour limits for a given cell, limb 0
	protected byte 	bot0 = Contour.MAX_LOOP_LIMIT; 	
	protected byte 	top1 = 0; 		// Contour limits for a given cell, limb 1
	protected byte 	bot1 = Contour.MAX_LOOP_LIMIT; 	
	protected byte 	CW0  = 0; 		// sign of slope of limb intersected by vector
	protected byte 	CW1  = 0; 		// sign of slope of limb intersected by vector
}
/*******************************************************************************
 * Copyright (c) 1984-2009 Ric Wright All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors: Ric Wright - initial implementation
 ****************************************************************************/
package com.geofx.map.util;

import java.lang.reflect.Array;
import java.util.Vector;

/**
 * 
 * @author riwright
 *
 */
public class Contour
{
	private static final double DELTA_DIV = 10000.0;

	
	public static double 	MIN_FLOAT = -1e37;
	public static double 	MAX_FLOAT = 1e37;
	
	public static byte		CLOCKWISE = 1;
	public static byte		CLOSED = 0;
	public static byte		COUNTERCLOCKWISE = -1;
	public static byte		UNDEFINED = (byte) 254;

	public static  byte 	MAX_LOOP_LIMIT = (byte) 255;

	public static int 		HORIZONTAL = 0;
	public static int 		VERTICAL = 1;
	
	public static int  		LEFT_EDGE = 0;
	public static int  		TOP_EDGE = 	1;
	public static int  		RIGHT_EDGE = 2;
	public static int  		BOTTOM_EDGE = 3;

	public static int  		TOP_BOT_EDGE = 1;		// if edg & this, then TOP or BOTTOM

	public static double 	EPSILON	= 0.005;
	
	protected int 		xdirec[] = { 1, 0, -1, 0 };
	protected int 		ydirec[] = { 0, 1, 0, -1 };
	protected int 		nexd[] = { 1, 2, 3, 0 };
	protected int 		nsd[] = { 3, 0, 1, 2 };
	protected int 		id[] = { 2, 3, 0, 1 };

	protected int 		ns; 			// first index in X
	protected int 		nf; 			// last index in X
	protected int 		ms; 			// first index in Y
	protected int 		mf; 			// last index in Y
	protected double 	minZ; 			// min value in whole array
	protected double 	maxZ; 			// min value in whole array
	protected double 	delta; 			// tolerance value used by contouring function
	protected int 		ccwKnt; 		// accumulates the CCW/CW count
	protected int 		nCont;			// number of contours to be found
	
	// the various contour levels to be done
	protected Vector<Double> 		contLevels = new Vector<Double>(); 

	 // contouring limits for each cell
	protected ContourLimit[][] 		bounds;

	protected Vector<ContourVector> contourVectors = new Vector<ContourVector>();

	
	/**
	 * 
	 * @param array
	 * @param contLevels
	 * @return
	 */
	public Vector<ContourVector> ThreadContours ( double[][] array, double contInterval )
	{
		if (SetUp( array, contInterval) == false)
		{
			return null;
		}
		
		for ( int i=0; i<contLevels.size(); i++ )
		{
			SingleContour(array, i, contLevels.get(i));
		}
		
		return contourVectors;
	}

	/**
	 * 
	 * @param contLevels 
	 * @param array 
	 * @return
	 */
	protected boolean SetUp ( double[][] array, double contInterval )
	{
		mf = Array.getLength(array);
		double[] row = (double[]) Array.get(array, 0);
		nf = Array.getLength(row);
		ns = 0;
		ms = 0;
		
		bounds = (ContourLimit[][]) new ContourLimit[nf][mf];
		for ( int i=ms; i<mf; i++ )
		{
			for ( int j=ns; j<nf; j++ )
			{
				bounds[i][j] = new ContourLimit();
			}
		}
		
		SetLimits(array, contInterval);
		
		InitFlags(array);
		
		return true;
	}

	/**
	 * 
	 * @param contLevels
	 */
	protected void SetLimits( double[][] array, double contInterval )
	{
		// find max and min in the array
		minZ = MAX_FLOAT;
		maxZ = -MAX_FLOAT;

		for ( int i=ms; i<mf; i++ )
		{
			for ( int j=ns; j<nf; j++ )
			{
				minZ = Math.min( minZ, array[i][j] );
				maxZ = Math.max( maxZ, array[i][j] );
			}
		}

		nCont = (int) Math.ceil((maxZ - minZ) / contInterval);

		for (int i = 0; i < nCont; i++)
		{
			contLevels.add(minZ + (double) i * contInterval);
		}

		delta = (maxZ - minZ) / DELTA_DIV;
	}

	/**
	 * Initialize the flag array, setting the upper and lower bounds
	 * for the contours for each segment as well as the winding direction
	 */
	protected void	InitFlags ( double[][] array )
	{
		byte		t, b;
		double		u, v;
		byte		upLim         = (byte) (contLevels.size()-1);
		
		for ( int i=ms; i<mf; i++ )
		{
			for ( int j=ns; j<nf; j++ )
			{
				ContourLimit bound = bounds[i][j];
			
				u = array[i][j];

				if (j < (nf-1))
				{
					v = array[i][j+1];	

					//  Here we set the slope-sign for this horizontal segment.
					//  We set it in terms of the X-direction vector which will intersect 
					//  this edge when the vector is going in the direction of increasing X. It 
					//  will have the opposite sign if the vector is going in the direction
					//  of decreasing X
					bound.CW0 = (v < u) ? COUNTERCLOCKWISE : CLOCKWISE;

					if (v > u)
					{
						t = upLim;
						while ( t > 0 && contLevels.get(t) > v ) t--;
						
						b = 0;
						while ( b <= t  && contLevels.get(b) <= u ) b++;
					}
					else
					{
						t = upLim;
						while ( t > 0 && contLevels.get(t) > u ) t--;

						b = 0;
						while ( b <= t  && contLevels.get(b) <= v ) b++;
					}

					if (t >= b)
					{
						bound.top0 = t;
						bound.bot0 = b;
					}
				}				

				if (i < (mf-1))
				{
					v = array[i+1][j];

					// Now we set the slope-sign for this vertical segment.  We set
					// it for the vector that will intersect this edge when the 
					// vector is going in the direction of increasing Y. It will
					// have the opposite sign if the vector is moving in
					// the direction of decreasing Y.

					bound.CW1 = (v > u) ? COUNTERCLOCKWISE : CLOCKWISE;

					if (v > u)
					{
						t = upLim;
						while ( t > 0 && contLevels.get(t) > v ) t--;

						b = 0;
						while ( b <= t && contLevels.get(b) <= u ) b++;
					}
					else
					{
						t = upLim;
						while ( t > 0 && contLevels.get(t) > u ) t--;

						b = 0;
						while ( b <= t && contLevels.get(b) <= v ) b++;
					}

					if (t >= b)
					{
						bound.top1 = t;
						bound.bot1 = b;
					}
				}
				
				System.out.println(String.format("%2d %2d: b0/t0: %2d/%2d cw0: %2d  b1/t1: %2d/%2d cw1: %2d", 
							j, i, bound.bot0, bound.top0, bound.CW0, bound.bot1, bound.top1, bound.CW1));
			} 
		} 
		
	}

	/**
	 * 
	 * @param contourNum
	 * @param contourLevel
	 * @return
	 */
	protected boolean	SingleContour ( double[][] 	array,				// the data array itself
									    int			contourNum,			// index of level of Contour
									    double		contourLevel  )  	// actual level of Contour
	{
		boolean  		bExit    = false;
		boolean  		bStart   = true;
		boolean  		bEdg     = true;
		boolean  		bInRange = true;
		boolean  		bCont    = false;
		int				dt       = 0;
		int				d0       = 0;
		int				direc    = 0;
		int				lmb      = 0;
		double			xmax     = nf - 1.0;
		double			ymax     = mf - 1.0;
		int				vecTop   = 0;
		int				x0       = 0;
		int				x1       = ns;
		int				x2       = 0;
		int				xlmb     = 0;
		int    			y0       = 0;
		int				y1       = ms;
		int				y2       = 0;
		int				ylmb     = 0;
		double			u, v, tt;
		double			m1,m2;
		short			ccwknt = 0;
		byte			ccwval = 0;
		double			delt;
		ContourLimit	bound = null;
		
		ContourVector	contVec = new ContourVector();

		while (!bExit)
		{
			bInRange = true;

			x2 = x1 + xdirec[direc];

			if (x2 < ns || x2 >= nf) 
				bInRange = false;
			else     
			{                     /* Y - range OK */
				y2 = y1 + ydirec[direc];

				if (y2 < ms || y2 >= mf)  
					bInRange = false;
			}

			if (bInRange)       	/* set xlmb,ylmb vars */
			{
				bCont = false;

				if (x1 == x2)
				{
					lmb   = VERTICAL;
					xlmb  = x2;
					ylmb  = (y2 > y1) ? y1 : y2;  
					bound = bounds[ylmb][xlmb];
					bCont = bound.bot1 == contourNum;
				}
				else
				{
					lmb   = HORIZONTAL;
					ylmb  = y1;
					xlmb  = (x2 > x1) ? x1 : x2;
					bound = bounds[ylmb][xlmb];
					bCont = bound.bot0 == contourNum;
				}

				/* if there is one, then find it */
				if (bCont)
				{    
					m1 = array[y1][x1];
					m2 = array[y2][x2];

					if (Math.abs(contourLevel - m1) <= delta)   
						m1 += ((m2 > m1) ? delta : -delta);

					if (Math.abs(contourLevel - m2) <= delta)   
					{
						delt = ((m1 > m2) ? delta : -delta);
						m2 = m2 + delt;
					}

					if (Math.abs(m2 - m1) < MIN_FLOAT)
						tt = 0.0;
					else
						tt = (contourLevel - m1) / (m2 - m1);

					if (Math.abs(tt) >= 1.0)
						tt = (1.0 - delta) * fpsign(tt);

					u  = (x2 - x1) * tt + x1;
					v  = (y2 - y1) * tt + y1;

					// store the result
					contVec.x.add(u);
					contVec.y.add(v);
					vecTop++;

					System.out.println(String.format("Found result: %d, %6.2f %6.2f for level: %6.2f", vecTop-1, u,v, contourLevel));
					
					// if the first elm, then set the entry slope value.
					// Note that we have to determine which direction we
					// are passing through the limb.
					if (vecTop == 1)
					{
						if (lmb == HORIZONTAL)
							ccwval = (byte) ((contVec.x.get(0) > 0) ? -bound.CW0 : bound.CW0);
						else
							ccwval = (byte) ((contVec.y.get(0) > 0) ? -bound.CW1 : bound.CW1);

						contVec.stCW   = ccwval;
						contVec.stEdge = FindEdge( contVec.x.get(0), contVec.y.get(0), xmax, ymax );
					}

					ccwknt += ccwval;

					// mark this seg as "used"
					if (lmb != HORIZONTAL)
					{
						bound.bot1++;
						if (bound.bot1 > bound.top1)
							bound.bot1 = MAX_LOOP_LIMIT;
					}
					else
					{
						bound.bot0++;

						if (bound.bot0 > bound.top0)
							bound.bot0 = MAX_LOOP_LIMIT;
					}
				} 
			}

			if ( !bStart )
			{
				if (bInRange)
				{
					if (bCont)
					{
						dt    = id[direc];
						direc = nsd[direc];
					}
					else    /* no contours found... */
					{
						direc = nexd[direc];
						if (direc == dt)
						{
							//  back going in same dir no contour found, it must 
							//  be closed, so dup ends so curve closes 

							// save the point
							contVec.x.add(contVec.x.get(0));
							contVec.y.add(contVec.y.get(0));
							vecTop++;

							System.out.println(String.format("Closed cont, duped end: %d, %6.2f %6.2f", vecTop-1, contVec.x.get(0),contVec.y.get(0)));

							// signal that this is closed and set the direction flag
							contVec.stCW  = CLOSED;
							contVec.finCW = (ccwknt < 0) ? COUNTERCLOCKWISE : CLOCKWISE;
							ccwknt = 0;

							contourVectors.add(contVec);

							vecTop  = 0;
							contVec = new ContourVector();

							// go back to where last Contour started and begin again
							direc   = d0;        
							x1      = x0 + xdirec[direc];
							y1      = y0 + ydirec[direc];
							bStart  = true;

							contVec.finCW = CLOSED;
						}
						else     
						{
							x1 = x2;
							y1 = y2;
						}
					}   /* cont false */
				}
				else
				{              /* out of range... */
					/*
					 *	We've reached the edge, so we need to figure out what
				     *  the slope is of the limb we are exiting through.
					 *  Note that we have to determine which direction we
					 *  we are passing through the limb.
					 */
					if (lmb == HORIZONTAL)
						contVec.finCW = (short) (( contVec.x.get(vecTop-1) < contVec.x.get(vecTop-2)) ? 
											-bound.CW0 : bound.CW0);
					else
						contVec.finCW = (short) (( contVec.y.get(vecTop-1) < contVec.y.get(vecTop-2)) ?
											-bound.CW1 : bound.CW1);

					contVec.finEdge = FindEdge( contVec.x.get(vecTop-1), contVec.y.get(vecTop-1), xmax, ymax );

					contourVectors.add(contVec);

					vecTop  = 0;
					contVec = new ContourVector();

					direc = d0;

					x1 = x0 + xdirec[direc];
					y1 = y0 + ydirec[direc];

					bStart  = true;

					contVec.finCW = CLOSED;
				}
			}     /* end of if !bStart */
			else
			{     /* if bStart */
				if (bInRange)
				{
					if (bCont)
					{       
						// found a contour, this is first cell, so 
						// save  current cell coords, direction 

						x0 = x1;   
						y0 = y1;
						d0 = direc;
						x1 = x2;
						y1 = y2;
						dt = direc;

						bStart = false;

						direc  = nexd[direc];
					}
					else
					{
						x1 = x2;
						y1 = y2;
					}
				}
				else    /* out of range... */
				{
					if (bEdg)  
					{
						if (y2 < ms)
						{
							x1    = ns;
							y1    = ms + 1;
							direc = 0;
							bEdg  = false;
						}
						else
							direc = nexd[direc];
					}
					else
					{
						if (direc == 1)
						{
							y1 = ms;
							x1++;
							bExit = (x1 >= nf);
						}
						else
						{
							y1++;
							x1 = ns;
							if (y1 >= mf) 
							{
								x1    = ns + 1;
								y1    = ms;
								direc = 1;
							}
						}
					}
				}
			}    // elseif bStart

		}	// while
		
		return true;
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param xmax
	 * @param ymax
	 * @return
	 */
	protected int FindEdge(double x, double y, double xmax, double ymax)
	{
		int edg = LEFT_EDGE;

		if (fpnear(x, xmax))
			edg = RIGHT_EDGE;
		else if (fpnear(y, ymax))
			edg = TOP_EDGE;
		else if (fpnear(y, 0))
			edg = BOTTOM_EDGE;

		return edg;
	}

	/**
	 * 
	 * @param arg
	 * @return
	 */
	protected double fpsign(double arg)
	{
		return (arg < 0.0) ? -1.0 : 1.0;
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	protected boolean fpnear(double a, double b)
	{
		return (Math.abs(a-b) < EPSILON);
	}
	
	/**
	 * 
	 * @author riwright
	 *
	 */
	private class ContourLimit
	{
		protected byte 	top0 = MAX_LOOP_LIMIT;	 	// Contour limits for a given cell, limb 0
		protected byte 	bot0 = MAX_LOOP_LIMIT; 	
		protected byte 	top1 = MAX_LOOP_LIMIT; 		// Contour limits for a given cell, limb 1
		protected byte 	bot1 = MAX_LOOP_LIMIT; 	
		protected byte 	CW0  = UNDEFINED; 		// sign of slope of limb intersected by vector
		protected byte 	CW1  = UNDEFINED; 		// sign of slope of limb intersected by vector
	}
}

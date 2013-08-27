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

package com.geofx.example.erosion;

import java.util.Stack;

/**
 * @author rkwright
 *
 */
public class MazeRat
{

	protected int  			m_row;				// actual number of rows in basin
	protected int  			m_col;				// actual number of cols in basin
	protected int  			m_seedX;			// original start point in X
	protected int  			m_seedY;			// original start point in Y
//	protected int   		m_maxFront;  		// max count of items in front list

	protected boolean		m_bSearch;			// true if still searching    
	protected boolean   	m_bSuccess;			// true if search was successful 
	protected boolean   	m_bSac;				// true if last cell was cul-de-sac
	protected int    	    m_targetX;			// coords of target
	protected int   		m_targetY;
	protected int   		m_x;				// coords of cell
	protected int			m_y;
	protected int    		m_lastX;
	protected int			m_lastY;			// coords of last cell drawn to
	protected int   		m_mask;				// bit flag for active flag check
	
	protected Stack<Coord>	  	m_stack; 			// solution search stack   
	protected Stack<Coord> 		m_mouseStack;		// mouse-values stack
	
	//int   				m_mouseIndex;		// index into mouse-stack

	protected boolean				m_bSingleHit;	
	protected MazeEvent		mazeEvent = null;
	protected byte[][]		maze;

	/**
	 * ctor
	 * 
	 * @param mazObj
	 */
	public MazeRat()
	{
		m_stack      = new Stack<Coord>();
		m_mouseStack = new Stack<Coord>();

	}
	
	//*****************************************************************************
	//
	//  Initialize the specified object, preparing it for solution.
	//
	//----------------------------------------------------------------------------
	public boolean initSolveObj ( byte[][]  maze,
								  int       row,
								  int       col,
								  int		seedX,
								  int		seedY,
								  byte		msk,			// mask value for object ID      
						          boolean	bSingHit  )
	{
		this.maze = maze;
		
		m_row = row;
		m_col = col;
		
	    m_x = seedX;
	    m_y = seedY;

	    m_mask = msk;          // unique mask value for this object     

	    m_bSearch    = true;
	    m_bSuccess   = false;

	    m_bSingleHit = bSingHit;

	    for ( int i=0; i<m_row; i++ )
	        for ( int j=0; j<m_col; j++ ) 
	            maze[i][j] |= 0xf0;

		 // push seed on Stack 
	    m_stack.add(new Coord(seedX,seedY));

	    return true;
	}

	//*****************************************************************************
	//
	//   Solves the specified maze by using a variant of the 4x4 seed fill.
	//
	//-----------------------------------------------------------------------------
	protected boolean findSolution ( byte[][]           maze,
									 int        		xexit,
									 int        		yexit,
									 MazeEvent			mazeEvent )
	{
		m_targetX = xexit;
		m_targetY = yexit;

		this.mazeEvent = mazeEvent;			// assign the local pointer... 
		
		// while Stack not empty... 
		while ( m_stack.size() > 0 && m_bSearch )
		{
			solveStep(maze);

			updateObject(maze);
		}

		return m_bSuccess;
	}

	//*****************************************************************************
	//
	//  This func solves one step for the specified by maze by using a variant 
	//  of the 4x4 seed fill.
	//
	//-----------------------------------------------------------------------------
	protected boolean solveStep ( byte[][]  maze )
	{
		int		px,py;
		int		k,mazval,zx,zy;
		
		// pop next value from Stack 
		Coord c = m_stack.pop();
		px = c.x;
		py = c.y;
		m_x = px;
		m_y = py;

		// if exit not yet found... 
		if ( px != m_targetX || py != m_targetY )
		{
			mazval = maze[py][px];
			
			if (mazeEvent != null)
				mazeEvent.report("solveStep",  px,  py,  -1,  -1,  m_stack.size(), false);

			// turn off top bit to show this cell has been checked 
			maze[py][px] ^= m_mask; 

			m_bSac = true;
			for ( k=0; k<4; k++ )
			{
				zx = px + GeoCell.XEdge[k];
				zy = py + GeoCell.YEdge[k];

				if ( zx >= 0 && zx < m_col && zy >= 0 && zy < m_row &&
					  (maze[zy][zx] & m_mask) != 0 && 
					  ((mazval & (1 << k)) == 0) )
				{
					m_bSac = false;
					m_stack.add(new Coord(zx, zy));
					//if (mazeEvent != null)
					//	mazeEvent.report("addStack",  px,  py,  zx,  zy,  m_stack.size(), false);
				}
			}
			
			m_bSuccess = false;
		}
		else
			m_bSuccess = true;

		m_bSearch = !m_bSuccess;
			
		return m_bSearch;
	}

	///*****************************************************************************
	//
	//  Updates the current position within the "maze".
	//
	//----------------------------------------------------------------------------
	protected boolean updateObject ( byte[][] maze)
	{
		int     msx, msy;
		int     posx, posy;

		// get and save object's current position 
		posx = m_lastX = m_x;
		posy = m_lastY = m_y;

		if ( m_bSingleHit ) // &&  m_mouseIndex > 0 )
		{
			if (m_mouseStack.size() > 0)
			{
				Coord c = m_mouseStack.peek();
				msx = c.x;
				msy = c.y;
			}
			else
			{
				msx = -1;
				msy = -1;
			}

			if (mazeEvent != null)
				mazeEvent.report( "updateObject", posx, posy, msx, msy, m_stack.size(), m_bSac );
		}

		// if NOT a cul-de-sac, then save position  on stack 
		if ( !m_bSac )
		{
			m_mouseStack.add(new Coord(posx,posy));
		}
		else 
			// if cul-de-sac then re-trace "steps" 
			retraceSteps( false );

		return true;
	}

	//*****************************************************************************
	//
	//  This func updates the current position within the "maze".
	//
	//----------------------------------------------------------------------------
	protected boolean retraceSteps ( boolean last_step )
	{
		boolean		Adjacent = false;
		int			msx, msy;
		int			posx, posy;
		int			mazval, edg;
//		boolean		bCulDeSac = true;	
		Coord		coord;

		//if (m_stack.size() == 0 && !last_step)
		//	return true;

		last_step = m_stack.size() == 0;
		
		// Get ACTUAL next position from Main Stack , i.e. the pos to which we 
		// must retrace our steps.  Note that we have to handle the last step 
		// specially because the stack is now empty.
		if (last_step)
		{
			posx = m_seedX;
			posy = m_seedX;
		}
		else
		{
			// set the point to retrace to as the next item on the stack
			Coord c = m_stack.peek();
			posx = c.x;
			posy = c.y;
		}
		
		// get maze value at that position 
		mazval = maze[posy][posx];


		do
		{
			if ( m_mouseStack.size() > 0 )
			{
				// pop previous position from mouse-stack 
				coord = m_mouseStack.pop();
				msx = coord.x;
				msy = coord.y;

				if ( !m_bSingleHit && mazeEvent != null )
					mazeEvent.report( "retraceSteps", m_lastX, m_lastY, msx, msy, m_stack.size(), m_bSac );

				// only the first cell is a real cul-de-sac, so clear the local flag
	//			bCulDeSac = false;

				// retrace line to that position 
				m_lastX = msx;
				m_lastY = msy;
			
				// simple computational convenience 
				msx -= posx;
				msy -= posy;

				// are we next to the "target"?? 
				Adjacent = ( msx == 0 || msy == 0 ) 
									&& ( Math.abs(msy) == 1 || Math.abs(msx) == 1 );

				if ( Adjacent && !last_step )
				{
					// see if the way is open.. 
					edg = GeoCell.EdgeIndx[msy+1][msx+1];

					if ((Adjacent = ((mazval & (1 << edg))) == 0))  
						m_mouseStack.add(coord);   // was m_mouseIndex++;  ??
				}
			}
		}
		while ((m_mouseStack.size() > 0) && ( !Adjacent || last_step ));

		// if this is the end, call back and report that we are exiting the initial seed point
		if (last_step)
		{
			if ( !m_bSingleHit && mazeEvent != null )
				mazeEvent.report( "retraceSteps", m_seedX, m_seedY, -1, -1, m_stack.size(), m_bSac );
		
		}
		
		return true;
	}

}

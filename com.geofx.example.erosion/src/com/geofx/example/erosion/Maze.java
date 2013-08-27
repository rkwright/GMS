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

import java.util.ArrayList;
import java.util.Random;

public class Maze implements MazeEvent
{	
	private  int 		col = 0; 	// actual number of rows in maze
	private  int 		row = 0;	// actual number of cols in maze

	private  int  		srcKnt;		// count of items in src list
	private  int		curX;
	private  int		curY;

	private  ArrayList<Coord> 	neighbors = new ArrayList<Coord>();
	
	public	int			maxNeighbors;	//just for info's sake
	public 	int			nStep = 0;

	private  Random		random;

	/**
	 * Just initialize the parameters that control the maze-building
	 * process.
	 * 
	 * @param maze - the array holding all the cell edges
	 * @param c - number of columns in the maze
	 * @param r - number of rows in the maze
	 */
	public void initMaze( byte[][] maze, int col, int row, int seedX, int seedY )
	{				
		this.row   = row;
		this.col   = col;

		srcKnt = col * row;
	
		maze[seedY][seedX] = (byte)0xff;
		
		curX = seedX;
		curY = seedY;
	
		srcKnt--;

		maxNeighbors = 0;
		
		random = new Random(System.currentTimeMillis());
	}

	/**
	 * Builds the maze.  basically, it just starts with the seed and visits 
	 * that cell and checks if there are any neighbors that have NOT been
	 * visited yet.  If so, it adds the to neighbors list.
	 * 
	 * Then it randomly removes one of the current set of unvisited neighbors
	 * and visits that cell and tries to connect it to surrounding cells that 
	 * been visited, i.e. add that cell to the tree.
	 * 
	 * @param maze - the array holding all the cell edges
	 */
	private void buildMaze ( byte[][] maze )
	{
		do
		{
			if (srcKnt > 0)
				findNeighbors(maze, curX, curY);

			int k = random.nextInt(65536) % neighbors.size();

			Coord c = neighbors.remove(k);
			curX = c.x;
			curY = c.y;

			System.out.printf("Dissolving edge for current cell: %3d %3d  k: %2d\n", curX, curY, k );

			dissolveEdge(maze, curX, curY);
		}
		while (neighbors.size() > 0);
	}

	/**
	 * Finds all neighbors of the specified cell.  Each neighbor is pushed onto 
	 * the "stack" (actually just an array list.  
	 * 
	 * @param maze - the array holding all the cell edges
	 * @param x - current index into the array
	 * @param y
	 * @return - true if a neighbor added to the list
	 */
	private  boolean findNeighbors ( byte[][] maze, int x, int y )
	{
		boolean   	flag=false;
		int     	zx,zy;

		for ( int i=0; i<4; i++ )
		{
			// set local variables 
			zx = x + GeoCell.XEdge[i];
			zy = y + GeoCell.YEdge[i];

			// if indicies in range and m_data cell still zero then  
			// the cell is still in the "src list" 			 
			 
			if (zx >= 0 && zx < col && zy >= 0 && zy < row && maze[zy][zx] == 0)
			{
				maze[zy][zx] = (byte)0xf0;

				neighbors.add(new Coord(zx,zy));
		
				System.out.printf("Adding to neighbors[%d]: %2d %2d\n", neighbors.size(), zx, zy );
			
				if (neighbors.size() > maxNeighbors)
					maxNeighbors = neighbors.size();

				srcKnt--;
				
				flag = true;
			}
		}

		return flag;
	}

	/**
	 * Dissolves the edge between the specified cell and one of the 
	 * adjacent cells in the spanning tree.  However, it does so ONLY
	 * if the adjacent cell is already part of the "maze tree", i.e. 
	 * it won't open a cell into an unvisited cell.
	 * The algorithm is such that it is guaranteed that each cell will 
	 * only be visited once.
	 * 
	 * @param maze - the array holding all the cell edges
	 * @param x - current index into the array
	 * @param y
	 * @return - true if added to the tree
	 */
	private  boolean dissolveEdge ( byte[][] maze, int x, int y )
	{
		boolean	flag = false;
		int		nabknt = 0;
		int		edg;
		int[]	EdgeRay = { 0,0,0,0 };
		int		zx,zy;

		// build the fence for this cell 
		maze[y][x] = (byte)0xff;
		
		for ( int i=0; i<4; i++ ) 
		{
			// set local variables 
			zx = x + GeoCell.XEdge[i];
			zy = y + GeoCell.YEdge[i];
			
			// if indicies in range 
			if ( zx >= 0 && zx < col && zy >= 0 && zy < row &&
				    (maze[zy][zx] & GeoCell.OppEdgeBit[i]) != 0 )
			{
				EdgeRay[nabknt++] = i;
			}
		}

		if ( nabknt > 0 )
		{
			edg = EdgeRay[random.nextInt(65536) % nabknt];
			zx  = x + GeoCell.XEdge[edg];           
			zy  = y + GeoCell.YEdge[edg];
		
			maze[y][x] ^= (1 << edg);
			maze[zy][zx] ^= GeoCell.OppEdgeBit[edg];	
			
			System.out.printf("%3d: In cell %3d %3d, dissolving edge: %2d (%s) into cell: %3d %3d\n", nStep++, x, y, edg,GeoCell.EdgeStr[edg], zx,zy);
	
			flag = true;
		}

		return flag;
	}
	
	/**
	 * @param maze
	 * @param nCELLS
	 * @param nCELLS2
	 */
	private void dumpEdges(byte[][] maze, int col, int row )
	{
		for (int i=0; i<row; i++)
			for (int j=0; j<col; j++ )
			{
				byte mz = maze[i][j];
				System.out.printf("%2d %2d: S: %d W: %d N: %d E: %d\n", 
						j, i, mz & GeoCell.SOUTH_BIT, mz & GeoCell.WEST_BIT,mz & GeoCell.NORTH_BIT,mz & GeoCell.EAST_BIT   );
			}
	}
	
	/** 
	 * @see com.geofx.example.erosion.MazeEvent#mazeEvent(int, int, int, int, int, boolean)
	 */
	public void report( String description, int posx, int posy, int msx, int msy, int stackDepth, boolean bSac)
	{
		System.out.printf("%12s, posx: %3d, posy: %3d, msx: %3d, msy: %3d, depth: %3d, bSac: %s\n", description, posx, posy, msx, msy, stackDepth, bSac?"true":"false");
	}
	
	/**
	 * The main method, used for testing.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		final int 	NCELLS = 64;
		byte[][]	maze = null;

		if (args.length > 0) {}

		long startTime = System.currentTimeMillis();
		Maze	mzObj = new Maze();
		
		maze = new byte[NCELLS][NCELLS];

		mzObj.initMaze( maze, NCELLS, NCELLS, 0, 0 );
		
		mzObj.buildMaze(maze);
	
		long endTime = System.currentTimeMillis();

		System.out.printf("Maze complete! elapsed: %6.2f seconds. maxNeighbors: %d\n", (endTime - startTime)/1000.0, mzObj.maxNeighbors);
	
		mzObj.dumpEdges(maze, NCELLS, NCELLS);
		
		startTime = System.currentTimeMillis();

		MazeRat rat = new MazeRat();
		
		rat.initSolveObj(maze, NCELLS,  NCELLS, 0, 0, (byte)0x80, false);
		
		rat.findSolution(maze, -10, -10, mzObj);
	
		endTime = System.currentTimeMillis();
		
		System.out.printf("Maze solved! elapsed: %6.2f seconds. maxStack: %d\n", (endTime - startTime)/1000.0, mzObj.maxNeighbors);
	}


}




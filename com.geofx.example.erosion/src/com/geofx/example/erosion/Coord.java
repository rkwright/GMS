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


/**
 * 
 * 
 *
 */
public class Coord 
{
	  public int x;
	  public int y;

	  public Coord(int x, int y) 
	  {
	    this.x = x;
	    this.y = y;
	  }

	  public Coord() 
	  {
	    this.x = 0;
	    this.y = 0;
	  }

	  public int getX() { return x; }
	  public int getY() { return y; }

	  public boolean equals ( Coord coord ) 
	  {
		  return this.x == coord.x && this.y == coord.y;
	  }
	  
	  public boolean equals ( int x, int y ) 
	  {
		  return this.x == x && this.y == y;
	  }
}
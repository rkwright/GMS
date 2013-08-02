/*******************************************************************************
 * Copyright (c) 2008 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 ****************************************************************************/

package com.geofx.opengl.util;

import java.lang.reflect.Array;

import javax.media.opengl.GL;

import com.geofx.opengl.util.Axis;
import com.geofx.opengl.util.CTM;
import com.geofx.opengl.util.DataIteratorXY;
import com.geofx.opengl.util.PSGraphics;
import com.geofx.opengl.util.Axis.AxisPos;
import com.geofx.opengl.util.LineCap.CapType;
import com.geofx.opengl.util.LineJoin.JoinType;

public class Graph2D
{	
	private static final double DEFAULT_PADDING = 0.05;
	
	public PSGraphics	ps;
	private CTM 		ctm = new CTM();
	public double		xVMin; 
	public double 		yVMin;
	public double 		xVMax; 
	public double 		yVMax;
	private double		xFrMin; 
	private double 		yFrMin;
	private double 		xFrMax; 
	private double 		yFrMax;
	public double 		xMin;
	public double 		yMin; 
	public double 		xMax;
	public double 		yMax;
	
	public double[]		padding = new double[4];
    
    public Axis[]		axes = new Axis[4];
	
	private double		fontScale = 0.05;
	private String		fontName = "Verdana";
	private int			symbolColor = 0xff0000;
	private int 		compileIndex = 0;

	
	public Graph2D ( PSGraphics ps )
	{
		this.ps = ps;
		
		setPadding(DEFAULT_PADDING);
	}
	
	/**
	 * Sets up the current model-view so that the specified viewport is scaled so that 
	 * it is mapped to the x,y values.
	 */
	public void setViewPort ( GL gl, double xVMin, double yVMin, double xVMax, double yVMax,
			                          double xOrg, double yOrg, double xMax, double yMax )
	{
		this.xVMin = xVMin;
		this.yVMin = yVMin;
		this.xVMax = xVMax;
		this.yVMax = yVMax;

		resetFrame();

		this.xMin = xOrg;
		this.yMin = yOrg;
		this.xMax = xMax;
		this.yMax = yMax;

		updateLayout(gl);
	}

	private void updateCTM(GL gl )
	{
		double width = (xFrMax-xFrMin);
		double height = (yFrMax - yFrMin);
				
		ctm.setToIdentity();
		ctm.translate(xFrMin, yFrMin);
		ctm.scale(width/(xMax-xMin), height/(yMax-yMin));
		ctm.translate(-xMin, -yMin);
		
		ps.setmatrix(ctm);
		
		gl.glEnable( GL.GL_NORMALIZE);
	}

	private void resetFrame()
	{
		this.xFrMin = this.xVMin + padding[Axis.AxisPos.LEFT.ordinal()];
		this.yFrMin = this.yVMin + padding[Axis.AxisPos.BOTTOM.ordinal()];
		this.xFrMax = this.xVMax - padding[Axis.AxisPos.RIGHT.ordinal()];
		this.yFrMax = this.yVMax - padding[Axis.AxisPos.TOP.ordinal()];
	}
	
	public void drawFrame()
	{
		ps.newpath();
		
		ps.moveto(xMin,yMin);
		ps.lineto(xMax, yMin);
		ps.lineto(xMax, yMax);
		ps.lineto(xMin, yMax);
		ps.closepath();
	
		ps.pushmatrix();
		
		ctm.setToIdentity();
		ps.setmatrix(ctm);

		ps.stroke();
		
		ps.popmatrix();
	}
	
	public static double fpsign( double arg )
	{
		return arg < 0 ? -1 : 1;
	}
	
	private void updateLayout (GL gl )
	{
		ps.setfont(fontName); 
		ps.scalefont(fontScale);
		ps.setFlatness(0.0005f);	
		
		resetFrame();
		updateCTM(gl);
		
		for ( int i=0; i< 4; i++ )
		{
			Axis ax = axes[i]; 
			if ( ax != null)
			{
				double padding = ax.getDepth();
				if (ax.getAxisPos() == AxisPos.BOTTOM)
				{
					yFrMin -= padding;
				}
				else if (ax.getAxisPos() == AxisPos.LEFT)
				{
					xFrMin -= padding;
				}
				else if (ax.getAxisPos() == AxisPos.TOP)
				{
					yFrMax += padding;
				}
				else  // RIGHT
				{
					xFrMax += padding;
				}
			}
		}

		updateCTM(gl);
	}

	private void resetAxes()
	{
		if (compileIndex > 0)
			ps.dispose(compileIndex);
		compileIndex = 0;
	}
	
	public void setAxis( AxisPos pos, double majorIntvl, double minorIntvl, int decimals, int significants, String label, String subLabel )
	{
		if (axes[pos.ordinal()] == null)
			axes[pos.ordinal()] = new Axis(this, pos);

		axes[pos.ordinal()].setAxis(majorIntvl, minorIntvl, decimals, significants, label, subLabel);

		resetAxes();
	}

	public void setProps( JoinType join, CapType cap, double linewidth, double depth, int color )
	{
		ps.setlinejoin(join);
		ps.setlinewidth(linewidth);
		ps.setDepth(depth);
		ps.setrgbcolor(color);
		
		ps.setFlatness(0.001f);
		
		ps.setfont(fontName);
		ps.scalefont(fontScale);
		
		resetAxes();
	}
	
	public void drawAxes(GL gl)
	{
		if (compileIndex != 0)
		{
			ps.call(compileIndex);
			return;
		}

		updateLayout(gl);
		
		System.out.println("Compiling axes");
		compileIndex = ps.beginCompile();
		
		drawFrame();
		
		for ( int i=0; i< 4; i++ )
		{
			Axis ax = axes[i]; 
			if ( ax != null)
				ax.drawAxis();
		}
		
		ps.endCompile();
	}

	public void drawPolyline ( double[] xcoords, double[] ycoords )
	{
		ps.newpath();

		for (int i=0; i<Array.getLength(xcoords); i++ )
		{
			if (i == 0)
				ps.moveto(xcoords[i], ycoords[i]);
			else
				ps.lineto(xcoords[i], ycoords[i]);
		}

		ps.pushmatrix();	

		ctm.setToIdentity();
		ps.setmatrix(ctm);

		ps.stroke();

		ps.popmatrix();
	}
	

	public void drawPolyline ( Object xObj, Object yObj )
	{
		double[] xcoords = (double[])xObj;
		double[] ycoords = (double[])yObj;
		
		drawPolyline(xcoords, ycoords);
	}
	
	public void drawPolyline ( DataIteratorXY iter )
	{
		boolean moveto = true;
		ps.newpath();

		while (iter.hasNext())
		{			
			if (moveto)
			{
				ps.moveto(iter.getX(), iter.getY());
				moveto = false;
			}
			else
				ps.lineto(iter.getX(), iter.getY());
			
			iter.next();
		}
		
		ps.pushmatrix();
		
		ctm.setToIdentity();
		ps.setmatrix(ctm);

		ps.stroke();
		
		ps.popmatrix();
	}
	
	public int getSymbolColor()
	{
		return symbolColor;
	}

	public void setSymbolColor(int symbolColor)
	{
		this.symbolColor = symbolColor;
		ps.setrgbcolor(symbolColor);
	}

	public double getFontScale()
	{
		return fontScale;
	}
	
	public void setPadding (double padding)
	{
		for ( int i=0; i<4; i++ )
			this.padding[i] = padding;
	}

	public void setPadding ( AxisPos axisPos, double padding )
	{
		this.padding[axisPos.ordinal()] = padding;
	}

	public void setFontScale(double fontScale)
	{
		this.fontScale = fontScale;
	}
	
	
}

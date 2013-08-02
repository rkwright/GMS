/*******************************************************************************
 * 
 * 
 * Code from the JOGL Demos:  https://jogl-demos.dev.java.net/
 * 
 * Contributors:
 *    
 *     Ric Wright - May 2008 - Ported to Eclipse 3.2, minor tweaks
 *******************************************************************************/
package com.geofx.gms.viewers;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Scanner;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Point2d;

import com.geofx.opengl.util.CTM;
import com.geofx.opengl.util.DataIteratorXY;
import com.geofx.opengl.util.Graph2D;
import com.geofx.opengl.util.PSGraphics;
import com.geofx.opengl.util.Axis.AxisPos;
import com.geofx.opengl.util.LineCap.CapType;
import com.geofx.opengl.util.LineJoin.JoinType;
import com.geofx.opengl.view.IGLView;


public class PolylineGraph implements IGLView 
{
	private static final double FONT_SCALE = 0.04;

	private int[]				colors; 
	
	protected Graph2D			seriesGr  = null;

	protected ArrayList<Object>	array;
	protected DataIteratorXY 	iter;
	protected int				nIter;

	protected double			xWidth;
	
	protected CTM 				ctm = new CTM();

	PSGraphics					ps = null;
	protected JoinType 			joinType = JoinType.Bevel;
	protected CapType 			capType  = CapType.Butt;
	protected double			strokeWidth = 0.01;

	protected Point2d     		point;

	protected int 				frameColor = 0xc0c0c0;

	protected double			xVMin; 
	protected double 			yVMin;
	protected double 			xVMax; 
	protected double 			yVMax;
	protected double 			xMin;
	protected double 			yMin; 
	protected double 			xMax;
	protected double 			yMax;

	private double 				span;
	
	public PolylineGraph() 
	{
		//System.out.println("PolylineGraph - constructor");

		ps = new PSGraphics();

		seriesGr = new Graph2D(ps);
	}
	

	public void init ( GLAutoDrawable drawable ) 
	{
		//System.out.println("PolylineGraph - init");

		final GL gl = drawable.getGL();	
	    
		setupGraphs(gl);
  	}

	/**
	 * Standard JOGL display method.  Called on every refresh
	 */
	public void display( GLAutoDrawable drawable ) 
	{
		//System.out.println("PolylineGraph - display ");

		GL gl = drawable.getGL();

		repaintGraph(gl);
	}
	
	private void setupGraphs(GL gl)
	{
		seriesGr.setFontScale(FONT_SCALE);
			
		seriesGr.setViewPort(gl, xVMin, yVMin, xVMax, yVMax, xMin, yMin, xMax, yMax);

		seriesGr.setProps(JoinType.Round, CapType.Round, 0.01, 0.01, frameColor);
		
		seriesGr.drawAxes(gl);
	}
	
	private void repaintGraph( GL gl )
	{		
		gl.glPushMatrix();
		
		seriesGr.setViewPort(gl, xVMin, yVMin, xVMax, yVMax, xMin, yMin, xMax, yMax);

		seriesGr.drawAxes(gl);

		if (array != null && array.size() >= 2)
		{
			if (iter == null && array.size() > 0)
				initIter();
	
			ps.setlinejoin(joinType);
			ps.setlinecap(capType);
			ps.setlinewidth(strokeWidth);
	
			iter = (DataIteratorXY) array.get(0);
			double minX = iter.getXY(0).x;
			
			iter = (DataIteratorXY) array.get(array.size()-1);
					
			double maxX = minX + span; 
	
			//System.out.println(String.format("Timespan: %8.0f   size: %d",iter.getXY(0).x-minX,array.size()));
	
			seriesGr.setViewPort(gl, xVMin, yVMin, xVMax, yVMax, minX, yMin, maxX, yMax);

			for ( int i=0; i<nIter; i++ )
			{
				//System.out.println(String.format("Depth: %2d",i));
	
				gl.glTranslated(0.0, 0.0, 0.0001 * i);
	
				seriesGr.setSymbolColor(colors[i]);
	
				ps.newpath();
				
				for ( int j=0; j<array.size(); j++ )
				{
					iter = (DataIteratorXY) array.get(j);
					point = iter.getXY(i);
	
					if (j == 0)
						ps.moveto(point.x, point.y);
					else
						ps.lineto(point.x, point.y);
				}
	
				ps.pushmatrix();
	
				ctm.setToIdentity();
				ps.setmatrix(ctm);
	
				ps.stroke();
				
				ps.popmatrix();
			}
		}
		
		gl.glPopMatrix();
	}
	
	private void initIter()
	{
		iter = (DataIteratorXY) array.get(0);	
		nIter = iter.size();
	}
	
	public void setArray(ArrayList<Object> array)
	{
		this.array = array;
	}

	public void dispose()
	{
	}

	public boolean handleKeyEvent(KeyEvent e)
	{
		return false;
	}
	
	public void setXWidth(double xWidth)
	{
		this.xWidth = xWidth;
	}

	public void setViewPort(String viewPort)
	{
		Scanner scanner = new Scanner(viewPort);
		scanner.useDelimiter(" *, *");
		
		xVMin = scanner.nextDouble();
		yVMin = scanner.nextDouble();
		xVMax = scanner.nextDouble();
		yVMax = scanner.nextDouble();
		xMin  = scanner.nextDouble();
		yMin  = scanner.nextDouble();
		xMax  = scanner.nextDouble();
		yMax  = scanner.nextDouble();
	}

	public void setAxis( AxisPos axis, String axisInfo )
	{
		if (axisInfo == null || axisInfo.isEmpty())
			return;

		Scanner scanner = new Scanner(axisInfo);
		scanner.useDelimiter(" *, *");
		
		double majorIntvl = scanner.nextDouble();
		double minorIntvl = scanner.nextDouble();
		int decimals = scanner.nextInt();
		int significants = scanner.nextInt();
		String label = scanner.next();
		String subLabel = null;
		if (scanner.hasNext())
		{
			subLabel = scanner.next();
		}
		
		seriesGr.setAxis(axis, majorIntvl, minorIntvl, decimals, significants, label, subLabel);
	}


	public void setColors(int[] colors)
	{
		this.colors = colors;
	}


	public void setSpan(double span)
	{
		this.span = span;		
	}


	public void setFrameColor(int frameColor)
	{
		this.frameColor = frameColor;		
	}
	
}

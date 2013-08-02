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

import java.awt.event.KeyEvent;
import java.util.Scanner;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.geofx.gms.datasets.Dataset;
import com.geofx.opengl.util.CTM;
import com.geofx.opengl.util.DataIteratorXY;
import com.geofx.opengl.util.Graph2D;
import com.geofx.opengl.util.PSGraphics;
import com.geofx.opengl.util.Axis.AxisPos;
import com.geofx.opengl.util.LineCap.CapType;
import com.geofx.opengl.util.LineJoin.JoinType;
import com.geofx.opengl.view.IGLView;


public class LineGraph implements IGLView 
{
	private static final double FONT_SCALE = 0.04;

	protected CTM 				ctm = new CTM();

	protected PSGraphics		ps = null;
	protected Graph2D			graph  = null;

	protected Object			array;
	protected Dataset			xDataset;
	protected Dataset			yDataset;
	protected DataIteratorXY 	iter;
	protected int				nIter;
	
	protected int 				frameColor = 0xc0c0c0;

	protected double			xVMin; 
	protected double 			yVMin;
	protected double 			xVMax; 
	protected double 			yVMax;
	protected double 			xMin;
	protected double 			yMin; 
	protected double 			xMax;
	protected double 			yMax;

	protected JoinType 			joinType = JoinType.Bevel;
	protected CapType 			capType  = CapType.Butt;
	
	public LineGraph() 
	{
		//System.out.println("LineGraph - constructor");

		ps = new PSGraphics();

		graph = new Graph2D(ps);
	}
	
	public void init ( GLAutoDrawable drawable ) 
	{
		//System.out.println("LineGraph - init");

		setupGraphs(drawable.getGL());
  	}


	/**
	 * Standard JOGL display method.  Called on every refresh
	 */
	public void display(GLAutoDrawable drawable) 
	{
		GL gl = drawable.getGL();

		// System.out.println("LineGraph - display ");

		repaintGraph(gl);
	}
	
	private void setupGraphs(GL gl)
	{
		graph.setFontScale(FONT_SCALE);
			
		graph.setViewPort(gl, xVMin, yVMin, xVMax, yVMax, xMin, yMin, xMax, yMax);

		graph.setProps(JoinType.Round, CapType.Round, 0.01, 0.01, frameColor);

		graph.drawAxes(gl);
	}
	
	private void repaintGraph( GL gl )
	{		
		gl.glPushMatrix();
		
		graph.setViewPort(gl, xVMin, yVMin, xVMax, yVMax, xMin, yMin, xMax, yMax);

		graph.drawAxes(gl);

		if (xDataset != null && yDataset != null)
		{
			graph.setSymbolColor(0xff0000);
	
			ps.setlinejoin(joinType);
			ps.setlinecap(capType);
			
			ps.newpath();
			
			graph.drawPolyline(xDataset.getObject(), yDataset.getObject());
		}
		
		gl.glPopMatrix();
	}
	
	public void setArray(Object array)
	{
		this.array = array;
	}

	public void setXDataset( Dataset vector )
	{
		this.xDataset = vector;
	}

	public void setYDataset( Dataset vector )
	{
		this.yDataset = vector;
	}

	public void dispose()
	{
	}

	public boolean handleKeyEvent(KeyEvent e)
	{
		return false;
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
		
		graph.setAxis(axis, majorIntvl, minorIntvl, decimals, significants, label, subLabel);
	}

	public void setFrameColor(int frameColor)
	{
		this.frameColor = frameColor;		
	}
}
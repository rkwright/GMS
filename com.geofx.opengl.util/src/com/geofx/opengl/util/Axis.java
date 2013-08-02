/*******************************************************************************
 * Copyright (c) 2008 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 *     
 ****************************************************************************/

package com.geofx.opengl.util;

import javax.vecmath.Point2d;

//import com.geofx.opengl.util.AxisPos;
import com.geofx.opengl.util.CTM;
import com.geofx.opengl.util.Graph2D;
import com.geofx.opengl.util.PSGraphics;
import com.geofx.opengl.util.PSGraphics.HAlign;
import com.geofx.opengl.util.PSGraphics.VAlign;

/**
 * This class provides a relatively seamless method for ticking an axis.  It is passed 
 * a Graph2D object from which the layout information can be derived.
 * Note that this class does not label the axis.  See the classes derived from this one
 * (e.g. NumberAxis)
 * 
 */
public class Axis
{
	public enum  AxisPos { LEFT, RIGHT, TOP, BOTTOM, UNDEFINED };

	private Graph2D		gr;	
	private PSGraphics	ps;
	private AxisPos		axisPos;
	private CTM 		ctm = new CTM();
	private Point2d		txPoint = new Point2d();
	private double		tickLen = 0.02;
	private double 		majorIntvl;
	//private double		minorIntvl;
	private String		label;
	//private String		subLabel;
	private int			decimals = 0;
	private int			significants = 2;
	private String		format;	
	

	public Axis ( Graph2D gr, AxisPos pos )
	{
		this.gr = gr;
		this.ps = gr.ps;
		this.axisPos = pos;
		this.format = "%." + decimals + "f";
	}

	public void setAxis( double majorIntvl, double minorIntvl, int decimals, int significants, String label, String subLabel )
	{
		this.majorIntvl = majorIntvl;
		//this.minorIntvl = minorIntvl;
		this.decimals = decimals;
		this.significants = significants;
		this.label = label;
		//this.subLabel = subLabel;
	}

	
	public void drawAxis()
	{
		txPoint.set(tickLen, tickLen);
		ps.idtransform(txPoint);
		
		if (axisPos == AxisPos.BOTTOM)
		{
			drawAxis( gr.xMin, gr.xMax, majorIntvl, -txPoint.y, gr.yMin);
		}
		else if (axisPos == AxisPos.LEFT)
		{
			drawAxis( gr.yMin, gr.yMax, majorIntvl, -txPoint.x, gr.xMin);
		}
		else if (axisPos == AxisPos.TOP)
		{
			drawAxis( gr.xMin, gr.xMax, majorIntvl, txPoint.y, gr.yMax);
		}
		else if (axisPos == AxisPos.RIGHT)
		{
			drawAxis( gr.yMin, gr.yMax, majorIntvl, txPoint.x, gr.xMax);
		}
	}


	
	private void drawAxis( double min, double max, double intvl, double tickLen, double value )
	{
		int ntk;
		double delx;
		double delta;
		
		if (intvl != 0)
		{
			delx = max - min;
			delta = Math.abs(intvl) * Graph2D.fpsign(delx);
	
			ntk = (int) (Math.abs(((max - min) / intvl)) + 1);
	
			for (int k = 0; k < ntk; k++)
			{
				ps.newpath();
	
				drawTick(min, tickLen, value);
				
				drawNumber(min, tickLen, value);
				
				min += delta;
			}
		}
		
		drawLabel(tickLen, value);
	}


	private void drawTick(double min, double tickLen, double value)
	{

		if (axisPos == AxisPos.BOTTOM || axisPos == AxisPos.TOP)
		{
			ps.moveto(min, value);
			ps.rlineto(0, tickLen);
		}
		else
		{
			ps.moveto(value, min);
			ps.rlineto(tickLen, 0);
		}
		
		ps.pushmatrix();

		ctm.setToIdentity();
		ps.setmatrix(ctm);

		ps.stroke();

		ps.popmatrix();
	}
	
	private void drawNumber( double min, double tickLen, double value )
	{
		String str = getFormattedString(min);

		if (axisPos == AxisPos.BOTTOM || axisPos == AxisPos.TOP)
		{
			ps.setstringalign(HAlign.MIDDLE, axisPos == AxisPos.BOTTOM ? VAlign.TOP : VAlign.BOTTOM);
			ps.moveto(min, value+tickLen*1.5);
		}
		else
		{
			ps.setstringalign( axisPos == AxisPos.LEFT ? HAlign.RIGHT : HAlign.LEFT, VAlign.MIDDLE);
			ps.moveto(value+tickLen*1.5, min);
		}

		ps.pushmatrix();

		ctm.setToIdentity();
		ps.setmatrix(ctm);

		ps.show(str);

		ps.popmatrix();

	}

	private void drawLabel( double tickLen, double value )
	{
		ps.pushmatrix();

		if (axisPos == AxisPos.BOTTOM || axisPos == AxisPos.TOP)
		{
			Point2d pt = getNumBBox(gr.yMin, gr.yMax);
			double mid = (gr.xMax+gr.xMin)/2.0;
			ps.setstringalign(HAlign.MIDDLE, axisPos == AxisPos.BOTTOM ? VAlign.TOP : VAlign.BOTTOM);
			double y = value + tickLen*2 + Math.abs(pt.y)*2*Graph2D.fpsign(tickLen);
			
			//drawCross(mid, y);
			
			ps.moveto(mid, y);
		}
		else
		{
			Point2d pt = getNumBBox(gr.yMin, gr.yMax);
			Point2d pt0 = getNumBBox(0,0);
			double mid = (gr.yMax+gr.yMin)/2.0;
			ps.setstringalign( axisPos == AxisPos.LEFT ? HAlign.RIGHT : HAlign.LEFT, VAlign.MIDDLE);
			double x = value + tickLen*2 + (pt.x + pt0.x)*Graph2D.fpsign(tickLen);
			
			//drawCross(x, mid);

			ps.moveto(x, mid);
			
			ctm.setToIdentity();
			ctm.scale(gr.getFontScale(), gr.getFontScale());
			ctm.rotate(90);
			ps.scalefont(ctm);
		}

		ctm.setToIdentity();

		ps.setmatrix(ctm);

		ps.show(label);

		ps.scalefont(gr.getFontScale());

		ps.popmatrix();
	}

	@SuppressWarnings("unused")
	private void drawCross(double x, double y)
	{
		int	color = ps.currentrgbcolor();
		ps.setrgbcolor(0x00ff00);
		
		txPoint.set(tickLen, tickLen);
		ps.idtransform(txPoint);

		ps.newpath();
		ps.moveto(x-txPoint.x, y);
		ps.lineto(x+txPoint.x,y);
		ps.stroke();
		
		ps.newpath();
		ps.moveto(x, y-txPoint.y);
		ps.lineto(x,y+txPoint.y);
		ps.stroke();		

		ps.newpath();
		ps.setrgbcolor(color);
	}

	public double getDepth()
	{
		txPoint.set(tickLen, tickLen);
		ps.idtransform(txPoint);
		
		double	padding = 0;
		if (axisPos == AxisPos.BOTTOM)
		{
			padding = getDepth( -txPoint.y );
			txPoint.set(0, padding);
			ps.dtransform( txPoint );
			return txPoint.y;
		}
		else if (axisPos == AxisPos.LEFT)
		{
			padding = getDepth( -txPoint.x );
			txPoint.set(padding,0);
			ps.dtransform( txPoint );
			return txPoint.x;
		}
		else if (axisPos == AxisPos.TOP)
		{
			padding = getDepth( txPoint.y );
			txPoint.set(0, padding);
			ps.dtransform( txPoint );
			return txPoint.y;
		}
		else
		{
			padding = getDepth( txPoint.x );
			txPoint.set(padding, 0);
			ps.dtransform( txPoint );
			return txPoint.x;
		}
	}
	private double getDepth( double tickLen )
	{
		if (axisPos == AxisPos.BOTTOM || axisPos == AxisPos.TOP)
		{
			Point2d pt = getNumBBox(gr.xMin, gr.xMax);
			return tickLen * 2 + Math.abs(pt.y) * 4 * Graph2D.fpsign(tickLen);
			// ps.moveto(mid, value + tickLen*2 + Math.abs(pt.y)*2*Graph2D.fpsign(tickLen) );
		}
		else
		{
			Point2d numWid = getNumBBox(gr.yMin, gr.yMax);
			//Point2d pt0 = getNumBBox(0,0);
			
			ctm.setToIdentity();
			ctm.scale(gr.getFontScale(),gr.getFontScale());
			ctm.rotate(90);
			ps.scalefont(ctm);

			Point2d lblHt = getNumBBox(0,0);
			
			ps.scalefont(gr.getFontScale());

			return tickLen * 2 + (numWid.x + lblHt.x * 3) * Graph2D.fpsign(tickLen);
			// ps.moveto(value + tickLen*2 + (pt.x + pt0.x)*Graph2D.fpsign(tickLen), mid);
		}
	}

	public Point2d getNumBBox( double min, double max)
	{
		double maxVal = Math.max(Math.abs(min), Math.abs(max));
		String str = getFormattedString(maxVal);
		Point2d pt = ps.stringbbox(str);
		return pt;
	}

	private String getFormattedString(double arg)
	{
		double numValue = significantDigits(arg,significants);
		String	str = String.format(format, numValue);
		return str;
	}

	/**
	 * Convenience method to convert int to AxisPos enum
	 */
    public static AxisPos convertEnum( int i ) 
    {
    	for ( AxisPos current : AxisPos.values() ) 
    	{
	    	if ( current.ordinal() == i ) 
	    	{
	    		return current;
	    	}
    	}
    	return AxisPos.UNDEFINED;
    }

	public AxisPos getAxisPos()
	{
		return axisPos;
	}

	public double getTickLen()
	{
		return tickLen;
	}

	public void setTickLen(double tickLen)
	{
		this.tickLen = tickLen;
	}

	/**
	 * Takes a double argument and returns the value corresponding
	 * to that number with the specified number of significant digits.
	 * 
	 * @param arg
	 * @param ndigits
	 * @return
	 */
	public double significantDigits( double arg, int ndigits )
	{

		double  limit = Math.pow(10,ndigits-1);
		int 	exp   = 0;
		double	answ  = 0;
		
		// no significant digits in 0 !
		if (arg == 0)
			return 0;
		
		int sign = arg < 0 ? -1 : 1;
		arg = Math.abs(arg);
		
		if (arg < limit)
		{
			while (limit > arg * Math.pow(10,exp))
			{
				exp++;
			}
			
			answ = Math.floor(arg*Math.pow(10,exp));
			answ /= Math.pow(10,exp);
		}
		else
		{
			while (arg > limit * Math.pow(10,exp))
			{
				exp++;
			}
			
			answ = Math.floor(arg/Math.pow(10,exp-1));
			answ *= Math.pow(10,exp-1);			
		}
		
		return answ * sign;
	}
}

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

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.text.StringCharacterIterator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Scanner;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.vecmath.Point2d;

import com.geofx.opengl.util.Bezier;
import com.geofx.opengl.util.CTM;
import com.geofx.opengl.util.Constants;
import com.geofx.opengl.util.CubicElm;
import com.geofx.opengl.util.Dash;
import com.geofx.opengl.util.DashOp;
import com.geofx.opengl.util.Edge;
import com.geofx.opengl.util.Extruder;
import com.geofx.opengl.util.GState;
import com.geofx.opengl.util.LineCap;
import com.geofx.opengl.util.LineJoin;
import com.geofx.opengl.util.PathElm;
import com.geofx.opengl.util.Pool;
import com.geofx.opengl.util.QuadElm;
import com.geofx.opengl.util.LineCap.CapType;
import com.geofx.opengl.util.LineJoin.JoinType;
import com.geofx.opengl.util.PathElm.PathType;

public class PSGraphics extends Extruder
{	
	public enum  VAlign { TOP, MIDDLE, BOTTOM, UNDEFINED };
	public enum  HAlign { LEFT, MIDDLE, RIGHT, UNDEFINED };

	private static double 	TINY_ANGLE = 0.0001;
	// private double			moveTolerance = 0.001;

	private Pool			pool = new Pool();

	private double			lineWidth = 1.0;
	private LineCap			lineCap = new LineCap();
	private LineJoin		lineJoin = new LineJoin();
	private DashOp			dashOp = new DashOp(getPool());
	private CTM				ctm = new CTM(1.0, 0.0, 0.0, 1.0, 0.0, 0.0);
	private LinkedList<PathElm> currentPath = new LinkedList<PathElm>();
	private PathElm				currentPoint = new PathElm();
	private PathElm				pathBegin = new PathElm();
	
	private LinkedList<PathElm> 	srcPath = null;
	private LinkedList<PathElm> 	subPath = new LinkedList<PathElm>();

	private LinkedList<GState> 	gstateStack = new LinkedList<GState>();
	private LinkedList<CTM> 	ctmStack = new LinkedList<CTM>();

	private double			lineWidthX = 0.5;
	private double			lineWidthY = 0.5;
	
	private float[]			currentColor = new float[4];
	
	private Edge 			offEdge0 = new Edge();
	private Edge 			offEdge1 = new Edge();
	private Edge 			miterEdge0 = new Edge();
	private Edge 			miterEdge1 = new Edge();
	private Edge 			edge0 = new Edge();
	private Edge 			edge1 = new Edge();
	private Edge			revOrig = new Edge();
	private Edge			fwdOrig = new Edge();
	private PathElm			previousPt = new PathElm();
	private PathElm			intersect = new PathElm();
	
	private int				elmCount = 0;
	private int 			matchCount = 0;
	
	private Font 			font;
	private CTM				fontCTM = new CTM(1,0,0,-1,0,0);
	//private String			fontName;
	

	private Bezier			bezier = new Bezier(getPool());

	private float 			currentAlpha = 1.0f;

	private HAlign 			hAlign;
	private VAlign 			vAlign;

	public PSGraphics()
	{
		bezier.setFlatness(flatness);
		
		setlinewidth(this.lineWidth);
	}
	
	//================= Painting Operations =================================
	
	/**
	 * This simply calls the Extruder to extrude the current path.
	 */
	public void fill()
	{
		gl = GLU.getCurrentGL();
		
		//gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, currentColor, 0);
		
		gl.glColor3f(currentColor[0], currentColor[1], currentColor[2]);
		
		extrude(currentPath);
		newpath();
	}

	public void stroke()
	{
		//System.out.println(" elmCount " + elmCount + " matching " + matchCount);
		
		gl = GLU.getCurrentGL();

		// gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, currentColor, 0);
		
		gl.glColor3f(currentColor[0], currentColor[1], currentColor[2]);

		strokePath( true );	
	}
	
	//============================  PSGraphics Construction ==========================
	
	public void setpath ( GeneralPath path )
	{
		newpath();
		appendpath(path);
	}

	public void setpath ( Shape shape )
	{
		newpath();
		appendpath(shape);
	}

	public void appendpath ( GeneralPath path )
	{
		appendPath(path.getPathIterator(AffineTransform.getScaleInstance(1, -1), flatness), null);
	}

	public void appendpath ( Shape shape )
	{
		appendPath(shape.getPathIterator(AffineTransform.getScaleInstance(1,-1), flatness), null);
	}

	private void appendPath ( PathIterator iter, Point2d offset )
	{
		copyPath(iter, offset);
		updateCurrentPoint(currentPath.getLast().type);
	}

	/**
	 * Create a new path by clearing the old, stroked path and returning it.
	 * @return
	 */
	public void newpath ()
	{
		pool.disposePathElm(currentPath);
		//currentPath.clear();
		
		currentPoint.clear();
		
		elmCount = 0;
		matchCount = 0;
	}

	public void arc ( double cx, double cy, double radius, double angleS, double angleF)
	{
		createArc(cx, cy, radius, radius, Math.toRadians(angleS), Math.toRadians(angleF), false, false);
	}

	public void arcn ( double cx, double cy, double radius, double angleS, double angleF)
	{
		createArc(cx, cy, radius, radius, Math.toRadians(angleS), Math.toRadians(angleF), true, false);
	}

	public void arcto( double x1, double y1, double x2, double y2, double radius )
	{
		int 	sgnpsi;
		double 	ang10, ang12, psi, dr, t1x, t1y, cx, cy;
		double 	angs, angf;
		double 	x0, y0;

		// assign local vars to current point
		x0 = currentPoint.x; 
		y0 = currentPoint.y;

		ang10 = Math.PI - Math.atan2(x1 - x0, y1 - y0); // get angle P1 -> P0
		ang12 = Math.PI - Math.atan2(x2 - x1, y2 - y1); // get angle P1 -> P2

		psi = ang10 - ang12; /*  and the included angle */
		if (psi < -Math.PI)
			psi += Constants.TWO_PI; /* correct it */
		if (psi > Math.PI)
			psi -= Constants.TWO_PI;
		
		/* set quadrant "flag" */
		sgnpsi = psi > 0 ? -1 : 1;  //fpsign(0.0 - psi);   return ((arg < 0) ? -1 : 1);

		angs = Math.atan2(x1 - x0, y1 - y0) - Constants.HALF_PI * sgnpsi; /* get perpendicular to P0->P1 */
		angf = Math.atan2(x2 - x1, y2 - y1) - Constants.HALF_PI * sgnpsi; /* get perpendicular to P1->P2 */

		dr = radius / Math.tan(psi / 2.0) * sgnpsi; /* find tangent points */
		t1x = x1 + dr * Math.cos(ang10); /* along first ray */
		t1y = y1 - dr * Math.sin(ang10);
		cx = t1x + radius * Math.sin(ang10) * sgnpsi; //  find center points   
		cy = t1y + radius * Math.cos(ang10) * sgnpsi;

		//t2x = x1 + dr * Math.cos(ang12); //  find tangent points along second ray   
		//t2y = y1 - dr * Math.sin(ang12);

		devLineTo(t1x, t1y); //  draw to tangent-point   

		if (psi < 0.0)
			createArc(cx, cy, radius, radius, angs, angf, false, false);
		else
			createArc(cx, cy, radius, radius, angs, angf, true, false);

		devLineTo(x2, y2); //   and to second tangent-point   
	}

	public void moveto(double x, double y)
	{
		Point2d pt = ctm.transform(x, y);
		devMoveTo( pt );
		updateCurrentPoint(PathType.moveto);
	}

	public void rmoveto(double x, double y)
	{
		PathElm ptCur = currentPath.getLast();
		Point2d pt = ctm.deltaTransform(x, y);
		devMoveTo(pt.x + ptCur.x, pt.y + ptCur.y);		
		updateCurrentPoint(PathType.moveto);
	}

	public void lineto(double x, double y)
	{
		Point2d pt = ctm.transform(x, y);
		devLineTo(pt);		
		updateCurrentPoint(PathType.lineto);
	}

	public void rlineto(double x, double y)
	{
		Point2d pt = ctm.deltaTransform(x, y);
		PathElm ptCur = currentPath.getLast();
		devLineTo(ptCur.x + pt.x, ptCur.y + pt.y);		
		updateCurrentPoint(PathType.lineto);
	}

	public void rcurveto(double x0, double y0, double x1, double y1, double x2, double y2)
	{
		PathElm curpt = currentPath.getLast();
		Point2d pt0 = ctm.deltaTransform(x0, y0);
		Point2d pt1 = ctm.deltaTransform(x1, y1);
		Point2d pt2 = ctm.deltaTransform(x2, y2);
		devCurveTo(curpt.x + pt0.x, curpt.y + pt0.y, curpt.x + pt1.x, curpt.y + pt1.y, curpt.x + pt2.x, curpt.y + pt2.y);	
		updateCurrentPoint(PathType.cubicto);
	}

	public void curveto(double x0, double y0, double x1, double y1, double x2, double y2)
	{
		Point2d pt0 = ctm.transform(x0, y0);
		Point2d pt1 = ctm.transform(x1, y1);
		Point2d pt2 = ctm.transform(x2, y2);
		devCurveTo(pt0, pt1, pt2);	
		updateCurrentPoint(PathType.cubicto);
	}

	public void quadto( double x0, double y0, double x1, double y1 )
	{
		Point2d pt0 = ctm.transform(x0, y0);
		Point2d pt1 = ctm.transform(x1, y1);
		devQuadTo(pt0, pt1);	
		updateCurrentPoint(PathType.cubicto);		
	}

	public void closepath()
	{
		devClosePath();
		updateCurrentPoint(PathType.closepath);
	}
	
	public void rectstroke(double x, double y, double width, double height )
	{
		gsave();
		newpath();
		
		moveto(x, y);
		lineto(x+width, y);
		lineto(x+width, y+height);
		lineto(x, y+height);
		closepath();
		
		stroke();	
		
		grestore();
	}

	public Point2d currentpoint()
	{
		PathElm elm = currentPath.getLast();
		Point2d	pt = itransform(elm.x, elm.y);
		return pt;
	}
	
	/**
	 * Load the current test path from a "PS" file.
	 */
	public boolean loadpath ( InputStream stream )
	{		
	 	Scanner	sc = new Scanner(stream);
		PathElm	moveto = getPool().newPathElm();
	
		newpath();

		double[] pts = new double[6];
		PathElm	prevPt = getPool().newPathElm();
		
 		while (sc.hasNext())
		{
			int knt = 0;
			while (sc.hasNextDouble() && knt < 6)
			{
				pts[knt++] = sc.nextDouble();
			}
				
			PathType type = getPathType(sc);	
			
			if (type == PathType.moveto)
			{
				moveto( pts[0], pts[1]);
				moveto.set(pts[0], pts[1], PathType.moveto);
			}
			else if (type == PathType.lineto)
			{
				lineto( pts[0], pts[1]);
			}
			else if (type == PathType.cubicto)
			{
				curveto( pts[0], pts[1], pts[2], pts[3], pts[4], pts[5]);
			}
			else if (type == PathType.quadto)
			{
				quadto( pts[0], pts[1], pts[2], pts[3]);
			}
			else if (type == PathType.closepath)
			{
				if (!prevPt.equals(moveto))
				{
					closepath();
				}	
			}

			prevPt.set(currentPoint);
		}
		
		dumpPathList("Loaded path", currentPath );

		return true;
	}

	/**
	 *  Simple routine to scan for the PathType enum
	 */
	private PathType getPathType(Scanner sc)
	{
   		for ( PathType current : PathType.values() ) 
   		{
			if (sc.hasNext( PathElm.getLabel(current)))
			{
				sc.next(PathElm.getLabel(current));
				return current;
			}
   		}
   		
   		return PathType.undefined;
	}
	
	
	public void dumppath( GeneralPath path )
	{
		float[] coords = new float[6];
		int		count = 1;
	
		PathIterator pi = path.getPathIterator(AffineTransform.getScaleInstance(1.0, -1.0));
	
		// System.out.println(" dump: winding: " + Winding[currentPath.getWindingRule()]);
	
		while ( !pi.isDone() )
		{
			int type = pi.currentSegment(coords);
			dumpSegment(count++, coords, type );
			
			pi.next();
		}
	}
	
	/**
	 * This just iterates along the path and flattens it.
	 */
	public void flattenpath()
	{		
		bezier.setFlatness(flatness);

		ListIterator<PathElm> 	iter = (ListIterator<PathElm>) currentPath.iterator();
		PathElm		lastElm = null;
		while (iter.hasNext())
		{
			PathElm elm = (PathElm) iter.next();
			
			if (elm.type == PathType.cubicto || elm.type == PathType.quadto)
			{
				iter.remove();
				lastElm = bezier.flattenCurve(iter, lastElm, elm);
				pool.disposePathElm(elm);
			}
			else
				lastElm = elm;
		}
	}

	//=========================== Font Support =================================
	
	/**
	 * Specifies which font to render with this TextRenderer3D
	 * 
	 * @param font
	 *            a font to use for rendering *
	 * @throws java.lang.NullPointerException
	 *             if the supplied font is null
	 */
	public void setfont( String fontName)
	{
		this.font = new Font( fontName, Font.TRUETYPE_FONT, 1);
	}

	/**
	 * Retrieves the Font currently associated with this TextRenderer3D
	 * 
	 * @return the Font in which this object renders strings
	 */
	public String getfont()
	{
		return this.font.getPSName();
	}

	/**
	 *  
	 */
	public void show( String str )
	{
		GlyphVector gv = font.createGlyphVector(new FontRenderContext( AffineTransform.getScaleInstance(1,1), false, true),
				                                new StringCharacterIterator(str));
		GeneralPath gp = (GeneralPath) gv.getOutline();
		
		gp.transform(fontCTM);
		
		Point2d pt = getStringOffset(gp);
			
		pt.x += currentPath.getLast().x;
		pt.y += currentPath.getLast().y;

		gsave();
		newpath();
		
		appendPath(gp.getPathIterator(AffineTransform.getScaleInstance(1,1), flatness), pt);
		
		//dumpPathList("From show command", currentPath);
		
		fill();
		
		grestore();
		
		// TODO need to moveto the end of the string per spec
		
	}	


	public void charpath( String str )
	{		
		GlyphVector gv = font.createGlyphVector(new FontRenderContext( fontCTM, false, true),
				new StringCharacterIterator(str));
		GeneralPath gp = (GeneralPath) gv.getOutline();
		
		Point2d pt = getStringOffset(gp);
	
		appendPath(gp.getPathIterator( fontCTM, flatness), pt);
	}

	public void scalefont ( double scale )
	{
		fontCTM.setToScale(1,-1);
		fontCTM.scale(scale, scale);
	}
	
	public void scalefont (CTM ctm )
	{
		fontCTM.setMatrix(ctm);
		fontCTM.scale(1, -1);
	}
	
	public void setstringalign ( HAlign h, VAlign v )
	{
		hAlign = h;
		vAlign = v;
	}
	
	private Point2d getStringOffset ( GeneralPath gp )
	{
		Rectangle2D rect = gp.getBounds2D();
		
		Point2d pt = new Point2d();
		
		if (vAlign == VAlign.TOP)
			pt.y = -rect.getHeight();
		else
			if (vAlign == VAlign.MIDDLE)
				pt.y = -rect.getHeight() / 2.0;

		if (hAlign == HAlign.RIGHT)
			pt.x = -(rect.getWidth()+rect.getX());
		else
			if (hAlign == HAlign.MIDDLE)
				pt.x = -(rect.getWidth()+rect.getX()) / 2.0;

		return pt;
	}

	/**
	 * Get the bounding box for the supplied string with the current font, etc.
	 * 
	 * @param str
	 * @return
	 */
	public Point2d  stringbbox( String str )
	{
		GlyphVector gv = font.createGlyphVector(new FontRenderContext(AffineTransform.getScaleInstance(1,1), false, true),
				                                new StringCharacterIterator(str));
		GeneralPath gp = (GeneralPath) gv.getOutline();

		Rectangle2D rect = gp.getBounds2D();

		//System.out.println("str: " + str + "  width: " + rect.getWidth() + "  ht: " + rect.getHeight() + " minX: " + rect.getMinX() + " maxX " + rect.getMaxX());
		//System.out.println(" minY: " + rect.getMinY() + " maxY " + rect.getMaxY() + " centerX: " + rect.getCenterX() + " centerY: " + rect.getCenterY());
	
		Point2d pt = fontCTM.deltaTransform(rect.getWidth(), rect.getHeight());
		ctm.inverseDeltaTransform(pt);
		
		return pt;
	}	
	
    public static VAlign convertVAlign( int i ) 
    {
    	for ( VAlign current : VAlign.values() ) 
    	{
	    	if ( current.ordinal() == i ) 
	    	{
	    		return current;
	    	}
    	}
    	return VAlign.BOTTOM;
    }
  
    public static HAlign convertHAlign( int i ) 
    {
    	for ( HAlign current : HAlign.values() ) 
    	{
	    	if ( current.ordinal() == i ) 
	    	{
	    		return current;
	    	}
    	}
    	return HAlign.LEFT;
    }
	
	//============================ Graphics State ==============================

	public void setlinejoin(JoinType type)
	{
		lineJoin.setType(type);		
	}

	public JoinType currentlinejoin()
	{
		return lineJoin.type;		
	}

	public void setlinecap(CapType type)
	{
		lineCap.setType(type);
	}

	public CapType currentlinecap()
	{
		return lineCap.type;
	}

	public void setlinewidth(double lineWidth)
	{
		this.lineWidth = lineWidth;
		this.lineWidthX = Math.abs(lineWidth * this.ctm.getScaleX() / 2.0);
		this.lineWidthY = Math.abs(lineWidth * this.ctm.getScaleY() / 2.0);		
	}

	public double currentlinewidth()
	{
		return this.lineWidth;
	}

	public void setdash( double[] dashArray, double dashOffset )
	{
		this.dashOp.setDash(dashArray, dashOffset);
	}


	public DashOp currentdash()
	{
		return dashOp;
	}
	
	public void sethsbcolor( double h, double s, double b )
	{
		setrgbcolor(Color.HSBtoRGB((float)h,(float)s, (float)b));
	}

	public void setrgbcolor( double r, double g, double b )
	{
	}

	public void setrgbcolor( int rgb )
	{
		currentColor[0] = (rgb >> 16) / 255.0f;
		currentColor[1] = ((rgb >> 8) & 0xff) /255.0f;
		currentColor[2] = (rgb & 0xff) / 255.0f;
		currentColor[3] = currentAlpha;
	}

	public int currentrgbcolor()
	{
		int	r = (int)(currentColor[0] * 255) << 16;
		int	g = (int)(currentColor[1] * 255) << 8;
		int	rgb = (int)(currentColor[2] * 255) + r + g;
		return rgb;
	}
	
	public void setmatrix( CTM ctm )
	{
		this.ctm.setMatrix(ctm);
		// update linewidth as the components are dependent on the current matrix
		setlinewidth(this.lineWidth);
	}

	public CTM currentmatrix()
	{
		return this.ctm;
	}
	
	public void pushmatrix()
	{
		ctmStack.add( getPool().newCTM(this.ctm) );
	}

	public void popmatrix()
	{
		if (ctmStack.size() > 0)
		{
			CTM oldCTM = ctmStack.removeLast();
			this.ctm.setMatrix(oldCTM);
			pool.disposeCTM(oldCTM);
		}
	}
	
	public void scale ( double sx, double sy )
	{
		ctm.scale(sx, sy);
	}

	public void rotate ( double angle )
	{
		ctm.rotate(angle);
	}

	public void translate ( double tx, double ty )
	{
		ctm.translate(tx, ty);
	}

	public Point2d transform ( double x, double y )
	{
		return this.ctm.transform(x,y);
	}

	public void transform ( Point2d pt )
	{
		this.ctm.transform(pt);
	}

	public Point2d itransform ( double x, double y )
	{
		return this.ctm.inverseTransform(x,y);
	}
	
	public void itransform ( Point2d pt )
	{
		this.ctm.inverseTransform(pt);
	}

	public void dtransform ( Point2d pt )
	{
		this.ctm.deltaTransform(pt);
	}

	public void idtransform ( Point2d pt )
	{
		this.ctm.inverseDeltaTransform(pt);
	}

	public void gsave()
	{
		GState gstate = new GState(this);
		gstate.currentPath = copyPath();
		gstateStack.add(gstate);
	}
	
	public void grestore()
	{
		GState gstate = gstateStack.removeLast();
		
		setlinewidth(gstate.lineWidth);
		setlinecap(gstate.lineCap.type);
		setlinejoin(gstate.lineJoin.type);
		dashOp.set(gstate.dashOp);
		this.ctm.setTransform(gstate.ctm);
	}
	
	//==================== Private Drawing Routines ==================================
	

	private boolean coincident ( double x, double y )
	{
		return (Math.abs(currentPoint.x - x) < Constants.PATH_TOLERANCE) &&
				( Math.abs(currentPoint.y - y) < Constants.PATH_TOLERANCE);
	}

	private void updateCurrentPoint( PathType type )
	{
		currentPoint.set( currentPath.getLast());
		// System.out.println( String.format("%6.2f",x) + " " + String.format("%6.2f",y) + " " + type.toString() );
	}

	private void devMoveTo( double x, double y ) 
	{
		//System.out.println("devMoveTo: " + String.format("%6.4f",x) + " " + String.format("%6.4f",y));
		//elmCount++;
		
		if (currentPoint.type == PathType.moveto)
		{
			currentPoint.x = x;
			currentPoint.y = y;
			currentPath.getLast().set(currentPoint);
		}
		else
		{
			currentPath.add(getPool().newPathElm(x, y, PathType.moveto));
			updateCurrentPoint(PathType.moveto);
			pathBegin.set(currentPoint);
		}
	}

	private void devMoveTo(Point2d pt)
	{
		devMoveTo(pt.x, pt.y);
		//System.out.println("devMoveTo: " + String.format("%6.4f",pt.x) + " " + String.format("%6.4f",pt.y));
		//elmCount++;
		//currentPath.add(pool.newPathElm(pt.x, pt.y, PathType.moveto));
		//updateCurrentPoint(PathType.moveto);
		//pathBegin.set(currentPoint);
	}

	private void devLineTo(Point2d pt)
	{
		//System.out.println("devLineTo: " + String.format("%6.4f",pt.x) + " " + String.format("%6.4f",pt.y));
		elmCount++;
		if (coincident(pt.x, pt.y))
		{
			matchCount++;
			//System.out.println("Coincident points!");
			return;
		}
		
		currentPath.add(getPool().newPathElm(pt.x, pt.y,PathType.lineto));	
		updateCurrentPoint(PathType.lineto);
	}

	private void devLineTo( double x, double y )
	{
		//System.out.println("devLineTo: " + String.format("%6.4f",x) + " " + String.format("%6.4f",y));
		elmCount++;
		if (coincident(x, y))
		{
			matchCount++;
			//System.out.println("Coincident points!");
			return;
		}

		currentPath.add(getPool().newPathElm(x, y,PathType.lineto));	
		updateCurrentPoint(PathType.lineto);
	}

	private void devClosePath()
	{
		//System.out.println("devClosePath: " + String.format("%6.4f",pathBegin.x) + " " + String.format("%6.4f",pathBegin.y));
		currentPath.add(getPool().newPathElm(pathBegin.x, pathBegin.y, PathType.closepath));
	}
	
	private void devCurveTo(Point2d pt0, Point2d pt1, Point2d pt2)
	{
		currentPath.add(getPool().newCubicElm( pt0.x, pt0.y, pt1.x, pt1.y, pt2.x, pt2.y, PathType.cubicto ));
	}

	private void devCurveTo(double x0, double y0, double x1, double y1, double x2, double y2 )
	{
		currentPath.add(getPool().newCubicElm(x0, y0, x1, y1, x2, y2, PathType.cubicto ));
	}

	@SuppressWarnings("unused")
	private void devQuadTo(double x0, double y0, double x1, double y1)
	{
		currentPath.add(getPool().newQuadElm( x0, y0, x1, y1, PathType.quadto ));
	}

	private void devQuadTo( Point2d pt0, Point2d pt1 )
	{
		currentPath.add(getPool().newQuadElm( pt0.x, pt0.y, pt1.x, pt1.y, PathType.quadto));		
	}

	/**
	 * All this does for now is reset the first-segment edges
	 * for the closepath on dashes
	 */
	private void init()
	{
		revOrig.clear();
		fwdOrig.clear();	
	}


	/**
	 *  This takes a list of path elms and splits it into one or more sub-paths, 
	 *  each of which it then stroked using the current PSGraphics settings.
	 * 
	 *  This is called from two points, strokePath and stroke operator.
	 *  If called from strokepath, then the resulting path REPLACES the 
	 *  current path.  If called from stroke it generates the extruded 
	 *  path, but the path is unchanged (i.e. restored).
	 */
	private void strokePath ( boolean strokeOp )
	{
		boolean  moveTo = true;
		
		try
		{	
			init();

			// now save the current path if we're an extrude i.e. extrude == true
			// if extrude is false we don't need to save/restore the currentPath
			srcPath = currentPath;
			LinkedList<PathElm> savePath = null;
			if (strokeOp)
			{
				savePath = copyPath();
			}
				
			currentPath = new LinkedList<PathElm>();
			
			Iterator<PathElm> iter = srcPath.iterator();
			if (!iter.hasNext())
				return;
	
			Dash 	dash = dashOp.getFirst();
			boolean dashing = dashOp.getDash().size() > 1;
			
			while (!srcPath.isEmpty())
			{
				if (strokeOp)
					newpath();
	
				boolean dashed = getSubPath( pool.newDash(dash) );
				
				/*
				System.out.println("The dash: " + dash );
				dumpPathList("The subpath: ",subPathList);
				dumpPathList("The root path: ", pathList);
				*/
				
				if (!dash.gap)
				{
					// System.out.println("PSGraphics Fwd side ------------------");
					moveTo = strokeFwdSide( subPath, fwdOrig, dashing );
					
					// System.out.println("PSGraphics Rev side ------------------");
					strokeRevSide( subPath, moveTo, revOrig, dashing);
					
					// dumppath();
					
					//System.out.println("strokePath:  elmCount " + elmCount + " matching " + matchCount);

					if (strokeOp)
					{
						//dumpPathList("extruding currentpath", currentPath);
						flattenpath();
						//dumpPathList("currrentPath after flattening",currentPath);
						//prunePath();
						extrude(currentPath);
						pool.disposePathElm(currentPath);
					}
				}
				
				if (dashed)
				{
					dash = dashOp.next();
				}	
				else
				{
					init();
				}
				
				pool.disposePathElm(subPath);
				pool.disposeDash(dash);
			}	
			
			if (strokeOp)
				currentPath = savePath;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Generates a deep copy of the currentPath
	 * 
	 */
	private LinkedList<PathElm> copyPath ()
	{
		LinkedList<PathElm> list = new LinkedList<PathElm>();	
	
		ListIterator<PathElm> iter = currentPath.listIterator();
		
		while (iter.hasNext())
		{
			PathElm elm = (PathElm) iter.next();
			if (elm.type == PathType.cubicto)
				list.add(getPool().newCubicElm((CubicElm) elm));
			else if (elm.type == PathType.quadto)
				list.add(getPool().newQuadElm((QuadElm) elm));
			else
				list.add(getPool().newPathElm(elm));
		}
		
		return list;
	}

	/**
	 * This fetches the next subpath.  It does so by cutting each valid elm 
	 * from the first path and adding each to the new subpath.  THe result is a new 
	 * subpath and the original path now begins just after the end of the 
	 * subpaths's position in the original path.
	 * @param dash 
	 */
	private boolean getSubPath ( Dash dash )
	{		
		Iterator<PathElm> iter = srcPath.iterator();
		if (!iter.hasNext())
			return false;
		
		PathElm curElm = (PathElm) iter.next();
		
		if (curElm.type != PathType.moveto)
			return false;
		
		subPath.clear();
	 	subPath.add(curElm);
		iter.remove();
		
		PathElm 	prevElm = curElm;
 		
		while (iter.hasNext())
		{
			curElm = (PathElm) iter.next();

			if (curElm.type == PathType.undefined)
					throw new RuntimeException();
			
			if (curElm.type == PathType.moveto)
				break;

			double len = Math.hypot(curElm.x-prevElm.x,curElm.y-prevElm.y);
			
			if (len >= dash.length)
			{
				splitStrokeAtDash( dash, prevElm, curElm );
				return true;
			}
			
			subPath.add(curElm);
			iter.remove();
			
			dash.length -= len;
			prevElm = curElm;
		}
		
		return false;
	}

	/**
	 * Break the current line, inserting a new moveto at the head of the source
	 * list and a new lineto to the end of the subpath
	 * @param dash
	 * @param prev
	 */
	private void splitStrokeAtDash( Dash dash, PathElm prev, PathElm curElm )
	{
		double angle = Math.atan2(curElm.y - prev.y, curElm.x - prev.x );
		
	 	double x = prev.x + Math.cos(angle) * dash.length;
		double y = prev.y + Math.sin(angle) * dash.length;
		
		if (srcPath.getFirst().equals(x,y))
			srcPath.getFirst().type = PathType.moveto;
		else
			srcPath.addFirst(getPool().newPathElm( x, y, PathType.moveto ));
		
		subPath.add( getPool().newPathElm( x, y, PathType.lineto ));
	}

	/** 
	 * This copies the specified GeneralPath into the LinkedList form for stroking.  It uses 
	 * a flattening path iterator, so it will get flattened if it hasn't already.
	 */
	private void copyPath ( PathIterator iter, Point2d offset )
	{
		// dumpPath();
		
		//pathList.clear();
	
		PathElm	moveto = getPool().newPathElm();
		PathElm prevPt = getPool().newPathElm();
		float[] coords = new float[6];
				
		while ( !iter.isDone() )
		{
			PathElm elm = getPool().newPathElm();
		
			int type = iter.currentSegment(coords );
			elm.set(coords[0], coords[1], PathType.undefined);
			if (offset != null)
			{
				elm.x += offset.x;
				elm.y += offset.y;
			}
			
			switch( type )
			{
				case PathIterator.SEG_MOVETO:
					moveto( elm.x, elm.y );
					elm.type = PathType.moveto;
					moveto.set(elm);
					prevPt = elm;
					break;
				case PathIterator.SEG_LINETO:
					if (!prevPt.equals(elm))
					{
						elm.type = PathType.lineto;
						prevPt = elm;
						lineto(elm.x, elm.y);
					}
					break;
				case PathIterator.SEG_CLOSE:
					elm.type = PathType.closepath;

					// make sure we aren't duplicating points. closepath is special - 
					// we need to keep it so we set the previous lineto to be a closepath				
					if (prevPt.equals(moveto))
					{
						if (prevPt.type == PathType.lineto)
						{
							prevPt.type = PathType.closepath;
							currentPath.getLast().type = PathType.closepath;
						}
					}
					else
					{
						closepath();
					}	
				break;
			}
			
			iter.next();
		}	

		pool.disposePathElm(moveto);
		pool.disposePathElm(prevPt);

		// dumpPathList("Raw path ", pathList);
	}

	/**
	 * This strokes the forward side (i.e. starting at the beginnning) of the specified 
	 * path.  It walks along the path, generating the  joins.  The joins are really 
	 * all there is to the path - the "sides" are implicit.
	 */
	private boolean strokeFwdSide ( LinkedList<PathElm> path, Edge orig, boolean dashed )
	{
		if (path.isEmpty() || path.getFirst().type != PathType.moveto)
			return false;
		
		previousPt.clear();
		previousPt.x = Constants.MAX_FLOAT;
		
		ListIterator<PathElm> iter = path.listIterator();
		edge0.setP0(getNext(iter));
		//edge0.setP1(getNext(iter));
		edge0.setP1(iter.hasNext() ? getNext(iter) : edge0.getP0());
		//edge0.setAngle();
		edge1.set(edge0);
		boolean 	closepath = edge0.getP1().type == PathType.closepath;
		boolean		moveTo = true;

		//System.out.println("strokeFwdSide - fwdOrigin: " + fwdOrig + " edge0: " + edge0);

		if (fwdOrig.isClear())
		{
			fwdOrig.set(edge0);
			//System.out.println("fwdOrigin: " + fwdOrig + " edge0: " + edge0);
		}

		while (iter.hasNext())
		{
			edge1.set(edge0.getP1(), getNext(iter));

			// closepath = edge1.getP1().type == PathType.closepath;			
			if (edge1.getP1().type == PathType.closepath)
			{
				closepath = true;
				
				// if the prev pt isn't on the moveto, we need to add another side/join
				if (!edge1.getP0().equals(fwdOrig.getP0()))
				{
					edge1.setP1(fwdOrig.getP0());
				}
				else
				{
					edge1.set(edge0);
					break;
				}
			}
			createJoin( moveTo, edge0, edge1 );
			
			moveTo = false;
						
			edge0.set(edge1);
		}
	 
		if (closepath && !dashed)
		{
			createJoin( moveTo, edge1, fwdOrig );
			closepath();
			moveTo = true;
		}
		else
		{
			createCap( moveTo, edge1 );
			moveTo = false;
		}
	

		return moveTo; 
	}
	
	/**
	 * This strokes the reverse side of the path (i.e. going backward from the end), 
	 * though since it goes from back to front, it appears to also be stroking the 
	 * "left side".
	 * @param dashed 
	 */
	private boolean strokeRevSide ( LinkedList<PathElm> path, boolean moveTo, Edge original, boolean dashed )
	{
		if (path.size() < 1)
			return false;
		
	 	ListIterator<PathElm> iter = path.listIterator(path.size());
		PathElm		last = (PathElm) iter.previous();
		boolean 	closepath = last.type == PathType.closepath;
		edge0.set( last, iter.hasPrevious() ? getPrevious(iter) : edge0.getP0() );
		previousPt.set( edge0.getP0());
		//edge0.setAngle();
		edge1.set(edge0);
		
		if (original.isClear())
		{
			revOrig.set(edge0);
		}
				
		while ( iter.hasPrevious() )
		{
			edge1.set(edge0.getP1(), getPrevious(iter));
			
			// closepath = edge1.getP1().type == PathType.closepath;
			if (edge1.getP1().type == PathType.closepath)
			{
				closepath = true;
	
				// if the prev pt isn't on the moveto, we need to add another side/join
				if (!edge1.getP0().equals(revOrig.getP0()))
				{
					edge1.setP1(revOrig.getP0());
				}
				else
					break;
			}
			
			createJoin( moveTo, edge0, edge1 );
			
			moveTo = false;
			
			edge0.set(edge1);
		}
	
		if (closepath && !dashed)
		{
			createJoin( moveTo, edge1, revOrig );
		}
		else
		{
			createCap( closepath ? false : moveTo, edge1 );
		}

		// always close after stroking the right side
		closepath();
		
		return true;
	}
	
	/**
	 * A simple method to fetch the previous point in the sub-path
	 * It checks that the point isn't coincident with the previous
	 * point.  If they are the same, the new one is chucked and
	 * another fetched.
	 */
	private PathElm getPrevious ( ListIterator<PathElm> iter )
	{
		boolean 	samePt = true;
		PathElm		pathElm = null;
		
		while (iter.hasPrevious() && samePt)
		{
			pathElm = (PathElm) iter.previous();
			samePt = pathElm.equals(previousPt);
		}
		
		previousPt.set(pathElm);

		return pathElm;
	}
	
	/**
	 * A simple method to fetch the next point in the subpath
	 * It checks that the point isn't coincident with the previous
	 * point.  If they are the same, the new one is chucked and
	 * another fetched.
	 */
	private PathElm getNext ( ListIterator<PathElm> iter )
	{
		boolean 	samePt = true;
		PathElm		pathElm = null;
		
		while (iter.hasNext() && samePt)
		{
			pathElm = (PathElm) iter.next();
			samePt = pathElm.equals(previousPt);
		}
		
		previousPt.set(pathElm);

		return pathElm;
	}
	
	/**
	 * The general entry point to generate a cap
	 */
	private void createCap( boolean moveTo, Edge edge )
	{
		// System.out.println("Creating a " + lineCap.type.toString() + " cap on edge: " + edge);
		
		createOffsetEdge(offEdge0, edge, true);
		createOffsetEdge(offEdge1, edge, false);

		if ( moveTo )
			devMoveTo( offEdge0.getP1().x, offEdge0.getP1().y );
		else
			devLineTo( offEdge0.getP1().x, offEdge0.getP1().y );

		if (lineCap.type == CapType.Project)
			createProjectCap( moveTo, edge );
		else if (lineCap.type == CapType.Round)
			createRoundCap( moveTo, edge );
		else
			createButtCap( moveTo, edge );
	}

	/**
	 *  Round cap creation.  Just create a 180 degree cap.
	 */
	private void createRoundCap(boolean moveTo, Edge edge)
	{
 		double angleS = Math.atan2(offEdge0.getP1().y - edge.getP1().y, offEdge0.getP1().x - edge.getP1().x );

		createArc(edge.getP1().x, edge.getP1().y, lineWidthX, lineWidthY, angleS, angleS + Math.PI, true, true );

	}

	/**
	 * Simple project cap.  Just extend to the two offset lines a half a strokewidth.
	 */
	private void createProjectCap(boolean moveTo, Edge edge)
	{
		double x0 = offEdge0.getP1().x + Math.cos(offEdge0.getAngle()) * lineWidthX;
		double y0 = offEdge0.getP1().y + Math.sin(offEdge0.getAngle()) * lineWidthY;
		
		double x1 = offEdge1.getP1().x + Math.cos(offEdge1.getAngle()) * lineWidthX;
		double y1 = offEdge1.getP1().y + Math.sin(offEdge1.getAngle()) * lineWidthY;

		devLineTo(x0, y0);
		devLineTo(x1, y1);
		devLineTo( offEdge1.getP1().x, offEdge1.getP1().y );
	}

	/**
	 * Create the simple butt cap.  this consists of simply drawing the line
	 * to the other side of the pSGraphics.
	 */
	private void createButtCap( boolean moveTo, Edge edge)
	{
		devLineTo( offEdge1.getP1().x, offEdge1.getP1().y );
	}

	/**
	 * This actually generates the join by calling the appropriate join-style 
	 * method which actually generates the path points. 
	 */
	private void createJoin( boolean moveTo, Edge edge0, Edge edge1)
	{
		createOffsetEdge(offEdge0, edge0, true);
		createOffsetEdge(offEdge1, edge1, true);

		//System.out.println("Creating " + lineJoin.type.toString() + " join on edge0: "  + edge0 +  " edge1: " + edge1);
		//System.out.println("Angle is " + String.format("%6.2f",edge0.getIncludeAngle(edge1)) + " and angle is " + (edge0.isObtuse(edge1) ? "obtuse" : "convex"));

		// note that the PostScript manual states that if the angle of the miter is
		// less than the limit then just do a bevel join
		if (!edge0.isObtuse(edge1))
			createConvexJoin(moveTo, edge0, edge1);
		else
		{
			if (moveTo)
				devMoveTo( offEdge0.getP1().x, offEdge0.getP1().y);
			else
				devLineTo( offEdge0.getP1().x, offEdge0.getP1().y);

			if (lineJoin.getType() == JoinType.Bevel
					|| (lineJoin.getType() == JoinType.Miter && edge0.getIncludeAngle(edge1) > lineJoin.getMiterLimit()))
				createBevelJoin(moveTo, edge0, edge1);
			else if (lineJoin.getType() == JoinType.Miter)
				createMiterJoin(moveTo, edge0, edge1);
			else
				createRoundJoin(moveTo, edge0, edge1);
		}
	}

	/**
	 * Create the verticies that constitute the bevel join.
	 */
	private void createBevelJoin( boolean moveTo, Edge edge0, Edge edge1 )
	{				
		devLineTo( offEdge1.getP0().x, offEdge1.getP0().y );
	}

	/**
	 * Create the verticies that constitute the miter join.
	 *
	 */
	private void createMiterJoin(boolean moveTo, Edge edge0, Edge edge1 )
	{
		double 	BIG_FP = 1e6;
		
		miterEdge0.set(offEdge0);
		miterEdge1.set(offEdge1);

		miterEdge0.getP1().x = (offEdge0.getP1().x + Math.cos(offEdge0.getAngle()) * BIG_FP);
		miterEdge0.getP1().y = (offEdge0.getP1().y + Math.sin(offEdge0.getAngle()) * BIG_FP);
		miterEdge0.setAngle();
		
		miterEdge1.getP0().x = offEdge1.getP0().x + Math.cos(offEdge1.getAngle() + Math.PI) * BIG_FP;
		miterEdge1.getP0().y = offEdge1.getP0().y + Math.sin(offEdge1.getAngle() + Math.PI) * BIG_FP;
		miterEdge1.setAngle();

		//System.out.println("Miter join:   edge0: "  + miterEdge0 +  " edge1: " + miterEdge1);

		if (!miterEdge0.intersect(miterEdge1, intersect))
			throw new RuntimeException("Whoa!  Miter join didn't have an intersection?!");

		devLineTo( intersect.x, intersect.y );
		
		devLineTo( offEdge1.getP0().x, offEdge1.getP0().y );	
	}

	/**
	 * Create the verticies that constitute the round join.
	 */
	private void createRoundJoin(boolean moveTo, Edge edge0, Edge edge1)
	{
		double angleS = Math.atan2(offEdge0.getP1().y - edge0.getP1().y, offEdge0.getP1().x - edge0.getP1().x );
		double angleF = Math.atan2(offEdge1.getP0().y - edge1.getP0().y, offEdge1.getP0().x - edge1.getP0().x );
						
		createArc(edge0.getP1().x, edge0.getP1().y, lineWidthX, lineWidthY, angleS, angleF, true, true);
	}

	/**
	 * All convex joins (round, bevel, miter) are the same when convex. 
	 * So just figure compute the intersection of the two offset edges
	 */
	private void createConvexJoin(boolean moveTo, Edge edge0, Edge edge1 )
	{
	 	
	 	/** in some cases where the pSGraphics is very wide relative to the 
	 	 *  length to  the offset edges don't intersect, it
	 	 */
	 	if (!offEdge0.intersect(offEdge1, intersect))
	 	{
			if (moveTo)
				devMoveTo(offEdge0.getP1().x, offEdge0.getP1().y);
			else
				devLineTo( offEdge0.getP1().x, offEdge0.getP1().y);

			devLineTo( edge1.getP0().x, edge1.getP0().y );
	
			devLineTo( offEdge1.getP0().x, offEdge1.getP0().y );

	 	//	createBevelJoin( moveTo, edge0, edge1);
	 		return;
	 	}
		
	 	// System.out.println("ConvexJoin: x,y: " + intersect.x + " " + intersect.y + "\n");

		if (moveTo)
			devMoveTo( intersect.x, intersect.y );
		else
			devLineTo( intersect.x, intersect.y );
	}
	
	/**
	 * Creates a new edge which is offset from the first edge by half the
	 * strokewidth
	 */
	private void createOffsetEdge( Edge offEdge, Edge edge, boolean left )
	{		
		offsetXY(offEdge.getP0(), edge.getP0(), edge.getAngle(), left);
        offsetXY(offEdge.getP1(), edge.getP1(), edge.getAngle(), left);
        offEdge.setAngle();
        
		//System.out.println("Offset edge: " + offEdge);
	}
	
	/**
	 * This finds the point offset to one side (left-hand) of the specified 
	 * point as a function of the path direction (angle) and the current
	 * linewidth.
	 */
	private void offsetXY ( PathElm offElm, PathElm p0, double angle, boolean left )
	{
		//PathElm elm = pool.newPathElm();
		double	offAngle = left ? angle+Constants.HALF_PI : angle-Constants.HALF_PI;

		//System.out.println("offsetXY:   offElm: "  + offElm +  " p0: " + p0 + " angle: " + angle);

		offElm.x = p0.x + lineWidthX * Math.cos(offAngle);
		offElm.y = p0.y + lineWidthY * Math.sin(offAngle);

		//System.out.println("offsetXY:   offElm: "  + offElm );

		//return elm;
	}
	
	/**
	 * Creates a circular arc of the specified angles and radius around the 
	 * specified point.  Generates up to 5 Bezier curves that approximate the 
	 * arc.  It does not generate an inital point nor a closepath
	 */
	protected void createArc( double cx, double cy, double radiusX, double radiusY, double angleS, double angleF, boolean clockwise, boolean deviceSpace )
	{			
		// make angles positive and less than equal to TWO_PI
		angleS = clampAngle(angleS); 
		angleF = clampAngle(angleF);  
		
		// if the start and end points are essentially the same and it is 
		// going backwards, just bail - there is nothing to draw
		double sweepAngle = (angleF - angleS) * (clockwise ? -1 : 1);
		if ( sweepAngle == -Constants.TWO_PI)
				return;
		
		// set up vars that are different in CW vs CCW.
		boolean SvsF = clockwise ? (angleS < angleF) : (angleS > angleF);
		boolean	startsAt0 = clockwise ? angleF == 0  : angleS == 0; 
		int		modIncr  = clockwise? 7 : 1;		
	
		double	nextAngle = getNextAngle(angleS, clockwise);
		int 	nextQuad = (tinyRound(angleS / Constants.HALF_PI)) % 4;
		int 	lastQuad = (tinyRound(angleF / Constants.HALF_PI)) % 4;
		boolean sameQuad = nextQuad == lastQuad && (SvsF || sweepAngle == Constants.TWO_PI);

		// handle the special case of a full circle which starts at the origin
		if (startsAt0 && sweepAngle == Constants.TWO_PI)
			nextQuad = (nextQuad + modIncr) % 4;
		else
			if (angleS == 0 && clockwise)
				nextQuad = (nextQuad + modIncr) % 4;

		// first do the intervening curves
		while (nextQuad != lastQuad || sameQuad)
		{
			bezierArc(cx, cy, radiusX, radiusY, angleS, nextAngle, clockwise, deviceSpace);
			angleS = copyStartAngle(nextAngle, clockwise);
			nextAngle = getNextAngle(nextAngle, clockwise);
			nextQuad = (nextQuad + modIncr) % 4;
			sameQuad = false;
		}

		// and then the final quadrant
		bezierArc(cx, cy, radiusX, radiusY, angleS, angleF, clockwise, deviceSpace);
	}

	/**
	 * THe standard Java round is floor(d+0.5).  We don't want that in
	 * this case, but we do want numbers that are ALMOST on an integer
	 * bound to return that value.
	 */
	private int tinyRound(double d)
	{	
		return (int) Math.floor(d + Constants.FP_ROUNDING_TOLERANCE);
	}

	/**
	 * We need to wrap the start angle around.
	 */
	private double copyStartAngle(double nextAngle, boolean clockwise )
	{
		if (clockwise && nextAngle == 0)
			nextAngle = Constants.TWO_PI;
		else if (nextAngle == Constants.TWO_PI)
				nextAngle = 0;

		return nextAngle;
	}

	/**
	 * Returns the end of the next quadrant along the circle from the 
	 * current angle, ensuring that the angle is in the range 0.. 2PI
	 */
	private double getNextAngle(double nextAngle, boolean clockwise)
	{
		if (clockwise)
			nextAngle = (Math.floor((nextAngle-TINY_ANGLE)/Constants.HALF_PI)) * Constants.HALF_PI;
		else
			nextAngle = (Math.ceil((nextAngle+TINY_ANGLE)/Constants.HALF_PI)) * Constants.HALF_PI;

		return clampAngle( nextAngle );
	}

	private double clampAngle( double angle )
	{
		if (angle < 0.0)
			angle += Constants.TWO_PI;
		else if (angle >= Constants.TWO_PI)
			angle -= Constants.TWO_PI;
		
		return angle;
	}

	/**
	 *  This procedure calculates the bezier control points for a given arc 
	 *  The information for this algorithm is covered several places, including:
	 *    http://www.tinaja.com/glib/ellipse4.pdf
	 *  and
	 *    http://www.spaceroots.org/documents/ellipse/elliptical-arc.pdf
	 *  
	 */
	private void bezierArc(double cx, double cy, double radiusX, double radiusY, double angleS, double angleF, boolean clockwise, boolean deviceSpace )
	{
		final double 	MAGIC_NUMBER = 0.0551784;	//  0.054242343;
		double arcFactor, deltAng;

		// System.out.println("bezierArc: angS: " + Math.toDegrees(angleS) + " angF: " + Math.toDegrees(angleF) + (clockwise ? " clockwise":"counter-clockwise"));
		
		// make angles positive and less than equal to TWO_PI
		angleS = clampAngle(angleS); 
		angleF = clampAngle(angleF);  

		deltAng = angleF - angleS;
		
		// if the start angle is zero we can get overranges, so correct for them
		if (deltAng < -Constants.HALF_PI)
			deltAng += Constants.TWO_PI;
		else
			if (deltAng > Constants.HALF_PI)
				deltAng -= Constants.TWO_PI;
 
		if (Math.abs(deltAng) > TINY_ANGLE)
		{
			arcFactor = deltAng * (1.0 + Math.pow(deltAng / Constants.HALF_PI, 2.0) * MAGIC_NUMBER) / 3.0; 

			// P1
			double x1 = (Math.cos(angleS) - Math.sin(angleS) * arcFactor) * radiusX + cx;
			double y1 = (Math.sin(angleS) + Math.cos(angleS) * arcFactor) * radiusY + cy;
			// P2
			double x2 = (Math.cos(angleF) + Math.sin(angleF) * arcFactor) * radiusX + cx;
			double y2 = (Math.sin(angleF) - Math.cos(angleF) * arcFactor) * radiusY + cy;
			 // P3
			double x3 = Math.cos(angleF) * radiusX + cx;
			double y3 = Math.sin(angleF) * radiusY + cy;
			
			if (deviceSpace)
				devCurveTo(x1, y1, x2, y2, x3, y3);
			else
				curveto(x1, y1, x2, y2, x3, y3);
				
			
		//	System.out.println("bezarc: p1: " + String.format("%6.2f",x1) + " " + String.format("%6.2f",y1) + 
		//			" p2: " + String.format("%6.2f",x2) + " " + String.format("%6.2f",y2) + " p3: " + String.format("%6.2f",x3) + " " + String.format("%6.2f",y3) );

		}
	}

	
	/**
	 * Simple routine to dump the contents of a path to the System.out
	 * 
	 * @param path
	 */

	private void dumpPathList ( String label, LinkedList<PathElm> path )
	{
		System.out.println(label);
		
		Iterator<PathElm> iter = path.iterator();
		if (!iter.hasNext())
		{
			System.out.println("Specified path is empty!");
		}

		while (iter.hasNext())
		{
			PathElm elm = (PathElm) iter.next();
			System.out.print(" " + String.format("%6.4f",elm.x) + " " + String.format("%6.4f",elm.y));
			System.out.println( " " + PathElm.getLabel(elm.type));
		}
	}
	private static String Segment[] = { "MoveTo", "LineTo", "QuadTo", "CubicTo", "Close" };
	//private static String Winding[] = { "EvenOdd", "NonZero" };
	
	protected void dumpSegment( int num, float[] points, int type )
	{
		
	//	System.out.print(num + " " );

		switch(type)
		{
		case PathIterator.SEG_MOVETO:
		case PathIterator.SEG_LINETO:
			System.out.print(" " + String.format("%6.2f",points[0]) + "," + String.format("%6.2f",points[1]) );
			break;
			
		case PathIterator.SEG_QUADTO:
			System.out.print(" " + String.format("%6.2f",points[0]) + "," + String.format("%6.2f",points[1]) + "  " + 
					String.format("%6.2f",points[2]) + "," + String.format("%6.2f",points[3]) );
			break;
			
		case PathIterator.SEG_CUBICTO:
			System.out.print(" " + String.format("%6.2f",points[0]) + "," + String.format("%6.2f",points[1]) + "  " + 
					String.format("%6.2f",points[2]) + "," + String.format("%6.2f",points[3]) + "  " + String.format("%6.2f",points[4]) + "," + String.format("%6.2f",points[5])  );
			break;
		}

		System.out.println( " " + Segment[type]);
	}

	public void setPool(Pool pool)
	{
		this.pool = pool;
	}

	public Pool getPool()
	{
		return pool;
	}

}

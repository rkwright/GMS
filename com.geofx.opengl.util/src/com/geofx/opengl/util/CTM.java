/*******************************************************************************
 * Copyright (c) 2008 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 *******************************************************************************/

package com.geofx.opengl.util;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

import javax.vecmath.Point2d;

import com.geofx.opengl.util.CTM;

public class CTM extends AffineTransform
{
    private double[] 			pts = new double[4];
    private AffineTransform		invmat;

	public CTM()
	{
		setInverseCTM();
	}

	/**
	 * Copy ctor
	 */
	public CTM( CTM ctm )
	{
		super(ctm);
		setInverseCTM();
	}

	public CTM( double m00, double m01, double m02, double m10, double m11, double m12 )
	{
		super( m00, m01, m02, m10, m11, m12 );
		setInverseCTM();
	}

	public void setMatrix (CTM ctm)
	{
		this.setToIdentity();
		this.concatenate(ctm);
		setInverseCTM();
	}
	
	public void scale (double sx, double sy )
	{
		super.scale(sx, sy);
		setInverseCTM();
	}
	
	public void rotate (double angle )
	{
		super.rotate(Math.toRadians(angle));
		setInverseCTM();
	}

	public void translate (double tx, double ty )
	{
		super.translate(tx, ty);
		setInverseCTM();
	}

	private void setInverseCTM()
	{
		try
		{
			invmat = this.createInverse();
		}
		catch (NoninvertibleTransformException e)
		{
			e.printStackTrace();
		}
	}
	
	/*
	public void identity( Matrix3d mat )
	{
		mat.setZero();
		mat.m00 = 1;
		mat.m11 = 1;
		mat.m22 = 1;
	}
*/
	
	public Point2d transform(double x, double y)
	{
		pts[0] = x;
		pts[1] = y;
		this.transform(pts, 0, pts, 2, 1);
		return new Point2d( pts[2], pts[3]);
	}

	public void transform( Point2d pt )
	{
		pts[0] = pt.x;
		pts[1] = pt.y;
		this.transform(pts, 0, pts, 2, 1);
		pt.x = pts[2];
        pt.y = pts[3];
	}

	public Point2d inverseTransform(double x, double y)
	{
		pts[0] = x;
		pts[1] = y;
		
		try
		{
			this.inverseTransform(pts, 0, pts, 2, 1);
		}
		catch (NoninvertibleTransformException e)
		{
			e.printStackTrace();
		}
		
		return new Point2d( pts[2], pts[3]);
	}

	public void inverseTransform( Point2d pt )
	{
		pts[0] = pt.x;
		pts[1] = pt.y;
		
		try
		{
			this.inverseTransform(pts, 0, pts, 2, 1);
		}
		catch (NoninvertibleTransformException e)
		{
			e.printStackTrace();
		}
		
		pt.x = pts[2];
        pt.y = pts[3];
	}

	public Point2d deltaTransform(double x, double y)
	{
		pts[0] = x;
		pts[1] = y;
		
		this.deltaTransform(pts, 0, pts, 2, 1);

		return new Point2d( pts[2], pts[3]);
	}

	public void deltaTransform( Point2d pt )
	{
		pts[0] = pt.x;
		pts[1] = pt.y;

		this.deltaTransform(pts, 0, pts, 2, 1);
	
		pt.x = pts[2];
        pt.y = pts[3];
	}

	public Point2d inverseDeltaTransform(double x, double y)
	{
		pts[0] = x;
		pts[1] = y;
		
		invmat.deltaTransform(pts, 0, pts, 2, 1);

		return new Point2d( pts[2], pts[3]);
	}

	public void inverseDeltaTransform( Point2d pt )
	{
		pts[0] = pt.x;
		pts[1] = pt.y;

		invmat.deltaTransform(pts, 0, pts, 2, 1);
	
		pt.x = pts[2];
        pt.y = pts[3];
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -7918516134708245706L;


}

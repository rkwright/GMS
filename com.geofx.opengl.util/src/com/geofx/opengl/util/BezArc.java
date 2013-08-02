package com.geofx.opengl.util;

import com.geofx.opengl.util.Constants;
import com.geofx.opengl.util.PathElm.PathType;

public class BezArc
{

	private double currentX;

	private double currentY;

	private PathType currentType;

	void arcto(double x1, double y1, double x2, double y2, double radius)
	{
		int  sgnpsi;
		double ang10, ang12, psi, dr, t1x, t1y, cx, cy;
		double angs, angf;
		double x0, y0;

		x0 = currentX; /* assign local vars to current point */
		y0 = currentY;

		ang10 = Math.PI - Math.atan2(y1 - y0, x1 - x0); /* get angle P1 -> P0 */
		ang12 = Math.PI - Math.atan2(y1 - y2, x1 - x2); /* get angle P1 -> P2 */

		psi = ang10 - ang12; /* && the included angle */
		if (psi < (-Math.PI))
			psi += Constants.TWO_PI; /* correct it */
		if (psi > Math.PI)
			psi -= Constants.TWO_PI;
		sgnpsi = fpsign(0.0 - psi); /* set quadrant "flag" */

		angs = Math.atan2( y1 - y0, x1 - x0 ) - Constants.HALF_PI * sgnpsi; /* get perpendicular to P0->P1 */
		angf = Math.atan2( y1 - y2, x1 - x2) - Constants.HALF_PI * sgnpsi; /* get perpendicular to P1->P2 */

		dr = radius / Math.tan(psi / 2.0) * sgnpsi; /* find tangent points */
		t1x = x1 + dr * Math.cos(ang10); /* along first ray */
		t1y = y1 - dr * Math.sin(ang10);
		cx = t1x + radius * Math.sin(ang10) * sgnpsi; /* find center points */
		cy = t1y + radius * Math.cos(ang10) * sgnpsi;

		//t2x = x1 + dr * Math.cos(ang12); /* find tangent points along second ray */
		//t2y = y1 - dr * Math.sin(ang12);

		_lineto(t1x, t1y); /* draw to tangent-point */

		if (psi < 0.0)
			arc(cx, cy, radius, angs, angf); /* draw arc || arcn */
		else
			arcn(cx, cy, radius, angs, angf);

		_lineto(x2, y2); /* && from second tangent-point
		 to final point */
	}

	private void _lineto(double t1x, double t1y)
	{
		// TODO Auto-generated method stub

	}

	private int fpsign(double d)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/*-------------------------------------------------------------------------*/
	void arc(double cx, double cy, double radius, double angs, double angf)
	{
		int nxqd, lstqd, nxq;
		// double x0, y0, x1, y1, x2, y2, x3, y3;

		if (angs < 0.0)
			angs += Constants.TWO_PI; /* make angles positive */
		if (angf < 0.0)
			angf += Constants.TWO_PI;

		/* if no current point then make new point */
		if (currentType == PathType.undefined)
			_moveto(cx + Math.cos(angs) * radius, cy + Math.sin(angs) * radius);
		else
			_lineto(cx + Math.cos(angs) * radius, cy + Math.sin(angs) * radius);

		nxqd = (int) Math.floor((angs) / Constants.HALF_PI); /* find next && last quad */
		lstqd = (int) Math.floor((angf - 0.01) / Constants.HALF_PI);

		if ((nxqd == lstqd) && (angf > angs)) /* arc is only in one quad */
		{
			bezarc(cx, cy, radius, angs, angf, false);
		}
		else
		/* arc is in multiple quadrants */
		{
			nxqd = (nxqd + 1) % 4; /* do first part-quad */
			bezarc(cx, cy, radius, angs, (nxqd * Constants.HALF_PI), false);

			while (nxqd != lstqd) /* then intervening quadrants */
			{
				nxq = (nxqd + 1) % 4;
				bezarc(cx, cy, radius, (nxqd * Constants.HALF_PI), (nxq * Constants.HALF_PI), false);
				nxqd = nxq;
			}
			/* && the final quadrant */
			bezarc(cx, cy, radius, (nxqd * Constants.HALF_PI), angf, false);
		}

	}

	/*-------------------------------------------------------------------------*/
	void arcn(double cx, double cy, double radius, double angs, double angf)
	{
		int nxqd, lstqd, nxq;
		//double y0, x1, y1, x2, y2, x3, y3;

		if (angs < 0.0)
			angs += Constants.TWO_PI; /* make angles positive */
		if (angf < 0.0)
			angf += Constants.TWO_PI;
		if (Math.abs(angs) < 0.01)
			angs += Constants.TWO_PI;

		/* if no current point then make new point */
		if (currentType == PathType.undefined)
			_moveto(cx + Math.cos(angs) * radius, cy + Math.sin(angs) * radius);

		nxqd = ((int) Math.ceil(angs / Constants.HALF_PI)) % 4; /* find next quad-end */
		lstqd = ((int) Math.ceil((angf - 0.01) / Constants.HALF_PI)) % 4;

		if ((nxqd == lstqd) && (angf < angs)) /* within one quadrant */
		{
			bezarc(cx, cy, radius, angs, angf, true);
		}
		else
		/* multiple quadrants */
		{ /* do first part-quadrant */
			nxqd = (nxqd + 3) % 4;
			bezarc(cx, cy, radius, angs, (nxqd * Constants.HALF_PI), true);

			while (nxqd != lstqd) /* then intervening quadrants */
			{
				nxq = (nxqd + 3) % 4;
				bezarc(cx, cy, radius, (nxqd * Constants.HALF_PI), (nxq * Constants.HALF_PI), true);
				nxqd = nxq;
			}
			/* then last part-quadrant */
			bezarc(cx, cy, radius, (nxqd * Constants.HALF_PI), angf, true);
		}

	}

	private void _moveto(double d, double e)
	{
		// TODO Auto-generated method stub

	}

	/*--------------------------------------------------------------------------*/
	void curveto(double x1, double y1, double x2, double y2, double x3, double y3)
	/*  This proc simply takes the 6 args
	 representing the 3 control points,
	 && calls _curveto 3 times. */

	{
		if (Math.hypot(currentX - x3, currentY - y3) > 0.001)
		{
			_curveto(x1, y1); /* P1 */
			_curveto(x2, y2); /* P2 */
			_curveto(x3, y3); /* P3 */
		}
	}

	private void _curveto(double x1, double y1)
	{
		// TODO Auto-generated method stub

	}

	/*==========================================================================*/
	void bezarc(double cx, double cy, double radius, double angs, double angf, boolean revflg)

	/* this procedure calculates the bezier control
	 points for a given arc */
	{
		double arcfactor, deltang, sins, coss, sinf, cosf;

		while (angs > (Constants.TWO_PI + 0.001))
			angs -= Constants.TWO_PI; /* correct for over-range */
		while (angf > (Constants.TWO_PI + 0.001))
			angf -= Constants.TWO_PI;

		if (revflg)
			deltang = angs - angf;
		else
			deltang = angf - angs;

		if (Math.abs(deltang) > 0.00001)
		{
			if (deltang < 0.0)
				deltang += Constants.TWO_PI;
			/* correct for zero-angle error */
			while (deltang > (Constants.HALF_PI + 0.001))
				deltang -= Constants.HALF_PI;

			arcfactor = deltang * (1.0 + Math.pow(deltang / Constants.HALF_PI, 2.0) * 0.054242343) / 3.0
					* (1 - 2 * (revflg ? 1 : 0));

			coss = Math.cos(angs); /* get the trig funcs */
			sins = Math.sin(angs);
			cosf = Math.cos(angf);
			sinf = Math.sin(angf);
			/* P1 */
			_curveto((coss - sins * arcfactor) * radius + cx, (sins + coss * arcfactor) * radius + cy);
			/* P2 */
			_curveto((cosf + sinf * arcfactor) * radius + cx, (sinf - cosf * arcfactor) * radius + cy);

			_curveto(cosf * radius + cx, sinf * radius + cy); /* P3 */

		}

	}

}

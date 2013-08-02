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

import java.util.LinkedList;

import com.geofx.opengl.util.CTM;
import com.geofx.opengl.util.CubicElm;
import com.geofx.opengl.util.Dash;
import com.geofx.opengl.util.Edge;
import com.geofx.opengl.util.PathElm;
import com.geofx.opengl.util.QuadElm;
import com.geofx.opengl.util.PathElm.PathType;

public class Pool
{
	private LinkedList<PathElm> 	pathPool = new LinkedList<PathElm>();
	private LinkedList<CubicElm> 	cubicPool = new LinkedList<CubicElm>();
	private LinkedList<QuadElm> 	quadPool = new LinkedList<QuadElm>();
	private LinkedList<Edge> 		edgePool = new LinkedList<Edge>();
	private LinkedList<CTM> 		ctmPool = new LinkedList<CTM>();
	private LinkedList<Dash> 		dashPool = new LinkedList<Dash>();
	
	@SuppressWarnings("unused")
	private int 					allocated = 0;

	public void dispose()
	{
		pathPool.clear();
		cubicPool.clear();
		quadPool.clear();
		edgePool.clear();
	}
	
	//=================== Path Elements ======================================
	
    public PathElm newPathElm ()
    {
    	return newPathElm(0, 0, PathType.undefined );	
    }

    public PathElm newPathElm ( PathElm p )
    {
    	return newPathElm( p.x, p.y, p.type );	
    }
    
	public PathElm newPathElm( double x, double y, PathType type )
	{
		if (pathPool.size() == 0)
		{
			//allocated++;
			//System.out.println("Allocating new pathelm, size = " + pathPool.size() + " allocated: " + allocated);
			return new PathElm( x, y, type);
		}
		else
		{
			//System.out.println("Fetch new pathelm from pool, size = " + pathPool.size()  + " allocated: " + allocated);
			PathElm elm = pathPool.removeLast();
			elm.set(x, y, type);
			return elm;
		}
	}

	public void disposePathElm( PathElm elm )
	{
		PathType type = elm.type;
		elm.clear();
		if (type == PathType.cubicto)
			cubicPool.add((CubicElm)elm);
		else if (type == PathType.quadto)
			quadPool.add((QuadElm)elm);
		else	
			pathPool.add(elm);
	}

	public void disposePathElm( LinkedList<PathElm> list )
	{
		//System.out.print("Disposed elms, total of " + list.size() + " ");
		while (list.size() > 0)
		{
			disposePathElm(list.removeLast());
		}
		//System.out.println("Pool size now " + pathPool.size());
	}

	//=================== Cubic Elements ======================================

	public CubicElm newCubicElm ( CubicElm c )
    {
    	return newCubicElm( c.x, c.y, c.x1, c.y1, c.x2, c.y2, c.type );	
    }

	public CubicElm newCubicElm( double x, double y, double x1, double y1, double x2, double y2, PathType type )
	{
		if (cubicPool.size() == 0)
			return new CubicElm( x, y, x1, y1, x2, y2, type);
		else
		{
			CubicElm elm = cubicPool.removeLast();
			elm.set(x, y, x1, y1, x2, y2, type);
			return elm;
		}
	}

	//=================== Quad Elements ======================================

	public QuadElm newQuadElm( QuadElm q )
	{
		return newQuadElm(q.x, q.y, q.x1, q.y1, q.type);
	}

	public QuadElm newQuadElm(double x, double y, double x1, double y1, PathType type)
	{
		if (quadPool.size() == 0)
			return new QuadElm(x, y, x1, y1, type);
		else
		{
			QuadElm elm = quadPool.removeLast();
			elm.set(x, y, x1, y1, type);
			return elm;
		}
	}
		
	//=================== Edge Elements ======================================

	public Edge newEdge ( PathElm p0, PathElm p1 )
	{
		Edge edge = getEdge();

		edge.set( p0, p1 );
		
		return edge;
	}

	public Edge newEdge( Edge e )
	{
		Edge edge = getEdge();
		
		edge.p0 = newPathElm(edge.getP0().x, edge.getP0().y, edge.getP0().type);
		edge.p1 = newPathElm(edge.getP1().x, edge.getP1().y, edge.getP1().type);
		edge.setAngle();
		
		return edge;
	}

	public Edge getEdge()
	{
		if (edgePool.size() == 0)
			return new Edge();
		else
			return edgePool.removeLast();
	}

	public void addEdge( Edge edge )
	{
		edgePool.add(edge);
	}

	public void addEdges( LinkedList<Edge> list )
	{
		Edge edge;
		while (list.size() > 0)
		{
			edge = list.removeLast();
			edgePool.add(edge);
		}
	}
	
	/*
	 * Note that we never dispose the path-elements.  There is no way to
	 * know if they are part of some other list.  So the caller has to 
	 * dispose of them explicitly.
	 */
	public void disposeEdge( Edge edge )
	{
		edge.clear();
		edgePool.add(edge);
	}
	
	//=================== CTM Elements ======================================

	public CTM newCTM( CTM ctm  )
	{
		CTM newCTM = newCTM();

		newCTM.setMatrix(ctm);
		
		return newCTM;
	}
	
	public CTM newCTM()
	{
		CTM newCTM = null;
		
		if (ctmPool.size() == 0)
			newCTM = new CTM();
		else
		{
			newCTM = ctmPool.removeLast();
		}
		
		return newCTM;
	}

	public void disposeCTM( CTM ctm )
	{
		ctm.setToIdentity();
		ctmPool.add(ctm);
	}
	//=================== CTM Elements ======================================

	public Dash newDash( Dash dash  )
	{
		Dash newDash = newDash( dash.length, dash.gap);

		newDash.length = dash.length;
		newDash.gap = dash.gap;
		
		return newDash;
	}
	
	public Dash newDash(  double length, boolean gap )
	{
		Dash newDash = null;
		
		if (dashPool.size() == 0)
			newDash = new Dash(length, gap );
		else
		{
			newDash = dashPool.removeLast();
			newDash.length = length;
			newDash.gap = gap;
		}
		
		return newDash;
	}

	public void disposeDash( Dash dash )
	{
		dashPool.add(dash);
	}

}

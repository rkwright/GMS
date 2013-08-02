/*
 * A part of this code was originally written by 
 * 
 * Copyright (c) 2006 Erik Tollerud (erik.tollerud@gmail.com) All Rights Reserved.
 *
 * It has been heavily refactored since then and many bugs fixed, but some of the 
 * original code remains. 
 * 
 * Extracted the extrusion cod, which was originally in TextRenderer3D and refactored
 * it as an abstract class for more widespread use.
 * 
 * - Ric Wright, rkwright@geofx.com, July 2008
 */

package com.geofx.opengl.util;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellator;
import javax.media.opengl.glu.GLUtessellatorCallback;
import javax.vecmath.Vector3f;

import com.geofx.opengl.util.PathElm;
import com.geofx.opengl.util.PathElm.PathType;

/**
 * 
 */
public abstract class Extruder
{
	protected double 	depth = 0.1f;

	protected boolean 	edgeOnly = false;

	protected boolean 	calcNormals = true;;
	protected double	flatness = 0.005f;
	protected boolean	zeroWind = true;
	private Vector3f 	vecA = new Vector3f();
	private Vector3f 	vecB = new Vector3f();
	private Vector3f 	normal = new Vector3f();

	protected GLU 		glu = new GLU();
	protected GL 		gl = null;

	// tesselation vars to avoid GC thrashing
	private float[] lastCoord  = new float[3];
	private float[] firstCoord = new float[3];
	private float[] coords     = new float[6];

	private PathElm	prevPt = new PathElm();

	private int compileIndex;
	
	GLUtessellatorCallback 	tessCallback;
	GLUtessellator 			tess;


	/**
	 * Determines how long the sides of the rendered text is. In the special
	 * case of 0, the rendering is 2D.
	 * 
	 * @param depth
	 *            specifies the z-size of the rendered 3D text. Negative numbers
	 *            will be set to 0.
	 */
	public void setDepth(double depth)
	{
		if (depth <= 0)
			this.depth = 0;
		else
			this.depth = depth;
	}

	/**
	 * Retrieves the z-depth used for this TextRenderer3D's text rendering.
	 * 
	 * @return the z-depth of the rendered 3D text.
	 */
	public double getDepth()
	{
		return this.depth;
	}

	/**
	 * Sets if the text should be rendered as filled polygons or wireframe.
	 * 
	 * @param fill
	 *            if true, uses filled polygons, if false, renderings are
	 *            wireframe.
	 */
	public void setEdgeOnly(boolean edgeOnly)
	{
		this.edgeOnly = edgeOnly;
	}

	/**
	 * Determines if the text is being rendered as filled polygons or
	 * wireframes.
	 * 
	 * @return if true, uses filled polygons, if false, renderings are
	 *         wireframe.
	 */
	public boolean isEdgeOnly()
	{
		return this.edgeOnly;
	}

	/**
	 * Set the flatness to which the glyph's curves will be flattened
	 * 
	 * @return
	 */
	public double getFlatness()
	{
		return flatness;
	}

	/**
	 * Get the current flatness to which the glyph's curves will be flattened
	 * 
	 * @return
	 */
	public void setFlatness(float flatness)
	{
		this.flatness = flatness;
	}
	
	/**
	 * Sets whether the normals will eb calculated for each face
	 * 
	 * @param mode
	 *            the mode to render in. Default is flat.
	 */
	public void setCalcNormals( boolean normals)
	{
		this.calcNormals = normals;
	}

	/**
	 * Gets whether normals are being calculated
	 * 
	 * @see setNormal
	 * @return the normal technique for this TextRenderer3D.
	 */
	public boolean getCalcNormals()
	{
		return this.calcNormals;
	}
	
	public int beginCompile()
	{
		gl = GLU.getCurrentGL();
		
		compileIndex = gl.glGenLists(1);
		
		gl.glNewList( compileIndex, GL.GL_COMPILE);
		
		return compileIndex;
	}
	
	public void endCompile()
	{
		if (compileIndex != 0)
		{
			gl.glEndList();	
		}
	}
	
	/**
	 * General entry point for the Extruder.  Tesselates the supplied path, 
	 * twice, once at zero and once at the current depth.  Then walks the edge
	 * and creates the side panels.
	 * 
	 * @param path
	 * @param xOff
	 * @param yOff
	 * @param zOff
	 * @param scaleFactor
	 * @return
	 */
	protected void extrude( LinkedList<PathElm> path, float xOff, float yOff, float zOff, float scaleFactor )
	{
		gl = GLU.getCurrentGL();		

		gl.glPushAttrib(GL.GL_TRANSFORM_BIT);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glEnable( GL.GL_NORMALIZE);
		
		gl.glScalef(scaleFactor, scaleFactor, scaleFactor);
		gl.glTranslatef(xOff, yOff, zOff);
		
		extrudePath( path );
		
		gl.glPopMatrix();
		gl.glPopAttrib();
	}

	protected void extrude( LinkedList<PathElm> path )
	{
		extrude(path, 0,0,0,1);
	}

	/**
	 * General entry point for the Extruder.  Tesselates the supplied path, 
	 * twice, once at zero and once at the current depth.  Then walks the edge
	 * and creates the side panels.
	 * 
	 * @param path
	 * @param xOff
	 * @param yOff
	 * @param zOff
	 * @param scaleFactor
	 * @return
	 */
	public void extrude( GeneralPath path, float xOff, float yOff, float zOff, float scaleFactor )
	{
		gl = GLU.getCurrentGL();		
		
		gl.glPushAttrib(GL.GL_TRANSFORM_BIT);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glEnable( GL.GL_NORMALIZE);
		
		gl.glScalef(scaleFactor, scaleFactor, scaleFactor);
		gl.glTranslatef(xOff, yOff, zOff);
		
		extrudePath( path );
		
		gl.glPopMatrix();
		gl.glPopAttrib();
	}
	
	/**
	 * Renders a string into the specified GL object, starting at the (0,0,0)
	 * point in OpenGL coordinates.
	 * 
	 */
	public void extrude( GeneralPath path)
	{
		this.extrude(path, 0, 0, 0, 1 );
	}	
	
	/**
	 * Draws the specified compiled string, if any.
	 *
	 */
	public void call( int index )
	{
		gl = GLU.getCurrentGL();
		gl.glCallList( index );
	}
	

	/**
	 * Diposes of the ALL the current compiled shapes, if any.
	 *
	 */
	public void dispose( int listIndex )
	{
		gl = GLU.getCurrentGL();

		gl.glDeleteLists( listIndex, 1);
		
		compileIndex = 0;
	}

	/**
	 * Get the bounding box for the supplied path.  Merely a convenience
	 * 
	 * @param str
	 * @return
	 */
	public Rectangle2D  getBounds( GeneralPath path )
	{		
		return path.getBounds2D();
	}	
	
	/**
	 * Get the bounding box for the supplied string for the current font and 
	 * specified scale factor.
	 */
	public Rectangle2D  getBounds( GeneralPath path, float scaleFactor  )
	{
		gl = GLU.getCurrentGL();

		gl.glPushAttrib(GL.GL_TRANSFORM_BIT);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		
		gl.glScalef(scaleFactor, scaleFactor, scaleFactor);

		Rectangle2D rect = path.getBounds2D();

		gl.glPopMatrix();
		gl.glPopAttrib();	
		
		return rect;
	}	
	

	
	/**
	 * This is the routine that does all the heavy lifting.
	 *
	 */
	private void extrudePath( LinkedList<PathElm> path )
	{
		prunePath( path );
		
		ListIterator<PathElm> iter = path.listIterator();		
	
		if (calcNormals)
			gl.glNormal3f(0, 0, -1.0f);
		
		tesselateFace(glu, gl, iter, this.edgeOnly, 0.0f);
		
		if (this.depth != 0.0)
		{
			iter = path.listIterator();
			
			if (calcNormals)
				gl.glNormal3f(0, 0, 1.0f);
			
			tesselateFace(glu, gl, iter, this.edgeOnly, depth);
			
			iter = path.listIterator();
	
			// TODO: add diagonal corner/VBO technique
	
			drawSides(gl, iter, this.edgeOnly, (float) depth);
		}
	}

	// construct the sides of each glyph by walking around and extending each vertex
	// out to the depth of the extrusion
	private void drawSides(GL gl, ListIterator<PathElm> iter, boolean justBoundary, float depth)
	{
		if (justBoundary)
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
		else
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
			
		PathElm	elm;
		while (iter.hasNext())
		{
			elm = (PathElm) iter.next();
			coords[0] = (float) elm.x;
			coords[1] = (float) elm.y;
			
			if (elm.type == PathType.moveto)
			{
				gl.glBegin(GL.GL_QUADS);
				lastCoord[0] = coords[0];
				lastCoord[1] = coords[1];
				firstCoord[0] = coords[0];
				firstCoord[1] = coords[1];
			}
			else if (elm.type == PathType.lineto)
			{
				if (calcNormals)
					setNormal(gl, (float) (lastCoord[0]-elm.x), lastCoord[1]-coords[1], 0.0f, 
							     0.0f, 0.0f, depth);

				lastCoord[2] = 0;
				gl.glVertex3fv(lastCoord, 0);
				lastCoord[2] = depth;
				gl.glVertex3fv(lastCoord, 0);
				coords[2] = depth;
				gl.glVertex3fv(coords, 0);
				coords[2] = 0;
				gl.glVertex3fv(coords, 0);

				if (calcNormals)
				{
					lastCoord[0] = coords[0];
					lastCoord[1] = coords[1];
				}
			}
			else if (elm.type == PathType.closepath)
			{
				if(calcNormals)
					setNormal(gl, lastCoord[0]-firstCoord[0], lastCoord[1]-firstCoord[1], 0.0f, 
							     0.0f, 0.0f, depth );

				lastCoord[2] = 0;
				gl.glVertex3fv(lastCoord, 0);
				lastCoord[2] = depth;
				gl.glVertex3fv(lastCoord, 0);
				firstCoord[2] = depth;
				gl.glVertex3fv(firstCoord, 0);
				firstCoord[2] = 0;
				gl.glVertex3fv(firstCoord, 0);
				gl.glEnd();
			}
			else
			{
				// this should never happen!
				throw new RuntimeException("ListIterator segment not moveto, lineto or closepath!");
			}
		}
	}

	/**
	 * routine that tesselates the current path
	 */ 
	private void tesselateFace(GLU glu, GL gl, ListIterator<PathElm> iter, boolean justBoundary, double tessZ)
	{
		initTessellator(glu, gl);

		glu.gluTessNormal(tess, 0.0, 0.0, -1.0);

		if ( zeroWind )
			glu.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_NONZERO);
		else
			glu.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ODD);
	
		if (justBoundary)
			glu.gluTessProperty(tess, GLU.GLU_TESS_BOUNDARY_ONLY, GL.GL_TRUE);
		else
			glu.gluTessProperty(tess, GLU.GLU_TESS_BOUNDARY_ONLY, GL.GL_FALSE);

		glu.gluTessBeginPolygon(tess, (double[]) null);

		int vertexCount = 0;
		
		double  lastX = 0, lastY = 0;
		int		matchCount = 0;
		final double FP_TOL = 0.001;
		
		PathElm	elm;
		while (iter.hasNext())
		{
			double[] coords = new double[3];
			coords[2] = tessZ;

			elm = (PathElm) iter.next();
			coords[0] = elm.x;
			coords[1] = elm.y;
			
			if (elm.type == PathType.moveto)
			{
				glu.gluTessBeginContour(tess);
				glu.gluTessVertex(tess, coords, 0, coords);
				vertexCount++;
				//System.out.println("iterator - moveto: x,y,z: " + coords[0] + " " + coords[1] + " " + coords[2] );
			}
			else if (elm.type == PathType.lineto)
			{
				vertexCount++;
				if (Math.abs(coords[0]-lastX) < FP_TOL && Math.abs(coords[1]-lastY) < FP_TOL)
				{
					//System.out.println("Coords match at " + lastX + " " + lastY);
					matchCount++;
					break;
				}
				glu.gluTessVertex(tess, coords, 0, coords);
				//System.out.println("iterator - lineto: x,y,z: " + coords[0] + " " + coords[1] + " " + coords[2] );
			}
			else if (elm.type == PathType.closepath)
			{
				glu.gluTessEndContour(tess);
				vertexCount++;
				//System.out.println("iterator - close: x,y,z: " + coords[0] + " " + coords[1] + " " + coords[2] );
			}
			else
			{
				// this should never happen!
				throw new RuntimeException("ListIterator segment not moveto, lineto or closepath!");
			}
	
			// save to test next time around
			lastX = coords[0];
			lastY = coords[1];
		}
		
		glu.gluTessEndPolygon(tess);

		glu.gluDeleteTess(tess);
		
		//System.out.println("Flatness: " + flatness + " Total Vertex Count = " + vertexCount + 
		//				" matchCount " + matchCount + " match " + Math.round(100.0 *(float)matchCount/(float)vertexCount) + "%");
	}

	private void initTessellator(GLU glu, GL gl)
	{
		if (tess != null)
			return;
		
		tessCallback = new GLUtesselatorCallbackImpl(gl);
		tess = glu.gluNewTess();

		glu.gluTessCallback(tess, GLU.GLU_TESS_BEGIN, tessCallback);
		glu.gluTessCallback(tess, GLU.GLU_TESS_END, tessCallback);
		glu.gluTessCallback(tess, GLU.GLU_TESS_ERROR, tessCallback);
		glu.gluTessCallback(tess, GLU.GLU_TESS_VERTEX, tessCallback);
		glu.gluTessCallback(tess, GLU.GLU_TESS_COMBINE, tessCallback);
	}

	/** This is the routine that does all the heavy lifting.
	 *
	 */
	private void extrudePath( GeneralPath gp )
	{
		PathIterator pi = gp.getPathIterator(AffineTransform.getScaleInstance(1.0, -1.0), flatness);
	
				
		if (calcNormals)
			gl.glNormal3f(0, 0, -1.0f);
		
		tesselateFace(glu, gl, pi, this.edgeOnly, 0.0f);
		
		if (this.depth != 0.0)
		{
			pi = gp.getPathIterator(AffineTransform.getScaleInstance(1.0, -1.0), flatness);
			
			if (calcNormals)
				gl.glNormal3f(0, 0, 1.0f);
			
			tesselateFace(glu, gl, pi, this.edgeOnly, depth);
			
			pi = gp.getPathIterator(AffineTransform.getScaleInstance(1.0, -1.0), flatness);
	
			// TODO: add diagonal corner/VBO technique
	
			drawSides(gl, pi, this.edgeOnly, (float)depth);
		}
	}

	// construct the sides of each glyph by walking around and extending each vertex
	// out to the depth of the extrusion
	private void drawSides(GL gl, PathIterator pi, boolean justBoundary, float depth)
	{
		if (justBoundary)
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
		else
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);

		float[] lastCoord = new float[3];
		float[] firstCoord = new float[3];
		float[] coords = new float[6];
		
		while ( !pi.isDone() )
		{
			switch (pi.currentSegment(coords))
			{
				case PathIterator.SEG_MOVETO:
					gl.glBegin(GL.GL_QUADS);
					lastCoord[0] = coords[0];
					lastCoord[1] = coords[1];
					firstCoord[0] = coords[0];
					firstCoord[1] = coords[1];
					break;
				case PathIterator.SEG_LINETO:
					if (calcNormals)
						setNormal(gl, lastCoord[0]-coords[0], lastCoord[1]-coords[1], 0.0f, 
								     0.0f, 0.0f, depth);
 
					lastCoord[2] = 0;
					gl.glVertex3fv(lastCoord, 0);
					lastCoord[2] = depth;
					gl.glVertex3fv(lastCoord, 0);
					coords[2] = depth;
					gl.glVertex3fv(coords, 0);
					coords[2] = 0;
					gl.glVertex3fv(coords, 0);

					if (calcNormals)
					{
						lastCoord[0] = coords[0];
						lastCoord[1] = coords[1];
					}
					break;
				case PathIterator.SEG_CLOSE:
					if(calcNormals)
						setNormal(gl, lastCoord[0]-firstCoord[0], lastCoord[1]-firstCoord[1], 0.0f, 
								     0.0f, 0.0f, depth );

					lastCoord[2] = 0;
					gl.glVertex3fv(lastCoord, 0);
					lastCoord[2] = depth;
					gl.glVertex3fv(lastCoord, 0);
					firstCoord[2] = depth;
					gl.glVertex3fv(firstCoord, 0);
					firstCoord[2] = 0;
					gl.glVertex3fv(firstCoord, 0);
					gl.glEnd();
					break;
				default:
					throw new RuntimeException(
							"PathIterator segment not SEG_MOVETO, SEG_LINETO, SEG_CLOSE; Inappropriate font.");
			}
			
			pi.next();
		}
	}

	// simple convenience for calculating and setting the normal
	private void setNormal ( GL gl, float x1, float y1, float z1, float x2, float y2, float z2 )
	{
		vecA.set( x1, y1, z1);
		vecB.set( x2, y2, z2);
		normal.cross( vecA, vecB );
		normal.normalize();
		gl.glNormal3f( normal.x, normal.y, normal.z );
	}

	// routine that tesselates the current set of glyphs
	private void tesselateFace(GLU glu, GL gl, PathIterator pi, boolean justBoundary, double tessZ)
	{
		initTessellator(glu, gl);

		glu.gluTessNormal(tess, 0.0, 0.0, -1.0);

		if ( pi.getWindingRule() == PathIterator.WIND_EVEN_ODD)
			glu.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ODD);
		else
			glu.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_NONZERO);
	
		if (justBoundary)
			glu.gluTessProperty(tess, GLU.GLU_TESS_BOUNDARY_ONLY, GL.GL_TRUE);
		else
			glu.gluTessProperty(tess, GLU.GLU_TESS_BOUNDARY_ONLY, GL.GL_FALSE);

		glu.gluTessBeginPolygon(tess, (double[]) null);

		int vertexCount = 0;
		
		double  lastX = 0, lastY = 0;
		int		matchCount = 0;
		final double FP_TOL = 0.001;
		while (!pi.isDone())
		{
			double[] coords = new double[3];
			coords[2] = tessZ;
			switch (pi.currentSegment(coords))
			{
				case PathIterator.SEG_MOVETO:
					glu.gluTessBeginContour(tess);
					glu.gluTessVertex(tess, coords, 0, coords);
					vertexCount++;
					//System.out.println("iterator - moveto: x,y,z: " + coords[0] + " " + coords[1] + " " + coords[2] );
					break;
				case PathIterator.SEG_LINETO:
					if (Math.abs(coords[0]-lastX) < FP_TOL && Math.abs(coords[1]-lastY) < FP_TOL)
					{
						//System.out.println("Coords match at " + lastX + " " + lastY);
						matchCount++;
						break;
					}
					glu.gluTessVertex(tess, coords, 0, coords);
					vertexCount++;
					//System.out.println("iterator - lineto: x,y,z: " + coords[0] + " " + coords[1] + " " + coords[2] );
					break;
				case PathIterator.SEG_CLOSE:
					glu.gluTessEndContour(tess);
					vertexCount++;
					//System.out.println("iterator - close: x,y,z: " + coords[0] + " " + coords[1] + " " + coords[2] );
					break;
			}
			
			// save to test next time around
			lastX = coords[0];
			lastY = coords[1];
			
			pi.next();
		}
		glu.gluTessEndPolygon(tess);

		glu.gluDeleteTess(tess);
		
		//System.out.println("Flatness: " + flatness + " Total Vertext Count = " + vertexCount + " matchCount " + matchCount);
	}
	
	//====================== Coord Pool Manager ================================
	
	private LinkedList<double[]> coordPool = new LinkedList<double[]>();
	
	public double[] newCoords ()
	{
		if (coordPool.size() == 0)
		{
			return new double[3];
		}
		else
		{
			return coordPool.removeFirst();
		}
	}

	public void disposeCoords( double[] coords )
	{
		coordPool.add(coords);
	}
	
	
	//==================== Callback Class ======================================
	
	// Private class that implements the required callbacks for the tesselator
	private class GLUtesselatorCallbackImpl extends javax.media.opengl.glu.GLUtessellatorCallbackAdapter
	{
		private GL gl;

		public GLUtesselatorCallbackImpl(GL gl)
		{
			this.gl = gl;
		}

		public void begin(int type)
		{
			gl.glBegin(type);
		}

		public void vertex(java.lang.Object vertexData)
		{
			double[] coords = (double[]) vertexData;

			// System.out.println("callback - vertex: x,y,z: " + coords[0] + " " + coords[1] + " " + coords[2] );

			gl.glVertex3dv(coords, 0);
			
			//disposeCoords(coords);
		}

		public void combine(double[] coords, Object[] data, float[] weight, Object[] outData)
		{
			//System.out.println("Combine called!");
			
			double newData[] = new double[6];

            newData[0] = coords[0];
            newData[1] = coords[1];
            newData[2] = coords[2];
            
            int n = 0;
            while (n < Array.getLength(data) && data[n] != null ) 
        	{
        		n++;
        	}
                         
            for ( int i=3; i<6; i++ )
            {
            	newData[i] = 0;
                for ( int j=0; j<n; j++ )
                {
                	newData[i] += weight[j] * Array.getDouble(data[j],i-3);
                }      	
            }
 
            outData[0] = newData;     
            
			//disposeCoords(coords);
		}

		public void end()
		{
			gl.glEnd();
		}
		
		public void error(int errnum)
		{
			System.err.println("Error in tesselation: " + errnum+ " " + glu.gluErrorString(errnum));
		}
	}
	
	
	/**
	 * Prunes coincident elements from the list. Assumes the
	 * the path has already been flattened.
	 */
	private void prunePath ( LinkedList<PathElm> path )
	{
		final double 		TOLERANCE = 0.001;

		ListIterator<PathElm> iter = path.listIterator();
		//int	count = 0;
		while (iter.hasNext())
		{
			PathElm elm = (PathElm) iter.next();
			
			//System.out.println("prune #" + count++ + "  " + elm);
			
			if (elm.type != PathType.moveto && elm.type != PathType.closepath)
			{
				if ((Math.abs(elm.x - prevPt.x) < TOLERANCE) && ( Math.abs(elm.y - prevPt.y) < TOLERANCE))
				{
					iter.remove();
				}
			}
			
			prevPt.set(elm);
		}
	}
}

	//--------------- diagnostic utilities -----------------------------------------
	

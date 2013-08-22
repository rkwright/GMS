/*******************************************************************************
 * 
 * 
 * Code from the JOGL Demos:  https://jogl-demos.dev.java.net/
 * 
 * Contributors:
 *    
 *     Ric Wright - May 2008 - Ported to Eclipse 3.2, minor tweaks
 *******************************************************************************/
package com.geofx.scenes.ps;

import java.awt.geom.AffineTransform;
import java.util.Iterator;
import java.util.LinkedList;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.geofx.opengl.scene.GLScene;
import com.geofx.opengl.view.GLComposite;
import com.sun.opengl.util.GLUT;

public class Path3DScene extends GLScene 
{
	public static final int MAX_ITEMS = 100;
	public static final float STROKE_RADIUS = 0.05f;
	
	float[] 		LightDiffuse =	 { 1.0f, 1.0f, 1.0f, 1.0f };
	float[] 		LightAmbient =	 { 0.8f, 0.8f, 0.8f, 1.0f };
	float[] 		LightPosition =	 { 1.8f, 1.8f, 1.8f, 0.0f };
	float[]			mat_specular = { 1.0f, 1.0f, 1.0f, 1.0f };
	float[]			mat_magenta = { 1.0f, 0.0f, 1.0f, 1.0f };
	float[]			mat_yellow = { 1.0f, 1.0f, 0.0f, 1.0f };
	float[]     	mat_shininess = { 100.0f };	

	AffineTransform	ctm = new AffineTransform();
	
	PathInfo3D		pathInfo;
	Point3d			lastPt = new Point3d();

	private LinkedList<Point3d> path = new LinkedList<Point3d>();

	public Path3DScene( GLComposite parent)
	{
		super(parent);

		System.out.println("Chart2DScene - constructor");

		glComposite.getGrip().setOffsets(0.0f, 0.0f, 0.0f);
		glComposite.getGrip().setRotation( 0.0f, 0.0f, 0.0f);
	}

	// a default constructor used by the ClassInfo enumerator
	public Path3DScene()
	{
		super();
	}

	public void init ( GLAutoDrawable drawable ) 
	{
		super.init(drawable);	
		
		System.out.println("Path3DScene - init");

		final GL gl = drawable.getGL();	

		gl.setSwapInterval(1);

        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, LightAmbient, 0);						
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, LightDiffuse, 0);				

		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, LightPosition, 0);			

		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, mat_specular, 0);
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SHININESS, mat_shininess, 0);
	
		glComposite.setClearColor(0.1f, 0.1f, 0.1f, 0 );
		
	    glComposite.setLightingEnabled(true);	
	 
	    pathInfo = randomPathInfo();
	    //lastPt.set(pathInfo.position);
	    
		glComposite.initAnimator(drawable, 100);
	}

	public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) 
	{
		System.out.println("Path3DScene - reshape");

		final GL gl = drawable.getGL();		
		
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
	
		glComposite.getGLU().gluPerspective( 45.0f, (double)width / (double)height, 0.1f, 100.0f);
		
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		glComposite.getGrip().setOffsets(0.0f, 0.0f, -3.0f);
		glComposite.getGrip().setRotation(45.0f, -30.0f, 0.0f);
	}
	
	public void display(GLAutoDrawable drawable) 
	{
		GL gl = drawable.getGL();

		super.display(drawable);

		System.out.println("Path3DScene - display");

		try
		{
			glComposite.updateLighting(gl);
		
			glComposite.drawAxes(gl);
			
			gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, mat_magenta, 0);
			
			updatePathInfo( pathInfo );
			Point3d currPt = new Point3d(pathInfo.position);
			
			path.add(currPt);
		//	System.out.println("added: " + pathInfo.position.x +" "+ pathInfo.position.y + " " + pathInfo.position.z );
					
			gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, pathInfo.material, 0);

			drawPath(gl, path );
	
		    lastPt.set(pathInfo.position);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void drawPath ( GL gl, LinkedList<Point3d> path )
	{	
		Iterator<Point3d> iter = path.iterator();
		if (!iter.hasNext())
			return;
		
		Point3d	lastPt = new Point3d();
		Point3d	currPt = new Point3d();
		lastPt.set((Point3d)iter.next());
		
		while (iter.hasNext())
		{
			currPt.set((Point3d) iter.next());
		//	System.out.println("last: " + lastPt.x +" "+ lastPt.y + " " + lastPt.z + " curr: " + currPt.x + " " + currPt.y + " " + currPt.z);
			drawPathSeg(gl, lastPt, currPt);
			drawPathJoin(gl, currPt);
			
			lastPt.set(currPt);
		}

		int count = path.size();
	//	System.out.println("Count = " + count);
		if (count > MAX_ITEMS)
		{
			path.removeFirst();
		}
	}
	
	private void drawPathSeg ( GL gl, Point3d p0, Point3d p1 )
	{
		gl.glPushMatrix();
		
		Vector3d	vz = new Vector3d(0,0,1);
		Vector3d	p = new Vector3d(p1.x-p0.x, p1.y-p0.y,p1.z-p0.z);
		Vector3d	cross = new Vector3d();
		p.normalize();
		
		double 	angle = vz.angle(p);
		double	dist = p1.distance(p0);
		
		cross.cross(vz,p);

		gl.glTranslated(p0.x, p0.y, p0.z);
		gl.glRotated(Math.toDegrees(angle), cross.x, cross.y, cross.z);
		
		glComposite.getGLU().gluCylinder(glComposite.getQuadric(), STROKE_RADIUS, STROKE_RADIUS, dist, 32, 5);
		
		gl.glPopMatrix();


	}

	private void drawPathJoin ( GL gl, Point3d p0 )
	{
		gl.glPushMatrix();
	
		gl.glTranslated(p0.x, p0.y, p0.z);
		
		glComposite.getGLU().gluSphere(glComposite.getQuadric(), STROKE_RADIUS, 32, 5);
		
		gl.glPopMatrix();


	}

	@SuppressWarnings("unused")
	private void drawTeapot(GL gl, float scale)
	{
		GLUT glut = new GLUT();	
		// gl.glTranslatef(0.5f, 0.5f, 0.0f);
		glut.glutSolidTeapot(scale);
	}
		
	public void  enableLighting( GL gl )
	{
	    gl.glEnable(GL.GL_LIGHT0);
	}
	
	public String getDescription()
	{
		return "Plot an aributrary 3D path";
	}

	public String getLabel()
	{
		return "Path 3D";
	}
	
	//------------------ Private Stuff below here ------------------------------
	private static final float 	INIT_ANG_VEL_MAG = 0.3f;
	private static final float 	INIT_VEL_MAG = 0.25f;
	private static final float 	MAX_BOUNDS = 1.5f;

	// Information about each piece of text
	private static class PathInfo3D
	{
		Point3d	angularVelocity;
		Point3d	velocity;
		Point3d	position;
		Point3d	angle;
		float[] material = new float[4];
	}

	Point3d		tmp = new Point3d();
	
	private void updatePathInfo( PathInfo3D info )
	{
		// Update velocities and positions of all text
		float deltaT = 1.0f; 

		// Now update angles and positions
		tmp.set(info.angularVelocity);
		tmp.scale(deltaT*deltaT);
		info.angle.add(tmp);
		
		tmp.set(info.velocity);
		tmp.scale(deltaT);
		info.position.add(tmp);

		// Wrap angles and positions
		info.angle.x = clampAngle(info.angle.x);
		info.angle.y = clampAngle(info.angle.y);
		info.angle.z = clampAngle(info.angle.z);
	
		info.velocity.x = clampBounds(info.position.x, info.velocity.x );
		info.velocity.y = clampBounds(info.position.y, info.velocity.y );
		info.velocity.z = clampBounds(info.position.z, info.velocity.z );
	}

	private double clampBounds( double pos, double velocity )
	{
		if (pos < -MAX_BOUNDS || pos > MAX_BOUNDS)
		{
			velocity *= -1.0;
		}
		
		return velocity;
	}
	
	private double clampAngle(double angle)
	{
		if (angle < 0)
		{
			angle += 360;
		}
		else if (angle > 360)
		{
			angle -= 360;
		}
		
		return angle;
	}
	
	private PathInfo3D randomPathInfo()
	{
		PathInfo3D info = new PathInfo3D();
		info.angle = randomRotation(INIT_ANG_VEL_MAG, INIT_ANG_VEL_MAG, INIT_ANG_VEL_MAG);
		info.position = randomVector(MAX_BOUNDS, MAX_BOUNDS, MAX_BOUNDS);
	
		info.angularVelocity = randomRotation(INIT_ANG_VEL_MAG, INIT_ANG_VEL_MAG, INIT_ANG_VEL_MAG);
		info.velocity = randomVelocity(INIT_VEL_MAG, INIT_VEL_MAG, INIT_VEL_MAG);

		info.material = glComposite.randomColor();
		info.material[3] = glComposite.getRandom().nextFloat() * 0.9f + 0.1f;
	
		return info;
	}

	private Point3d randomVector(float x, float y, float z)
	{
		return new Point3d(x * glComposite.getRandom().nextFloat(), y * glComposite.getRandom().nextFloat(), z * glComposite.getRandom().nextFloat());
	}

	private Point3d randomVelocity(float x, float y, float z)
	{
		return new Point3d(x * (glComposite.getRandom().nextFloat() - 0.5f), y * (glComposite.getRandom().nextFloat() - 0.5f), z * (glComposite.getRandom().nextFloat() - 0.5f));
	}

	private Point3d randomRotation(float x, float y, float z)
	{
		return new Point3d(glComposite.getRandom().nextFloat() * 360.0f, glComposite.getRandom().nextFloat() * 360.0f, glComposite.getRandom().nextFloat() * 360.0f);
	}
}

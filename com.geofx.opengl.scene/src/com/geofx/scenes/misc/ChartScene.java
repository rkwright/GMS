/*******************************************************************************
 * Copyright (c) 2005 Bo Majewski 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Code from an article by Bo Majewski
 * http://www.eclipse.org/articles/Article-SWT-OpenGL/opengl.html
 * 
 * Contributors:
 *     Bo Majewski - initial API and implementation
 *     Ric Wright - Jan 2008 - Ported to Eclipse 3.2, minor tweaks
 *     Ric Wright - May 2008 - ported to JOGL
 *******************************************************************************/
package com.geofx.scenes.misc;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import com.geofx.opengl.scene.GLScene;
import com.geofx.opengl.view.GLComposite;

/**
 * A 3D cylinder chart.
 * 
 * @author Bo Majewski
 */
public class ChartScene extends GLScene
{
	public static final int ROW_LENGTH = 7;

	public static final int CHART_COUNT = 4;

	private static final float[][] COLOR = { { 1.0f, 1.0f, 0.0f, 0.7f }, 
										     { 0.0f, 1.0f, 0.0f, 0.7f },
											 { 0.0f, 0.0f, 1.0f, 0.7f }, 
											 { 1.0f, 0.0f, 1.0f, 0.7f }, };

	private BarValue[][] chart;

	private Axis axis;

  	/**
   	 * The Chart constructor.  Like all the scene derived classes, it 
   	 * takes a Composite parent and an AWT Frame object.
   	 * @param parent
   	 * @param glFrame
   	 */
	public ChartScene ( GLComposite parent )
	{		
		super(parent);
		
		System.out.println("ChartScene - constructor");

		glComposite.getGrip().setOffsets(-3.25f, 3.25f, -30.5f);
		glComposite.getGrip().setRotation(45.0f, -30.0f, 0.0f);
	}

	/**
	 *  a default constructor used by the ClassInfo enumerator
	 */
	public ChartScene()
	{
		super();
	}

	/**
	 * The scene's implementation of the init method.
	 */	
	public void init( GLAutoDrawable drawable ) 
	{	
		super.init(drawable);	
		
		System.out.println("ChartScene - init");

		final GL gl = drawable.getGL();	
		
		System.out.println("ChartScene - initScene");

		BarValue.QUADRIC = glComposite.getGLU().gluNewQuadric();
		
		glComposite.getGLU().gluQuadricNormals(BarValue.QUADRIC, GLU.GLU_SMOOTH);

		gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, new float[] { 1.0f, 1.0f, 1.0f, 1.0f }, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, new float[] { 0.5f, 0.5f, 0.5f, 1.0f }, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, new float[] { -50.f, 50.0f, 100.0f, 1.0f }, 0);
		gl.glEnable(GL.GL_LIGHT1);
		gl.glEnable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_COLOR_MATERIAL);
		gl.glColorMaterial(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE);

		this.axis = new Axis(gl, 15.0f, 9.0f, 11.0f);
		this.axis = new Axis(gl, 15.0f, 9.0f, 11.0f);
		this.chart = new BarValue[CHART_COUNT][ROW_LENGTH];
		double slice = Math.PI / ROW_LENGTH;

		for (int i = 0; i < this.chart.length; ++i)
		{
			BarValue[] value = this.chart[i];
			double shift = i * Math.PI / 4.0;

			for (int j = 1; j <= value.length; ++j)
			{
				value[j - 1] = new BarValue(gl, (float) (8.0 * Math.abs(Math.sin(slice * j - shift))));
			}
		}
		
		glComposite.initAnimator(drawable, 100);

	}

	/**
	 * Here we actually draw the scene.
	 */
	public void display(GLAutoDrawable drawable) 
	{
		super.display(drawable);
	
		System.out.println("GLScene - display");
		GL gl = drawable.getGL();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		
		gl.glLineWidth(1.0f);
		this.axis.draw(gl);
		
		gl.glTranslatef(BarValue.RADIUS, 0.0f, BarValue.RADIUS);
		
		for (int i = 0; i < this.chart.length; ++i)
		{
			BarValue[] value = this.chart[i];
			gl.glColor4fv(COLOR[i % COLOR.length], 0);

			for (int j = 0; j < value.length; ++j)
			{
				value[j].draw(gl);
				gl.glTranslatef(2.0f * BarValue.RADIUS, 0.0f, 0.0f);
			}

			gl.glTranslatef(-2.0f * BarValue.RADIUS * value.length, 0.0f, 2.0f * BarValue.RADIUS + 0.5f);
		}

		if (glComposite.getAnimator().isAnimating())
		{
			glComposite.getGrip().adjustXRot(1.0f);
			glComposite.getGrip().adjustYRot(1.0f);
			glComposite.getGrip().adjustZRot(1.0f);		
		}
	}

	public void dispose()
	{
		dispose(glComposite.getCanvas().getGL());
	}

	public void dispose( GL gl )
	{
		System.out.println("ChartScene - dispose" );
		
		if (this.chart[0][0] != null)
		{
			glComposite.getGLU().gluDeleteQuadric(BarValue.QUADRIC);

			for (int i = 0; i < this.chart.length; ++i)
			{
				BarValue[] value = this.chart[i];

				for (int j = 0; j < value.length; ++j)
				{
					value[j].dispose(gl);
					value[j] = null;
				}
			}
		}
		
		System.out.println("About to call ChartScene super.dispose");
		super.dispose();
	}

	/** 
	 * Return the string that is the description of this scene
	 */
	public String getDescription()
	{
		return "This is a simple demonstration of drawing a 3D chart";
	}

	/**
	 * Return the string that is the label for this string that
	 * will be shown in the "SelectDialog"
	 */
	public String getLabel()
	{
		return "3D Chart";
	}
	
	private static class Axis extends CompiledShape
	{
		private static float[] COLOR1 = new float[] { 0.6f, 0.6f, 0.6f, 0.3f };

		private static float[] COLOR2 = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };

		private static float[] COLOR3 = new float[] { 0.6f, 0.0f, 0.0f, 1.0f };

		public Axis(GL gl, float x, float y, float z)
		{
			super(gl);
						
			gl.glNewList(this.getListIndex(), GL.GL_COMPILE_AND_EXECUTE);
			
			gl.glBegin(GL.GL_QUADS);
			gl.glColor4fv(COLOR1, 0);
			gl.glVertex3f(0.0f, y, z);
			gl.glVertex3f(0.0f, -1.0f, z);
			gl.glVertex3f(0.0f, -1.0f, -1.0f);
			gl.glVertex3f(0.0f, y, -1.0f);

			gl.glVertex3f(-1.0f, y, 0.0f);
			gl.glVertex3f(-1.0f, -1.0f, 0.0f);
			gl.glVertex3f(x, -1.0f, 0.0f);
			gl.glVertex3f(x, y, 0.0f);
			gl.glEnd();

			gl.glColor4fv(COLOR2, 0);
			for (float a = 1.0f; a < y; a += 1.0f)
			{
				gl.glBegin(GL.GL_LINE_STRIP);
				gl.glVertex3f(0.1f, a, z);
				gl.glVertex3f(0.1f, a, 0.1f);
				gl.glVertex3f(x, a, 0.1f);
				gl.glEnd();
			}

			gl.glColor4fv(COLOR3, 0);
			gl.glBegin(GL.GL_LINE_STRIP);
			gl.glVertex3f(0.1f, 0.0f, z);
			gl.glVertex3f(0.1f, 0.0f, 0.1f);
			gl.glVertex3f(x, 0.0f, 0.1f);
			gl.glEnd();
			
			gl.glBegin(GL.GL_LINES);
			gl.glVertex3f(0.1f, -1.0f, 0.1f);
			gl.glVertex3f(0.1f, y, 0.1f);
			gl.glEnd();
					
			gl.glEndList();
			
		}
	}

	private static class BarValue extends CompiledShape
	{
		final GLU glu = new GLU();
		
		public static final float RADIUS = 1.0f;

		public static GLUquadric QUADRIC;

		public BarValue(GL gl, float value)
		{
			super(gl);
			
			gl.glNewList(this.getListIndex(), GL.GL_COMPILE);
			gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
			glu.gluCylinder(BarValue.QUADRIC, RADIUS, RADIUS, value, 32, 1);
			glu.gluDisk(BarValue.QUADRIC, 0.0, RADIUS, 32, 32);
			gl.glTranslatef(0.0f, 0.0f, value);
			glu.gluDisk(BarValue.QUADRIC, 0.0, RADIUS, 32, 32);
			gl.glTranslatef(0.0f, 0.0f, -value);
			gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
			gl.glEndList();
		}
	}

	
	protected void drawPyramid(GL gl)
	{
		gl.glBegin(GL.GL_TRIANGLES);
		gl.glColor3f(1.0f, 0.0f, 0.0f);
		gl.glVertex3f(0.0f, 1.0f, 0.0f);
		gl.glColor3f(0.0f, 1.0f, 0.0f);
		gl.glVertex3f(-1.0f, -1.0f, 1.0f);
		gl.glColor3f(0.0f, 0.0f, 1.0f);
		gl.glVertex3f(1.0f, -1.0f, 1.0f);
		gl.glColor3f(1.0f, 0.0f, 0.0f);
		gl.glVertex3f(0.0f, 1.0f, 0.0f);
		gl.glColor3f(0.0f, 0.0f, 1.0f);
		gl.glVertex3f(1.0f, -1.0f, 1.0f);
		gl.glColor3f(0.0f, 1.0f, 0.0f);
		gl.glVertex3f(1.0f, -1.0f, -1.0f);
		gl.glColor3f(1.0f, 0.0f, 0.0f);
		gl.glVertex3f(0.0f, 1.0f, 0.0f);
		gl.glColor3f(0.0f, 1.0f, 0.0f);
		gl.glVertex3f(1.0f, -1.0f, -1.0f);
		gl.glColor3f(0.0f, 0.0f, 1.0f);
		gl.glVertex3f(-1.0f, -1.0f, -1.0f);
		gl.glColor3f(1.0f, 0.0f, 0.0f);
		gl.glVertex3f(0.0f, 1.0f, 0.0f);
		gl.glColor3f(0.0f, 0.0f, 1.0f);
		gl.glVertex3f(-1.0f, -1.0f, -1.0f);
		gl.glColor3f(0.0f, 1.0f, 0.0f);
		gl.glVertex3f(-1.0f, -1.0f, 1.0f);
		gl.glEnd();
	}

	private static abstract class CompiledShape
	{
		private int listIndex;

		public CompiledShape( GL gl)
		{
			this.listIndex = gl.glGenLists(1);
		}

		public int getListIndex()
		{
			return this.listIndex;
		}

		public void draw( GL gl )
		{
			gl.glCallList(this.getListIndex());
		}

		public void dispose( GL gl )
		{
			System.out.println("Compiled shape dispose...");
			gl.glDeleteLists(this.getListIndex(), 1);
		}
	}


}

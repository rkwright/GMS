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
 *******************************************************************************/
package com.geofx.scenes.misc;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.geofx.opengl.scene.GLScene;
import com.geofx.opengl.view.GLComposite;

/**
 * Draws a picture I needed for the article.
 * 
 * @author Bo Majewski
 */
public class RotationScene extends GLScene
{
	public static final float AXIS_SIZE = 5.0f;

  	/**
   	 * The Rotation constructor.  Like all the scene derived classes, it 
   	 * takes a Composite parent and an AWT Frame object.
   	 * @param parent
   	 * @param glFrame
   	 */
	public RotationScene( GLComposite parent )
	{
		super(parent);
	
		System.out.println("RotationScene - constructor");

		glComposite.getGrip().setOffsets(-3.25f, 3.25f, -30.5f);
		glComposite.getGrip().setRotation(45.0f, -30.0f, 0.0f);
	}

	/**
	 * a default constructor used by the ClassInfo enumerator
	 */ 
	public RotationScene()
	{
		super();
	}
	
	/**
	 * The scene's implementation of the init method.
	 */
	public void init ( GLAutoDrawable drawable ) 
	{
		System.out.println("RotationScene - init");
		super.init(drawable);	
	}

	/**
	 * Here we actually draw the scene.
	 */
	public void display(GLAutoDrawable drawable) 
	{
		super.display(drawable);
	
		System.out.println("RotationScene - display");
		GL gl = drawable.getGL();
		
		double angle, slice;
		float x = 0.0f, y = 0.0f, z = 0.0f;


		gl.glColor3f(1.0f, 1.0f, 1.0f);
		gl.glLineWidth(2.0f);

		// Z axis - red
		gl.glColor3f(1.0f, 0.0f, 0.0f);

		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glVertex3f(0.0f, 0.0f, AXIS_SIZE);
		gl.glVertex3f(0.0f, 0.0f, 0.0f);
		gl.glEnd();
		
		// X axis - green
		gl.glColor3f(0.0f, 1.0f, 0.0f);

		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glVertex3f(0.0f, 0.0f, 0.0f);
		gl.glVertex3f(AXIS_SIZE, 0.0f, 0.0f);
		gl.glEnd();
			
		// Y axis - blue
		gl.glColor3f(0.0f, 0.0f, 1.0f);

		gl.glBegin(GL.GL_LINES);
		gl.glVertex3f(0.0f, 0.0f, 0.0f);
		gl.glVertex3f(0.0f, AXIS_SIZE, 0.0f);
		gl.glEnd();

		gl.glColor3f(1.0f, 1.0f, 1.0f);

		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glVertex3f(-0.1f, AXIS_SIZE - 0.2f, 0.0f);
		gl.glVertex3f(0.0f, AXIS_SIZE, 0.0f);
		gl.glVertex3f(0.1f, AXIS_SIZE - 0.2f, 0.0f);
		gl.glEnd();
		
		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glVertex3f(AXIS_SIZE - 0.2f, 0.0f, -0.1f);
		gl.glVertex3f(AXIS_SIZE, 0.0f, 0.0f);
		gl.glVertex3f(AXIS_SIZE - 0.2f, 0.0f, 0.1f);
		gl.glEnd();
		
		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glVertex3f(0.0f, -0.1f, AXIS_SIZE - 0.2f);
		gl.glVertex3f(0.0f, 0.0f, AXIS_SIZE);
		gl.glVertex3f(0.0f, 0.1f, AXIS_SIZE - 0.2f);
		gl.glEnd();

		angle = 0.0;
		slice = (2 * Math.PI / 100.0);

		gl.glTranslatef(AXIS_SIZE - 1.0f, 0.0f, 0.0f);
		
		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glVertex3f(-0.1f, 0.2f, 1.0f);
		gl.glVertex3f(0.0f, 0.0f, 1.0f);
		gl.glVertex3f(0.1f, 0.2f, 1.0f);
		gl.glEnd();
		
		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glVertex3f(0.0f, 0.0f, 1.0f);

		for (int i = 0; i < 90; ++i)
		{
			y = (float) Math.sin(angle);
			z = (float) Math.cos(angle);
			gl.glVertex3f(0.0f, y, z);
			angle += slice;
		}

		gl.glEnd();
		
		gl.glTranslatef(-AXIS_SIZE + 1.0f, AXIS_SIZE - 1.0f, 0.0f);
		angle = 0.0;
		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glVertex3f(0.2f, 0.1f, 1.0f);
		gl.glVertex3f(0.0f, 0.0f, 1.0f);
		gl.glVertex3f(0.2f, -0.1f, 1.0f);
		gl.glEnd();
		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glVertex3f(0.0f, 0.0f, 1.0f);

		for (int i = 0; i < 90; ++i)
		{
			x = (float) Math.sin(angle);
			z = (float) Math.cos(angle);
			gl.glVertex3f(x, 0, z);
			angle += slice;
		}

		gl.glEnd();
		gl.glTranslatef(0.0f, -AXIS_SIZE + 1.0f, AXIS_SIZE - 1.0f);
		
		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glVertex3f(0.2f, 1.0f, -0.1f);
		gl.glVertex3f(0.0f, 1.0f, 0.0f);
		gl.glVertex3f(0.2f, 1.0f, 0.1f);
		gl.glEnd();
		
		angle = 0.0;
		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glVertex3f(0.0f, 1.0f, 0.0f);

		for (int i = 0; i < 90; ++i)
		{
			x = (float) Math.sin(angle);
			y = (float) Math.cos(angle);
			gl.glVertex3f(x, y, 0.0f);
			angle += slice;
		}

		gl.glEnd();
	}

	/** 
	 * Return the string that is the description of this scene
	 */
	public String getDescription()
	{
		return "A simple set of XYZ axes to demonstrate the UI";
	}

	/**
	 * Return the string that is the label for this string that
	 * will be shown in the "SelectDialog"
	 */
	public String getLabel()
	{
		return "Simple XYZ Axes";
	}
}

/*******************************************************************************
 * 
 * 
 * Code from the JOGL Demos:  https://jogl-demos.dev.java.net/
 * 
 * Contributors:
 *    
 *     Ric Wright - May 2008 - Ported to Eclipse 3.2, minor tweaks
 *******************************************************************************/
package com.geofx.scenes.misc;

import java.awt.Font;
import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.geofx.opengl.scene.GLScene;
import com.geofx.opengl.view.GLComposite;
import com.sun.opengl.util.j2d.TextRenderer;

public class ProjectionScene extends GLScene
{
	 
	private float xAng;
	private float yAng;
	private TextRenderer renderer;
	private float textScaleFactor;
	
  	/**
   	 * The Projection Scene constructor.  Like all the scene-derived classes, 
   	 * it takes a GLComposite parent
   	 * @param parent
   	 */
	public ProjectionScene( GLComposite parent )
	{
		super(parent);
	
		System.out.println("Projection Scene - constructor");

		glComposite.getGrip().setOffsets(0.0f, 0.0f, -3.0f);
		glComposite.getGrip().setRotation(45.0f, -30.0f, 0.0f);
	}

	/**
	 * a default constructor used by the ClassInfo enumerator
	 */ 
	public ProjectionScene()
	{
		super();
	}

	/**
	 * The scene's implementation of the init method.
	 */
	public void init ( GLAutoDrawable drawable ) 
	{
		super.init(drawable);	
		
		System.out.println("Projection Scene - init");

	//	initLighting(drawable.getGL());	

		InitTextCube();
	}
	
	private void initLighting(final GL gl)
	{
		float[] 	LightDiffuse =	 { 1.0f, 1.0f, 1.0f, 1.0f };
		float[] 	LightAmbient =	 { 0.75f, 0.75f, 0.75f, 1.0f };
		float[] 	LightPosition =	 { 1.0f, 1.0f, -1.0f, 0.0f };
		float[]		mat_specular = { 1.0f, 1.0f, 1.0f, 1.0f };
		float[]     mat_shininess = { 50.0f };	
        
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, LightAmbient, 0);						
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, LightDiffuse, 0);				

		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, mat_specular, 0);
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SHININESS, mat_shininess, 0);

		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, LightPosition, 0);			

		gl.glEnable(GL.GL_DEPTH_TEST);
	    gl.glDepthFunc(GL.GL_LESS);    
	           
	    glComposite.setLightingEnabled(true);
	    gl.glEnable(GL.GL_LIGHT0);
    
	}
	
	/*
	  gluPerspective(f32 fov,f32 aspect,f32 znear,f32 zfar) 
	  {
		f64 range = znear*tan(DEG_TO_RAD(fov/2));
		glFrustum(-range*aspect,range*aspect,-range,range,znear,zfar);
		
		
      }
    */
	public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) 
	{
		System.out.println("Projection Scene - reshape");

		final GL gl = drawable.getGL();		
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();

		/*
		double	left = -1;
		double	right = 1;
		double 	bottom = -1;
		double 	top = 1;
		double 	nearZ = 1;
		double 	farZ = 4;
		*/
		//double 	near = 0.1;
		//double 	far = 100;
		//double 	fovy = 45;
		//double 	aspect =(double)width/(double)height;
		
		//gl.glFrustum(left, right, bottom, top, nearZ, farZ);
		//gl.glFrustum(-0.06, 0.06, -0.04, -0.04, 0.1, 100);
		//glu.gluPerspective(fovy, aspect, near, far);

		gl.glOrtho(-1.0, 1.0, -1.0, 1.0, 5, 100);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();

	}
	
	
	/**
	 * Here we actually draw the scene.
	 */
	public void display(GLAutoDrawable drawable) 
	{
		super.display(drawable);
			
		System.out.println("Projection Scene - display");
		GL gl = drawable.getGL();
		
		try
		{
			gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

			glComposite.updateLighting(gl);
	
			//	gl.glRotatef(10.0f, 0.0f, 0.0f, 1.0f);

			glComposite.drawAxes(gl);
			
			drawTracks(gl);
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	private void drawTracks ( GL gl )
	{
		/*
		gl.glColor3f(1.0f, 0.0f, 0.0f);
		gl.glBegin(GL.GL_LINE_STRIP);
		
		gl.glVertex3f(-0.5f, -0.5f, -1.0f);
		gl.glVertex3f(-0.5f, -0.5f, 1.0f);
		
		gl.glEnd();
	
		gl.glColor3f(0.0f, 1.0f, 0.0f);
		gl.glBegin(GL.GL_LINE_STRIP);
		
		gl.glVertex3f(-1.0f, -1.0f, -1.0f);
		gl.glVertex3f(-1.0f, 1.0f, -1.0f);
		
		gl.glEnd();

		
		gl.glColor3f(0.0f, 0.0f, 1.0f);		
		gl.glBegin(GL.GL_LINE_STRIP);
		
		gl.glVertex3f(-1.0f, -1.0f, -1.0f);
		gl.glVertex3f(-1.0f, -1.0f, 4.0f);
		
		gl.glEnd();

		gl.glColor3f(0.0f, 1.0f, 1.0f);		
		gl.glBegin(GL.GL_LINE_STRIP);
		
		gl.glVertex3f(-1.0f, -1.0f, -1.0f);
		gl.glVertex3f(1.0f, 1.0f, 4.0f);
		
		gl.glEnd();

		gl.glColor3f(1.0f, 1.0f, 0.0f);		
		gl.glBegin(GL.GL_LINE_STRIP);
		
		gl.glVertex3f(1.0f, -1.0f, -1.0f);
		gl.glVertex3f(-1.0f, -1.0f, 4.0f);
		
		gl.glEnd();

		gl.glColor3f(1.0f, 0.0f, 1.0f);		
		gl.glBegin(GL.GL_LINE_STRIP);
		
		gl.glVertex3f(-1.0f, -1.0f, -1.0f);
		gl.glVertex3f(-1.0f, 1.0f, 4.0f);
		
		gl.glEnd();
		*/
	}

	/*
	private void drawFloor( GL gl )
	{
		gl.glBegin(GL.GL_QUADS);
		
		for ( int i=0; i<10; i++ )
		{
			for ( int j=0; j<10; j++ )
			{				
				gl.glColor3f( 1.0f,1.0f,1.0f );
				gl.glVertex3f( 0.0f, 1.0f, 0.0f);	
			}
		}
		
		gl.glEnd();
	}
	*/
	private void InitTextCube()
	{
		renderer = new TextRenderer(new Font("Times New Roman", Font.TRUETYPE_FONT, 72));
		
		// Compute the scale factor of the largest string which will make
		// them all fit on the faces of the cube
		Rectangle2D bounds = renderer.getBounds("Bottom");
		float w = (float) bounds.getWidth();
		// float h = (float) bounds.getHeight();
		textScaleFactor = 1.0f / (w * 1.1f);
	}
	
	@SuppressWarnings("unused")
	private void DrawTextCube(GL gl)
	{
		// Base rotation of cube
		gl.glRotatef(xAng, 1, 0, 0);
		gl.glRotatef(yAng, 0, 1, 0);

		// Six faces of cube
		// Top face
		gl.glPushMatrix();
		gl.glRotatef(-90, 1, 0, 0);
		drawFace(gl, 1.0f, 0.2f, 0.2f, 0.8f, "Top");
		gl.glPopMatrix();
		// Front face
		drawFace(gl, 1.0f, 0.8f, 0.2f, 0.2f, "Front");
		// Right face
		gl.glPushMatrix();
		gl.glRotatef(90, 0, 1, 0);
		drawFace(gl, 1.0f, 0.2f, 0.8f, 0.2f, "Right");
		// Back face
		gl.glRotatef(90, 0, 1, 0);
		drawFace(gl, 1.0f, 0.8f, 0.8f, 0.2f, "Back");
		// Left face
		gl.glRotatef(90, 0, 1, 0);
		drawFace(gl, 1.0f, 0.2f, 0.8f, 0.8f, "Left");
		gl.glPopMatrix();
		// Bottom face
		gl.glPushMatrix();
		gl.glRotatef(90, 1, 0, 0);
		drawFace(gl, 1.0f, 0.8f, 0.2f, 0.8f, "Bottom");
		gl.glPopMatrix();
	}

	private void drawFace(GL gl, float faceSize, float r, float g, float b, String text)
	{
		float halfFaceSize = faceSize / 2;
		
		// Face is centered around the local coordinate system's z axis,
		// at a z depth of faceSize / 2
		gl.glColor3f(r, g, b);
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex3f(-halfFaceSize, -halfFaceSize, halfFaceSize);
		gl.glVertex3f(halfFaceSize, -halfFaceSize, halfFaceSize);
		gl.glVertex3f(halfFaceSize, halfFaceSize, halfFaceSize);
		gl.glVertex3f(-halfFaceSize, halfFaceSize, halfFaceSize);
		gl.glEnd();

		// Now draw the overlaid text. In this setting, we don't want the
		// text on the backward-facing faces to be visible, so we enable
		// back-face culling; and since we're drawing the text over other
		// geometry, to avoid z-fighting we disable the depth test. We
		// could plausibly also use glPolygonOffset but this is simpler.
		// Note that because the TextRenderer pushes the enable state
		// internally we don't have to reset the depth test or cull face
		// bits after we're done.
		renderer.begin3DRendering();
		
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_CULL_FACE);
		
		// Note that the defaults for glCullFace and glFrontFace are
		// GL_BACK and GL_CCW, which match the TextRenderer's definition
		// of front-facing text.
		Rectangle2D bounds = renderer.getBounds(text);
		float w = (float) bounds.getWidth();
		float h = (float) bounds.getHeight();
		
		renderer.draw3D(text, w / -2.0f * textScaleFactor, h / -2.0f * textScaleFactor, halfFaceSize, textScaleFactor);
		
		renderer.end3DRendering();
	}
	
	/** 
	 * Return the string that is the description of this scene
	 */
	public String getDescription()
	{
		return "A simple demo of Projection";
	}

	/**
	 * Return the string that is the label for this string that
	 * will be shown in the "SelectDialog"
	 */
	public String getLabel()
	{
		return "Projection Demo";
	}
}

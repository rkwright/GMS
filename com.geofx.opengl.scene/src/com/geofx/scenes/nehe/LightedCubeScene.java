/*******************************************************************************
 * 
 * 
 * Code from the JOGL Demos:  https://jogl-demos.dev.java.net/
 * 
 * Contributors:
 *    
 *     Ric Wright - May 2008 - Ported to Eclipse 3.2, minor tweaks
 *******************************************************************************/
package com.geofx.scenes.nehe;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Vector3f;

import com.geofx.opengl.scene.GLScene;
import com.geofx.opengl.view.GLComposite;

public class LightedCubeScene extends GLScene 
{	
	public LightedCubeScene( GLComposite parent )
	{
		super(parent);

		System.out.println("LightedCubeScene - constructor");

		glComposite.getGrip().setOffsets(0.0f, 0.0f, -3.0f);
		glComposite.getGrip().setRotation(45.0f, -30.0f, 0.0f);
	}

	// a default constructor used by the ClassInfo enumerator
	public LightedCubeScene()
	{
		super();
	}


	public void init ( GLAutoDrawable drawable ) 
	{
		super.init(drawable);	
		
		System.out.println("LightedCubeScene - init");
	}

	public void display(GLAutoDrawable drawable) 
	{
		super.display(drawable);
	
		System.out.println("LightedCube - display");
		GL gl = drawable.getGL();

		try
		{
			glComposite.updateLighting(gl);
		
			glComposite.drawAxes(gl);

			drawCube(gl);
           
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void drawCube(GL gl)
	{
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
		

		gl.glColor3i( 0xff, 0xff, 0);
		
		Vector3f vecA = new Vector3f( halfFaceSize, 0.0f, 0.0f);
		Vector3f vecB = new Vector3f( 0.0f, halfFaceSize, 0.0f);
		Vector3f normal = new Vector3f();
		normal.cross( vecA, vecB );
		normal.normalize();
		
		/*
		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glVertex3f(0.0f, 0.0f, 0.0f);
		gl.glVertex3f(0.0f, 0.0f, faceSize);
		gl.glEnd();
		*/
		
		gl.glNormal3f( normal.x, normal.y, normal.z ); 
		//gl.glNormal3f( 0.0f, 0.0f, faceSize);

		gl.glColor3f(r, g, b);
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex3f(-halfFaceSize, -halfFaceSize, halfFaceSize);
		gl.glVertex3f(halfFaceSize, -halfFaceSize, halfFaceSize);
		gl.glVertex3f(halfFaceSize, halfFaceSize, halfFaceSize);
		gl.glVertex3f(-halfFaceSize, halfFaceSize, halfFaceSize);
		gl.glEnd();
	}
	
/*
	public void  enableLighting( GL gl )
	{
	    gl.glEnable(GL.GL_LIGHT0);
	}
	*/
	public String getDescription()
	{
		return "A simple demo of a lighted cube";
	}

	public String getLabel()
	{
		return "Lighted Cube";
	}

}

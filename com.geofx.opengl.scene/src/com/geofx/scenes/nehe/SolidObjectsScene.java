/*******************************************************************************
 * 
 * Code from Lesson 8 at NeHe. http://www.nehe.com 
 * 
 * Drawn from the article on NeHe.
 * http://nehe.gamedev.net/data/lessons/lesson.asp?lesson=05
 * 
 * Contributors:    
 *     Ric Wright - Jan 2008 - Ported from NeHe
 *     Ric Wright - May 2008 - ported to JOGL
 *******************************************************************************/

package com.geofx.scenes.nehe;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.geofx.opengl.scene.GLScene;
import com.geofx.opengl.view.GLComposite;

/**
 * Draws a simple cube and pyramid.  
 * 
 */
public class SolidObjectsScene extends GLScene
{
	public static final float AXIS_SIZE = 5.0f;

   	/**
   	 * The Solid Objects constructor.  Like all the scene derived classes, it 
   	 * takes a Composite parent and an AWT Frame object.
   	 * @param parent
   	 * @param glFrame
   	 */
	public SolidObjectsScene( GLComposite parent )
	{
		super(parent);
	}

	/**
	 * a default constructor used by the ClassInfo enumerator
	 */
	public SolidObjectsScene()
	{
		super();
	}

	/**
	 * The scene's implementation of the init method.
	 */
	public void init ( GLAutoDrawable drawable ) 
	{
		System.out.println("SolidObjectsScene - init");	
		super.init(drawable);			
	}
	
	/**
	 * Here we actually draw the scene.
	 */
	public void display(GLAutoDrawable drawable) 
	{
		super.display(drawable);
	
		GL gl = drawable.getGL();

		drawable.getGL().glTranslatef(-1.5f,-2.0f,-3.0f);	

		drawPyramid(gl);

		gl.glTranslatef(1.5f,0.0f,-7.0f);	

		drawCube(gl);
	}

	// draw some content
	protected void drawPyramid(GL gl)
	{
		/**
		 * Expanding on the last tutorial, we'll now make the object into TRUE 3D object, rather than 
		 * 2D objects in a 3D world. We will do this by adding a left, back, and right side to the triangle, 
		 * and a left, right, back, top and bottom to the square. By doing this, we turn the triangle into 
		 * a pyramid, and the square into a cube.
         *
		 * We'll blend the colors on the pyramid, creating a smoothly colored object, and for the square 
		 * we'll color each face a different color.
		 */
		
		gl.glBegin(GL.GL_TRIANGLES);		
	
		/**
		 * The following code will create the pyramid around a central axis. The top of the pyramid is one high from 
		 * the center, the bottom of the pyramid is one down from the center. The top point is right in the middle 
		 * (zero), and the bottom points are one left from center, and one right from center.
		 * 
		 * Note that all triangles are drawn in a counterclockwise rotation. This is important, and will be explained 
		 * in a future tutorial, for now, just know that it's good practice to make objects either clockwise or 
		 * counterclockwise, but you shouldn't mix the two unless you have a reason to.
		 * 
		 * We start off by drawing the Front Face. Because all of the faces share the top point, we will make 
		 * this point red on all of the triangles. The color on the bottom two points of the triangles will alternate. 
		 * The front face will have a green left point and a blue right point. Then the triangle on the right side will 
		 * have a blue left point and a green right point. By alternating the bottom two colors on each face, we make 
		 * a common colored point at the bottom of each face.		
		 */
		
		gl.glColor3f(1.0f,0.0f,0.0f);			// Red
		gl.glVertex3f( 0.0f, 1.0f, 0.0f);			// Top Of Triangle (Front)
		gl.glColor3f(0.0f,1.0f,0.0f);			// Green
		gl.glVertex3f(-1.0f,-1.0f, 1.0f);			// Left Of Triangle (Front)
		gl.glColor3f(0.0f,0.0f,1.0f);			// Blue
		gl.glVertex3f( 1.0f,-1.0f, 1.0f);			// Right Of Triangle (Front)
		
		/**
		 * Now we draw the right face. Notice then the two bottom point are drawn one to the right of center, and 
		 * the top point is drawn one up on the y axis, and right in the middle of the x axis. causing the face 
		 * to slope from center point at the top out to the right side of the screen at the bottom.
		 * 
		 * Notice the left point is drawn blue this time. By drawing it blue, it will be the same color as the 
		 * right bottom corner of the front face. Blending blue outwards from that one corner across both the 
		 * front and right face of the pyramid.
		 * 
		 * Notice how the remaining three faces are included inside the same glBegin(GL_TRIANGLES) and glEnd() 
		 * as the first face. Because we're making this entire object out of triangles, OpenGL will know that 
		 * every three points we plot are the three points of a triangle. Once it's drawn three points, if there are 
		 * three more points, it will assume another triangle needs to be drawn. If you were to put four points 
		 * instead of three, OpenGL would draw the first three and assume the fourth point is the start of a new triangle. 
		 * It would not draw a Quad. So make sure you don't add any extra points by accident.
		 */
		
		gl.glColor3f(1.0f,0.0f,0.0f);			// Red
		gl.glVertex3f( 0.0f, 1.0f, 0.0f);			// Top Of Triangle (Right)
		gl.glColor3f(0.0f,0.0f,1.0f);			// Blue
		gl.glVertex3f( 1.0f,-1.0f, 1.0f);			// Left Of Triangle (Right)
		gl.glColor3f(0.0f,1.0f,0.0f);			// Green
		gl.glVertex3f( 1.0f,-1.0f, -1.0f);			// Right Of Triangle (Right)
		
		/**
		 * Now for the back face. Again the colors switch. The left point is now green again, because the corner 
		 * it shares with the right face is green.
		 */
		
		gl.glColor3f(1.0f,0.0f,0.0f);			// Red
		gl.glVertex3f( 0.0f, 1.0f, 0.0f);			// Top Of Triangle (Back)
		gl.glColor3f(0.0f,1.0f,0.0f);			// Green
		gl.glVertex3f( 1.0f,-1.0f, -1.0f);			// Left Of Triangle (Back)
		gl.glColor3f(0.0f,0.0f,1.0f);			// Blue
		gl.glVertex3f(-1.0f,-1.0f, -1.0f);			// Right Of Triangle (Back)
		
		/**
		 * Finally we draw the left face. The colors switch one last time. The left point is blue, and blends 
		 * with the right point of the back face. The right point is green, and blends with the left point of 
		 * the front face.
		 * 
		 * We're done drawing the pyramid. Because the pyramid only spins on the Y axis, we will never see the 
		 * bottom, so there is no need to put a bottom on the pyramid. If you feel like experimenting, 
		 * try adding a bottom using a quad, then rotate on the X axis to see if you've done it correctly. Make sure 
		 * the color used on each corner of the quad matches up with the colors being used at the four 
		 * corners of the pyramid.
		 */
		
		gl.glColor3f(1.0f,0.0f,0.0f);			// Red
		gl.glVertex3f( 0.0f, 1.0f, 0.0f);			// Top Of Triangle (Left)
		gl.glColor3f(0.0f,0.0f,1.0f);			// Blue
		gl.glVertex3f(-1.0f,-1.0f,-1.0f);			// Left Of Triangle (Left)
		gl.glColor3f(0.0f,1.0f,0.0f);			// Green
		gl.glVertex3f(-1.0f,-1.0f, 1.0f);			// Right Of Triangle (Left)
		
		gl.glEnd();	}
	
	/*
	 * Now we'll draw the cube. It's made up of six quads. All of the quads are drawn in a counter clockwise order. 
	 * Meaning the first point is the top right, the second point is the top left, third point is bottom left, 
	 * and finally bottom right. When we draw the back face, it may seem as though we are drawing clockwise, 
	 * but you have to keep in mind that if we were behind the cube looking at the front of it, the left side 
	 * of the screen is actually the right side of the quad, and the right side of the screen would actually be 
	 * the left side of the quad.
	 * 
	 * Notice we move the cube a little further into the screen in this lesson. By doing this, the size of the 
	 * cube appears closer to the size of the pyramid. If you were to move it only 6 units into the screen, the 
	 * cube would appear much larger than the pyramid, and parts of it might get cut off by the sides of the 
	 * screen. You can play around with this setting, and see how moving the cube further into the screen makes it 
	 * appear smaller, and moving it closer makes it appear larger. The reason this happens is perspective. Objects 
	 * in the distance should appear smaller :)
	 */
	protected void drawCube(GL gl)
	{
		gl.glBegin(GL.GL_QUADS);	
		
		/**
		 * We'll start off by drawing the top of the cube. We move up one unit from the center of the cube. Notice 
		 * that the Y axis is always one. We then draw a quad on the Z plane. Meaning into the screen. We start off 
		 * by drawing the top right point of the top of the cube. The top right point would be one unit right, and 
		 * one unit into the screen. The second point would be one unit to the left, and unit into the screen. Now 
		 * we have to draw the bottom of the quad towards the viewer. so to do this, instead of going into the screen, 
		 * we move one unit towards the screen. Make sense?
		 */
		
		gl.glColor3f(0.0f,1.0f,0.0f);			// Set The Color To Green
		gl.glVertex3f( 1.0f, 1.0f,-1.0f);			// Top Right Of The Quad (Top)
		gl.glVertex3f(-1.0f, 1.0f,-1.0f);			// Top Left Of The Quad (Top)
		gl.glVertex3f(-1.0f, 1.0f, 1.0f);			// Bottom Left Of The Quad (Top)
		gl.glVertex3f( 1.0f, 1.0f, 1.0f);			// Bottom Right Of The Quad (Top)
		
		/**
		 * The bottom is drawn the exact same way as the top, but because it's the bottom, it's drawn down one unit 
		 * from the center of the cube. Notice the Y axis is always minus one. If we were under the cube, looking 
		 * at the quad that makes the bottom, you would notice the top right corner is the corner closest to the 
		 * viewer, so instead of drawing in the distance first, we draw closest to the viewer first, then on the 
		 * left side closest to the viewer, and then we go into the screen to draw the bottom two points.
		 * 
		 * If you didn't really care about the order the polygons were drawn in (clockwise or not), you could just 
		 * copy the same code for the top quad, move it down on the Y axis to -1, and it would work, but ignoring 
		 * the order the quad is drawn in can cause weird results once you get into fancy things such as texture 
		 * mapping.
		 */
		
		gl.glColor3f(1.0f,0.5f,0.0f);			// Set The Color To Orange
		gl.glVertex3f( 1.0f,-1.0f, 1.0f);			// Top Right Of The Quad (Bottom)
		gl.glVertex3f(-1.0f,-1.0f, 1.0f);			// Top Left Of The Quad (Bottom)
		gl.glVertex3f(-1.0f,-1.0f,-1.0f);			// Bottom Left Of The Quad (Bottom)
		gl.glVertex3f( 1.0f,-1.0f,-1.0f);			// Bottom Right Of The Quad (Bottom)
		
		/**
		 * Now we draw the front of the Quad. We move one unit towards the screen, and away from the center to 
		 * draw the front face. Notice the Z axis is always one. In the pyramid the Z axis was not always one. 
		 * At the top, the Z axis was zero. If you tried changing the Z axis to zero in the following code, 
		 * you'd notice that the corner you changed it on would slope into the screen. That's not something we 
		 * want to do right now :)
		 */
		
		gl.glColor3f(1.0f,0.0f,0.0f);			// Set The Color To Red
		gl.glVertex3f( 1.0f, 1.0f, 1.0f);			// Top Right Of The Quad (Front)
		gl.glVertex3f(-1.0f, 1.0f, 1.0f);			// Top Left Of The Quad (Front)
		gl.glVertex3f(-1.0f,-1.0f, 1.0f);			// Bottom Left Of The Quad (Front)
		gl.glVertex3f( 1.0f,-1.0f, 1.0f);			// Bottom Right Of The Quad (Front)
		
		/** 
		 * The back face is a quad the same as the front face, but it's set deeper into the screen. Notice the 
		 * Z axis is now minus one for all of the points.
		 */
		
		gl.glColor3f(1.0f,1.0f,0.0f);			// Set The Color To Yellow
		gl.glVertex3f( 1.0f,-1.0f,-1.0f);			// Bottom Left Of The Quad (Back)
		gl.glVertex3f(-1.0f,-1.0f,-1.0f);			// Bottom Right Of The Quad (Back)
		gl.glVertex3f(-1.0f, 1.0f,-1.0f);			// Top Right Of The Quad (Back)
		gl.glVertex3f( 1.0f, 1.0f,-1.0f);			// Top Left Of The Quad (Back)
		
		/** 
		 * Now we only have two more quads to draw and we're done. As usual, you'll notice one axis is always 
		 * the same for all the points. In this case the X axis is always minus one. That's because we're 
		 * always drawing to the left of center because this is the left face.
		 */
		
		gl.glColor3f(0.0f,0.0f,1.0f);			// Set The Color To Blue
		gl.glVertex3f(-1.0f, 1.0f, 1.0f);			// Top Right Of The Quad (Left)
		gl.glVertex3f(-1.0f, 1.0f,-1.0f);			// Top Left Of The Quad (Left)
		gl.glVertex3f(-1.0f,-1.0f,-1.0f);			// Bottom Left Of The Quad (Left)
		gl.glVertex3f(-1.0f,-1.0f, 1.0f);			// Bottom Right Of The Quad (Left)
		
		/**
		 * This is the last face to complete the cube. The X axis is always one. Drawing is counter clockwise. 
		 * If you wanted to, you could leave this face out, and make a box :)
		 * 
		 * Or if you felt like experimenting, you could always try changing the color of each point on the cube 
		 * to make it blend the same way the pyramid blends. You can see an example of a blended cube by downloading 
		 * Evil's first GL demo from my web page. Run it and press TAB. You'll see a beautifully colored cube, with 
		 * colors flowing across all the faces.
		 */
		
		gl.glColor3f(1.0f,0.0f,1.0f);			// Set The Color To Violet
		gl.glVertex3f( 1.0f, 1.0f,-1.0f);			// Top Right Of The Quad (Right)
		gl.glVertex3f( 1.0f, 1.0f, 1.0f);			// Top Left Of The Quad (Right)
		gl.glVertex3f( 1.0f,-1.0f, 1.0f);			// Bottom Left Of The Quad (Right)
		gl.glVertex3f( 1.0f,-1.0f,-1.0f);			// Bottom Right Of The Quad (Right)
		gl.glEnd();						// Done Drawing The Quad
}

	/** 
	 * Return the string that is the description of this scene
	 */
	public String getDescription()
	{
		return "A set of simple solid 3D objects";
	}

	/**
	 * Return the string that is the label for this string that
	 * will be shown in the "SelectDialog"
	 */
	public String getLabel()
	{
		return "Solid Objects";
	}
}

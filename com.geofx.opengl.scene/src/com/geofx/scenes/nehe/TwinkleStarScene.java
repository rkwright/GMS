/*******************************************************************************
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html 
 * 
 * Contributors:
 *     Ric Wright - Jan 2008 - Ported from NeHe
 *******************************************************************************/
package com.geofx.scenes.nehe;

import java.awt.event.KeyEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.geofx.opengl.scene.GLScene;
import com.geofx.opengl.view.GLComposite;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;


public class TwinkleStarScene extends GLScene
{
	public static final String textureFile = "/resource/star.png";

	Texture texture;
	
	int 		filter = 0; 			// Which Filter To Use
    boolean		twinkle = false;		// Twinkling Stars
    float		zoom = -15.0f;			// Distance Away From Stars
    float 		tilt = 90.0f;			// Tilt The View
    float		spin;					// Spin Stars
 
    final int 	numStars = 50;				// Number Of Stars To Draw
  
    // Our star sub-class
    private class Star			
    {
       int r=0, g=0, b=0;				// Star's Color
       float dist=0.0f,angle=0.0f;		// Star's Current Angle & Distance From Center
    }
    
    Star[] star = new Star[numStars];			// Need To Keep Track Of 'num' Stars
 
	
  	/**
   	 * The Twinkle Star constructor.  Like all the scene derived classes, it 
   	 * takes a Composite parent and an AWT Frame object.
   	 * @param parent
   	 * @param glFrame
   	 */
    public TwinkleStarScene( GLComposite parent )
	{
		super(parent);

		glComposite.getGrip().setOffsets(-3.25f, 3.25f, -20.5f);
		glComposite.getGrip().setRotation(45.0f, -30.0f, 0.0f);
		
        for (int loop=0; loop<numStars; loop++)
        {
           star[loop] = new Star();
           star[loop].angle = 0.0f;
           star[loop].dist = ((float)(loop)/numStars)*5.0f;
           star[loop].r = (int)(256 * Math.random());
           star[loop].g = (int)(256 * Math.random());
           star[loop].b = (int)(256 * Math.random());
        }		
	}

	/**
	 * a default constructor used by the ClassInfo enumerator
	 */ 
    public TwinkleStarScene()
	{
		super();
	}
	
	/**
	 * Any intialization that is specific to this particular scene goes here.  In this
	 * case, the only specific init is GL_TEXTURE_2D which enables the texture mapping
	 */
	public void init(GLAutoDrawable drawable)
	{
		super.init(drawable);

		System.out.println("TwinkleStarScene - init");

		final GL gl = drawable.getGL();
		
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);                    // Black Background
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);                  // Set The Blending Function For Translucency
 		gl.glDepthFunc(GL.GL_LEQUAL);
 
		LoadGLTextures(gl);
		
		glComposite.initAnimator(drawable, 100);
	}
	

	/**
	 * There are a few VERY important things you need to know about the images you plan 
	 * to use as textures. The image height and width MUST be a power of 2. The width and 
	 * height must be at least 64 pixels, and for compatability reasons, shouldn't be 
	 * more than 256 pixels. If the image you want to use is not 64, 128 or 256 pixels 
	 * on the width or height, resize it in an art program. There are ways around this 
	 * limitation, but for now we'll just stick to standard texture sizes.
	 */
	public boolean LoadGLTextures( GL gl )
	{		
		
		texture = glComposite.createTexture(this, textureFile, GL.GL_LINEAR, false);
		
		return texture != null;
	}
	
	/**
	 * Here we actually draw the scene.
	 */
	public void display(GLAutoDrawable drawable) 
	{
		super.display(drawable);
	
		System.out.println("TwinkleStarScene - display");
		GL gl = drawable.getGL();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);       //Clear The Screen And The Depth Buffer
  
   		TextureCoords tc = texture.getImageTexCoords();
		float tx1 = tc.left();
		float ty1 = tc.top();
		float tx2 = tc.right();
		float ty2 = tc.bottom();

		texture.enable();
		texture.bind();

        for (int loop=0; loop<numStars; loop++)						// Loop Through All The Stars
        {
        	System.out.println("Star: " + loop + " dist: " + star[loop].dist);
        	
        	gl.glLoadIdentity();								// Reset The View Before We Draw Each Star
        	gl.glTranslatef(0.0f, 0.0f, zoom);					// Zoom Into The Screen (Using The Value In 'zoom')
        	
        	gl.glRotatef(tilt, 1.0f, 0.0f, 0.0f);					// Tilt The View (Using The Value In 'tilt')
        	gl.glRotatef(star[loop].angle, 0.0f, 1.0f, 0.0f);		// Rotate To The Current Stars Angle
        	
        	gl.glTranslatef(star[loop].dist, 0.0f, 0.0f);		// Move Forward On The X Plane
        	
        	gl.glRotatef(-star[loop].angle, 0.0f, 1.0f, 0.0f);	// Cancel The Current Stars Angle
        	gl.glRotatef(-tilt, 1.0f, 0.0f, 0.0f);				// Cancel The Screen Tilt
        
    		
           if (twinkle)
           {
        	   gl.glColor4ub((byte)star[(numStars-loop)-1].r,(byte)star[(numStars-loop)-1].g,(byte)star[(numStars-loop)-1].b,(byte)255);
        	   gl.glBegin(GL.GL_QUADS);
        	   
        	   gl.glTexCoord2f(tx1, ty2);
        	   gl.glVertex3f(-1.0f,-1.0f, 0.0f);
        	   
           	   gl.glTexCoord2f(tx2, ty2);
           	   gl.glVertex3f( 1.0f,-1.0f, 0.0f);
        	   
           	   gl.glTexCoord2f(tx2, ty1);
           	   gl.glVertex3f( 1.0f, 1.0f, 0.0f);
        	   
           	   gl.glTexCoord2f(tx1, ty1);
           	   gl.glVertex3f(-1.0f, 1.0f, 0.0f);
        	   
        	   gl.glEnd();
           }
        
           gl.glRotatef(spin,0.0f,0.0f,1.0f);
           gl.glColor4ub((byte)star[loop].r,(byte)star[loop].g,(byte)star[loop].b,(byte)255);
           
           gl.glBegin(GL.GL_QUADS);
           
           gl.glTexCoord2f(tx1, ty2);
           gl.glVertex3f(-1.0f,-1.0f, 0.0f);
           
       	   gl.glTexCoord2f(tx2, ty2);
           gl.glVertex3f( 1.0f,-1.0f, 0.0f);
           
       	   gl.glTexCoord2f(tx2, ty1);
           gl.glVertex3f( 1.0f, 1.0f, 0.0f);
  
           gl.glTexCoord2f(tx1, ty1);
           gl.glVertex3f(-1.0f, 1.0f, 0.0f);
           
           gl.glEnd();
        
           spin += 0.01f;
           star[loop].angle += (float)(loop) / numStars;
           star[loop].dist -= 0.01f;
           if (star[loop].dist < 0.0f)
           {
              star[loop].dist += 5.0f;
              star[loop].r = (int)(256 * Math.random());
              star[loop].g = (int)(256 * Math.random());
              star[loop].b = (int)(256 * Math.random());
           }
        }		

	}

	/** 
	 * Return the string that is the description of this scene
	 */
	public String getDescription()
	{
		return "A solar system of twinking stars";
	}

	/** 
	 * Return the string that is the description of this scene
	 */
	public String getLabel()
	{
		return "Twinkle Stars";
	}
	
	protected boolean handleKeyEvent( KeyEvent e )
	{
		if (e.getKeyChar() == 't')
		{
			twinkle = ! twinkle;
			return true;
		}
		
		return false;
	}
}

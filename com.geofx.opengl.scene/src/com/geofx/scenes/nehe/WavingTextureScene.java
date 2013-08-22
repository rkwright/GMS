/*******************************************************************************
 * 
 * Code from Lesson 11 at NeHe. www.nehe.com 
 * Written by bosco (bosco4@home.com)
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
import com.sun.opengl.util.texture.Texture;

/**
 * Learning how to texture map has many benefits. Lets say you wanted a missile to fly 
 * across the screen. Up until this tutorial we'd probably make the entire missile out 
 * of polygons, and fancy colors. With texture mapping, you can take a real picture of 
 * a missile and make the picture fly across the screen. Which do you think will look 
 * better? A photograph or an object made up of triangles and squares? By using texture 
 * mapping, not only will it look better, but your program will run faster. The texture 
 * mapped missile would only be one quad moving across the screen. A missile made out 
 * of polygons could be made up of hundreds or thousands of polygons. The single texture 
 * mapped quad will use alot less processing power.
 * 
 */
public class WavingTextureScene extends GLScene
{
	public static final String 	textureFile = "/resource/Tim.png";
    Texture						texture;
    
    float[][][] points = new float[45][45][3];  // The Array For The Points On The Grid Of Our "Wave"
    int 		wiggle_count = 0;				// Counter Used To Control How Fast Flag Waves
    float 		xrot;				
    float 		yrot;				
    float 		zrot;				//
    float 		hold;

  	/**
   	 * The Waving Texture constructor.  Like all the scene derived classes, it 
   	 * takes a Composite parent and an AWT Frame object.
   	 * @param parent
   	 * @param glFrame
   	 */
    public WavingTextureScene( GLComposite parent )
	{
		super(parent);
	
		glComposite.getGrip().setOffsets( 0.0f, 0.0f, -5.0f);
		glComposite.getGrip().setRotation(45.0f, -30.0f, 0.0f);
	}

	/**
	 * a default constructor used by the ClassInfo enumerator
	 */ 
    public WavingTextureScene()
	{
		super();
	}
	/**
	 * Any intialization that is specific to this particular scene goes here.  In this
	 * case, the only specific init is GL_TEXTURE_2D which enables the texture mapping
	 */
	public void init(GLAutoDrawable drawable)
	{
		System.out.println("WavingTextureScene - init");

		super.init(drawable);

		LoadGLTextures(drawable.getGL());

		for (int x = 0; x < 45; x++)
		{
			for (int y = 0; y < 45; y++)
			{
				points[x][y][0] = ((x / 5.0f) - 4.5f);
				points[x][y][1] = ((y / 5.0f) - 4.5f);
				points[x][y][2] = (float) (Math.sin((((x / 5.0f) * 40.0f) / 360.0f) * Math.PI * 2.0f));
			}
		}
		
		glComposite.initAnimator(drawable, 100);
	}
	
	
	/**
	 * There are a few VERY important things you need to know about the images
	 * you plan to use as textures. The image height and width MUST be a power
	 * of 2. The width and height must be at least 64 pixels, and for
	 * compatability reasons, shouldn't be more than 256 pixels. If the image
	 * you want to use is not 64, 128 or 256 pixels on the width or height,
	 * resize it in an art program. There are ways around this limitation, but
	 * for now we'll just stick to standard texture sizes.
	 */
	public boolean LoadGLTextures( GL gl )
	 {		 
		// The Type Of Depth Testing To Do
		gl.glDepthFunc(GL.GL_LEQUAL);
	
		texture = glComposite.createTexture(this, textureFile, GL.GL_LINEAR, false);

		return texture != null;
	}		

	/**
	 * Here we actually draw the scene.
	 */
	public void display(GLAutoDrawable drawable) 
	{
		System.out.println("WavingTextureScene - display");

		super.display(drawable);
	
		GL gl = drawable.getGL();
		
        float float_x, float_y, float_xb, float_yb;
  
	    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT); // Clear The Screen And The Depth Buffer
		gl.glLoadIdentity(); // Reset The View

		gl.glTranslatef(0.0f, 0.0f, -12.0f);

		gl.glRotatef(xrot, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(yrot, 0.0f, 1.0f, 0.0f);
		gl.glRotatef(zrot, 0.0f, 0.0f, 1.0f);
		
		texture.enable();
		texture.bind();

		gl.glBegin(GL.GL_QUADS);
		for ( int x = 0; x < 44; x++)
		{
			for ( int y = 0; y < 44; y++)
			{
				float_x = (float)  (x) / 44.0f;
				float_y = (float)  1.0f - (y) / 44.0f;
				float_xb = (float)  (x + 1) / 44.0f;
				float_yb = (float) 1.0f -  (y + 1) / 44.0f;

				gl.glTexCoord2f(float_x, float_y);
				gl.glVertex3f(points[x][y][0], points[x][y][1], points[x][y][2]);

				gl.glTexCoord2f(float_x, float_yb);
				gl.glVertex3f(points[x][y + 1][0], points[x][y + 1][1], points[x][y + 1][2]);

				gl.glTexCoord2f(float_xb, float_yb);
				gl.glVertex3f(points[x + 1][y + 1][0], points[x + 1][y + 1][1], points[x + 1][y + 1][2]);

				gl.glTexCoord2f(float_xb, float_y);
				gl.glVertex3f(points[x + 1][y][0], points[x + 1][y][1], points[x + 1][y][2]);
			}
		}
		gl.glEnd();

		if (wiggle_count == 2)
		{
			for ( int y = 0; y < 45; y++)
			{
				hold = points[0][y][2];
				for ( int x = 0; x < 44; x++)
				{
					points[x][y][2] = points[x + 1][y][2];
				}
				points[44][y][2] = hold;
			}
			wiggle_count = 0;
		}

		wiggle_count++;
		
		glComposite.drawFPS();
	}

	
	/** 
	 * Return the string that is the description of this scene
	 */
	public String getDescription()
	{
		return "Texture mapped onto waving shape";
	}

	/** 
	 * Return the string that is the description of this scene
	 */
	public String getLabel()
	{
		return "Waving Texture";
	}
}

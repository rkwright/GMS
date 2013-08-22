/*******************************************************************************
 * 
 * Code from Lesson 6 at NeHe. www.nehe.com 
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
import com.sun.opengl.util.texture.TextureCoords;

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
 * @author Ric Wright
 */
public class TextureMappedCubeScene extends GLScene
{
	public static final String textureFile = "/resource/NeHe.png";
	
    Texture			texture;
    
   	/**
   	 * The Textue Mapped Cube constructor.  Like all the scene derived classes, it 
   	 * takes a Composite parent and an AWT Frame object.
   	 * @param parent
   	 * @param glFrame
   	 */
	public TextureMappedCubeScene( GLComposite parent )
	{
		super(parent);
	
		glComposite.getGrip().setOffsets( 0.0f, 0.0f, -5.0f);
		glComposite.getGrip().setRotation(45.0f, -30.0f, 0.0f);
	}

	/**
	 * a default constructor used by the ClassInfo enumerator
	 */ 
	public TextureMappedCubeScene()
	{
		super();
	}
	
	/**
	 * The scene's implementation of the init method.
	 */
	public void init(GLAutoDrawable drawable)
	{
		super.init(drawable);

		System.out.println("FlyingTextScene - init");

		final GL gl = drawable.getGL();

		LoadGLTextures(gl);
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
		texture = glComposite.createTexture(this, textureFile, GL.GL_NEAREST, false);
				
		return true;
	}		

	/**
	 * Here we actually draw the scene.
	 */
	public void display(GLAutoDrawable drawable) 
	{
		super.display(drawable);
	
		System.out.println("GLScene - display");
		GL gl = drawable.getGL();
		
        // Front Face
        renderFace( gl, texture, 0.0f, 0.0f, 0.0f, 0.0f );

        // Back Face
        renderFace( gl, texture, 180.0f, 0f, 1f, 0f );
 
        // Top Face
        renderFace( gl, texture, 270.0f, 1f, 0f, 0f );
        
        // Bottom Face
        renderFace( gl, texture, 90.0f, 1f, 0f, 0f );
        
       // Right face
        renderFace( gl, texture, 90.0f, 0f, 1f, 0f );
        
         // Left Face
        renderFace( gl, texture, 270.0f, 0f, 1f, 0f );
   	
	}

	/**
	 * Renders the given texture so that it is centered within the given
	 * dimensions.
	 */
	private void renderFace( GL 	 gl, 
							 Texture texture, 
							 float   rotation, 
							 float 	 xAxis, 
							 float   yAxis, 
							 float   zAxis )
	{
		gl.glPushMatrix();
		gl.glRotatef(rotation, xAxis, yAxis, zAxis);
		gl.glTranslatef(0.0f, 0.0f, 1.0f);

		TextureCoords tc = texture.getImageTexCoords();
		float tx1 = tc.left();
		float ty1 = tc.top();
		float tx2 = tc.right();
		float ty2 = tc.bottom();

		int imgw = texture.getImageWidth();
		int imgh = texture.getImageHeight();
		float 	h = 1.0f;
		float   w = 1.0f;
		if (imgw > imgh)
		{
			h = ((float) imgh) / imgw;
		}
		else
		{
			w = ((float) imgw) / imgh;
		}

		texture.enable();
		texture.bind();
		
		gl.glBegin(GL.GL_QUADS);
		
		gl.glTexCoord2f(tx1, ty1);
		gl.glVertex3f(-w, h, 0f);
		
		gl.glTexCoord2f(tx2, ty1);
		gl.glVertex3f(w, h, 0f);
		
		gl.glTexCoord2f(tx2, ty2);
		gl.glVertex3f(w, -h, 0f);
		
		gl.glTexCoord2f(tx1, ty2);
		gl.glVertex3f(-w, -h, 0f);
		
		gl.glEnd();
		
		texture.disable();
		gl.glPopMatrix();
	}

	/** 
	 * Return the string that is the description of this scene
	 */
	public String getDescription()
	{
		return "A cube with a Java2D texture mapped image on it";
	}

	/**
	 * Return the string that is the label for this string that
	 * will be shown in the "SelectDialog"
	 */
	public String getLabel()
	{
		return "Texture Mapped Cube";
	}
}

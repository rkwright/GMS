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
public class PhotoCubeScene extends GLScene
{
	public static final String textureFile = "/resource/NeHe.png";
    
	private Texture[] textures;
	private float size = 1.0f;
	private float alpha = 1.0f;
	
 	/**
   	 * The PhotoCube constructor.  Like all the scene derived classes, it 
   	 * takes a Composite parent and an AWT Frame object.
   	 * @param parent
   	 * @param glFrame
   	 */
	public PhotoCubeScene( GLComposite parent )
	{
		super(parent);
	
		glComposite.getGrip().setOffsets( 0.0f, 0.0f, -3.0f);
		glComposite.getGrip().setRotation(45.0f, -30.0f, 0.0f);
	}

	/**
	 * a default constructor used by the ClassInfo enumerator
	 */ 
	public PhotoCubeScene()
	{
		super();
	}

	/**
	 * The scene's implementation of the init method.
	 */	
	public void init ( GLAutoDrawable drawable ) 
	{
		super.init(drawable);	
		
		System.out.println("FlyingTextScene - init");

		final GL gl = drawable.getGL();	
		LoadGLTextures( gl );
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
		textures = new Texture[6];
	    
		textures[0] = glComposite.createTexture(this, "resource/rhinecliff.jpg", GL.GL_LINEAR, false);
		textures[1] = glComposite.createTexture(this, "resource/candice.jpg", GL.GL_LINEAR, false);
		textures[2] = glComposite.createTexture(this, "resource/relics.jpg", GL.GL_LINEAR, false);
		textures[3] = glComposite.createTexture(this, "resource/tmsk.jpg", GL.GL_LINEAR, false);
		textures[4] = glComposite.createTexture(this, "resource/caldera.jpg", GL.GL_LINEAR, false);
		textures[5] = glComposite.createTexture(this, "resource/mule.jpg", GL.GL_LINEAR, false);
		
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
		
		float size2 = size * 1.0f;  //0.75f;

		if (alpha <= 1.0f)
		{
			// enable blending, using the SrcOver rule
			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
		}

		// use the GL_MODULATE texture function to effectively multiply
		// each pixel in the texture by the current alpha value (this controls
		// the opacity of the cube)
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);

		// front
		gl.glPushMatrix();
		gl.glTranslatef(0f, 0f, size2);
		renderFace(gl, textures[0], size, size);
		gl.glPopMatrix();

		// left
		gl.glPushMatrix();
		gl.glRotatef(270.0f, 0f, 1f, 0f);
		gl.glTranslatef(0f, 0f, size2);
		renderFace(gl, textures[1], size, size);
		gl.glPopMatrix();

		// back
		gl.glPushMatrix();
		gl.glRotatef(180.0f, 0f, 1f, 0f);
		gl.glTranslatef(0f, 0f, size2);
		renderFace(gl, textures[2], size, size);
		gl.glPopMatrix();

		// right
		gl.glPushMatrix();
		gl.glRotatef(90.0f, 0f, 1f, 0f);
		gl.glTranslatef(0f, 0f, size2);
		renderFace(gl, textures[3], size, size);
		gl.glPopMatrix();

		// top
		gl.glPushMatrix();
		gl.glRotatef(270.0f, 1f, 0f, 0f);
		gl.glTranslatef(0f, 0f, size2);
		renderFace(gl, textures[4], size, size);
		gl.glPopMatrix();

		// bottom
		gl.glPushMatrix();
		gl.glRotatef(90.0f, 1f, 0f, 0f);
		gl.glTranslatef(0f, 0f, size2);
		renderFace(gl, textures[5], size, size);
		gl.glPopMatrix();

		if (alpha <= 1.0f)
		{
			gl.glDisable(GL.GL_BLEND);
		}
	}
	  
   /**
	 * Renders the given texture so that it is centered within the given
	 * dimensions.
	 */
	private void renderFace(GL gl, Texture t, float w, float h)
	{
		TextureCoords tc = t.getImageTexCoords();
		float tx1 = tc.left();
		float ty1 = tc.top();
		float tx2 = tc.right();
		float ty2 = tc.bottom();

		int imgw = t.getImageWidth();
		int imgh = t.getImageHeight();
		if (imgw > imgh)
		{
			h *= ((float) imgh) / imgw;
		}
		else
		{
			w *= ((float) imgw) / imgh;
		}
		float w2 = w / 2f;
		float h2 = h / 2f;

		t.enable();
		t.bind();
		gl.glColor4f(alpha, alpha, alpha, alpha);
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(tx1, ty1);
		gl.glVertex3f(-w2, h2, 0f);
		gl.glTexCoord2f(tx2, ty1);
		gl.glVertex3f(w2, h2, 0f);
		gl.glTexCoord2f(tx2, ty2);
		gl.glVertex3f(w2, -h2, 0f);
		gl.glTexCoord2f(tx1, ty2);
		gl.glVertex3f(-w2, -h2, 0f);
		gl.glEnd();
		t.disable();
	}
	
	/** 
	 * Return the string that is the description of this scene
	 */
	public String getDescription()
	{
		return "A photocube using Java2D texture mapped images";
	}

	/**
	 * Return the string that is the label for this string that
	 * will be shown in the "SelectDialog"
	 */
	public String getLabel()
	{
		return "Photo Cube";
	}
}

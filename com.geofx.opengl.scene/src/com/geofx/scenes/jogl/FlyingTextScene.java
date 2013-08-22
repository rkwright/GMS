/*******************************************************************************
 * 
 * 
 * Code from the JOGL Demos:  https://jogl-demos.dev.java.net/
 * 
 * Contributors:
 *    
 *     Ric Wright - May 2008 - Ported to Eclipse 3.2, minor tweaks
 *******************************************************************************/
package com.geofx.scenes.jogl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Point2f;

import com.geofx.opengl.scene.GLScene;
import com.geofx.opengl.view.GLComposite;
import com.sun.opengl.util.j2d.TextRenderer;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureIO;

public class FlyingTextScene extends GLScene
{
	// Put a little physics on the text to make it look nicer
	private static final float 	INIT_ANG_VEL_MAG = 0.3f;
	private static final float 	INIT_VEL_MAG = 200.0f;
	private static final int 	DEFAULT_DROP_SHADOW_DIST = 20;
	private static final int 	NUM_STRINGS = 50;

	private ArrayList<TextInfo> textInfo = new ArrayList<TextInfo>();

	private Texture 		backgroundTexture;
	private TextRenderer 	renderer;
	private Random 			random = new Random();

	private int 			dropShadowDistance = DEFAULT_DROP_SHADOW_DIST;
	private int 			width;
	private int 			height;
	private int 			maxTextWidth;

  	/**
   	 * The Flying Text constructor.  Like all the scene derived classes, it 
   	 * takes a Composite parent and an AWT Frame object.
   	 * @param parent
   	 * @param glFrame
   	 */	public FlyingTextScene( GLComposite parent )
	{
		super(parent);

		System.out.println("FlyingTextScene - constructor");

		glComposite.getGrip().setOffsets(0.0f, 0.0f, -3.0f);
		glComposite.getGrip().setRotation(45.0f, -30.0f, 0.0f);
	}

	/** 
	 * a default constructor used by the ClassInfo enumerator
	 */
	public FlyingTextScene()
	{
		super();
	}
	
	// Information about each piece of text
	private static class TextInfo
	{
		float 	angularVelocity;
		Point2f	velocity;
		Point2f	position;
		float 	angle;
		float 	h;
		float 	s;
		float 	v;
		float 	curTime;	// Cycle the saturation

		// Cache of the RGB color
		float 	r;
		float 	g;
		float 	b;

		String 	text;
	}

	/**
	 * The scene's implementation of the init method.
	 */	
	public void init ( GLAutoDrawable drawable ) 
	{
		super.init(drawable);	
		
		System.out.println("FlyingTextScene - init");

		final GL gl = drawable.getGL();	

		// Create the text renderer
		renderer = new TextRenderer(new Font("Palatino Linotype", Font.TRUETYPE_FONT, 72), true);

		// Create the background texture
		BufferedImage bgImage = new BufferedImage(2, 2, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = bgImage.createGraphics();
		g.setColor(new Color(0.3f, 0.3f, 0.3f));
		g.fillRect(0, 0, 2, 2);
		g.setColor(new Color(0.7f, 0.7f, 0.7f));
		g.fillRect(0, 0, 1, 1);
		g.fillRect(1, 1, 1, 1);
		g.dispose();
		backgroundTexture = TextureIO.newTexture(bgImage, false);
		backgroundTexture.bind();
		backgroundTexture.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		backgroundTexture.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		backgroundTexture.setTexParameteri(GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
		backgroundTexture.setTexParameteri(GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);

		width = glComposite.getCanvas().getWidth();
		height = glComposite.getCanvas().getHeight();

		// Compute maximum width of text we're going to draw to avoid
		// popping in/out at edges
		maxTextWidth = (int) renderer.getBounds("Java 2D").getWidth();
		maxTextWidth = Math.max(maxTextWidth, (int) renderer.getBounds("OpenGL").getWidth());

		// Create random text
		textInfo.clear();
		for (int i = 0; i < NUM_STRINGS; i++)
		{
			textInfo.add(randomTextInfo());
		}

		// Set up properties; note we don't need the depth buffer in this demo
		gl.glDisable(GL.GL_DEPTH_TEST);
		
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

		try
		{
			// Update velocities and positions of all text
			float deltaT = 0.1f; 
			Point2f		tmp = new Point2f();
			
		
			for (Iterator<TextInfo> iter = textInfo.iterator(); iter.hasNext();)
			{
				TextInfo info = (TextInfo) iter.next();

				// Randomize things a little bit at run time
				if (random.nextInt(1000) == 0)
				{
					info.angularVelocity = INIT_ANG_VEL_MAG * (randomAngle() - 180);
					info.velocity = randomVelocity(INIT_VEL_MAG, INIT_VEL_MAG);
				}

				// Now update angles and positions
				info.angle += info.angularVelocity * deltaT;
				tmp.set(info.velocity);
				tmp.scale(deltaT);
				info.position.add(tmp);

				// Update color
				info.curTime += deltaT;
				if (info.curTime > 2 * Math.PI)
				{
					info.curTime -= 2 * Math.PI;
				}
				int rgb = Color.HSBtoRGB(info.h, (float) (0.5 * (1 + Math.sin(info.curTime)) * info.s), info.v);
				info.r = ((rgb >> 16) & 0xFF) / 255.0f;
				info.g = ((rgb >> 8) & 0xFF) / 255.0f;
				info.b = (rgb & 0xFF) / 255.0f;

				// Wrap angles and positions
				if (info.angle < 0)
				{
					info.angle += 360;
				}
				else if (info.angle > 360)
				{
					info.angle -= 360;
				}
				// Use maxTextWidth to avoid popping in/out at edges
				// Would be better to do oriented bounding rectangle computation
				if (info.position.x < -maxTextWidth)
				{
					info.position.x = info.position.x + glComposite.getCanvas().getWidth() + 2 * maxTextWidth;
				}
				else if (info.position.x > glComposite.getCanvas().getWidth() + maxTextWidth)
				{
					info.position.x = info.position.x - glComposite.getCanvas().getWidth() - 2 * maxTextWidth;
				}
				if (info.position.y < -maxTextWidth)
				{
					info.position.y = info.position.y + glComposite.getCanvas().getHeight() + 2 * maxTextWidth;
				}
				else if (info.position.y > glComposite.getCanvas().getHeight() + maxTextWidth)
				{
					info.position.y = info.position.y - glComposite.getCanvas().getHeight() - 2 * maxTextWidth;
				}
			}

			gl.glClear(GL.GL_COLOR_BUFFER_BIT);
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glLoadIdentity();
			glComposite.getGLU().gluOrtho2D(0, glComposite.getCanvas().getWidth(), 0, glComposite.getCanvas().getHeight());
			gl.glMatrixMode(GL.GL_MODELVIEW);
			gl.glLoadIdentity();

			// Draw the background texture
			backgroundTexture.enable();
			backgroundTexture.bind();
			TextureCoords coords = backgroundTexture.getImageTexCoords();
			int w = glComposite.getCanvas().getWidth();
			int h = glComposite.getCanvas().getHeight();
			float fw = w / 100.0f;
			float fh = h / 100.0f;
			gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
			gl.glBegin(GL.GL_QUADS);
			gl.glTexCoord2f(fw * coords.left(), fh * coords.bottom());
			gl.glVertex3f(0, 0, 0);
			gl.glTexCoord2f(fw * coords.right(), fh * coords.bottom());
			gl.glVertex3f(w, 0, 0);
			gl.glTexCoord2f(fw * coords.right(), fh * coords.top());
			gl.glVertex3f(w, h, 0);
			gl.glTexCoord2f(fw * coords.left(), fh * coords.top());
			gl.glVertex3f(0, h, 0);
			gl.glEnd();
			backgroundTexture.disable();

			// Render all text
			renderer.beginRendering(w, h);

			// Note we're doing some slightly fancy stuff to position the text.
			// We tell the text renderer to render the text at the origin, and
			// manipulate the modelview matrix to put the text where we want.

			gl.glMatrixMode(GL.GL_MODELVIEW);

			// First render drop shadows
			renderer.setColor(0, 0, 0, 0.5f);
			for (Iterator<TextInfo> iter = textInfo.iterator(); iter.hasNext();)
			{
				TextInfo info = (TextInfo) iter.next();
				gl.glLoadIdentity();
				gl.glTranslatef(info.position.x + dropShadowDistance, info.position.y - dropShadowDistance, 0);
				gl.glRotatef(info.angle, 0, 0, 1);
				renderer.draw(info.text, 0, 0);
				renderer.flush();
			}

			// Now render the actual text
			for (Iterator<TextInfo> iter = textInfo.iterator(); iter.hasNext();)
			{
				TextInfo info = (TextInfo) iter.next();
				gl.glLoadIdentity();
				gl.glTranslatef(info.position.x, info.position.y, 0);
				gl.glRotatef(info.angle, 0, 0, 1);
				renderer.setColor(info.r, info.g, info.b, 1);
				renderer.draw(info.text, 0, 0);
				renderer.flush();
			}

			renderer.endRendering();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/** 
	 * Return the string that is the description of this scene
	 */
	public String getDescription()
	{
		return "A simple demo of Java2D text animation";
	}

	/**
	 * Return the string that is the label for this string that
	 * will be shown in the "SelectDialog"
	 */
	public String getLabel()
	{
		return "Flying Text";
	}

	//----------------------------------------------------------------------
	// Internals only below this point
	//

	private TextInfo randomTextInfo()
	{
		TextInfo info = new TextInfo();
		info.text = randomString();
		info.angle = randomAngle();
		info.position = randomVector(width, height);

		info.angularVelocity = INIT_ANG_VEL_MAG * (randomAngle() - 180);
		info.velocity = randomVelocity(INIT_VEL_MAG, INIT_VEL_MAG);

		Color c = randomColorX();
		float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
		info.h = hsb[0];
		info.s = hsb[1];
		info.v = hsb[2];
		info.curTime = (float) (2 * Math.PI * random.nextFloat());
		return info;
	}

	private String randomString()
	{
		switch (random.nextInt(3))
		{
		case 0:
			return "OpenGL";
		case 1:
			return "Java 2D";
		default:
			return "JOGL";
		}
	}

	private float randomAngle()
	{
		return 360.0f * random.nextFloat();
	}

	private Point2f randomVector(float x, float y)
	{
		return new Point2f(x * random.nextFloat(), y * random.nextFloat());
	}

	private Point2f randomVelocity(float x, float y)
	{
		return new Point2f(x * (random.nextFloat() - 0.5f), y * (random.nextFloat() - 0.5f));
	}

	private Color randomColorX()
	{
		// Get a bright and saturated color
		float r = 0;
		float g = 0;
		float b = 0;
		float s = 0;
		do
		{
			r = random.nextFloat();
			g = random.nextFloat();
			b = random.nextFloat();

			float[] hsb = Color.RGBtoHSB((int) (255.0f * r), (int) (255.0f * g), (int) (255.0f * b), null);
			s = hsb[1];
		} while ((r < 0.8f && g < 0.8f && b < 0.8f) || s < 0.8f);
		return new Color(r, g, b);
	}
}

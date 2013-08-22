/*******************************************************************************
 * 
 *******************************************************************************/
package com.geofx.scenes.misc;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.vecmath.Point3f;

import com.geofx.opengl.scene.GLScene;
import com.geofx.opengl.util.TextRenderer3D;
import com.geofx.opengl.view.GLComposite;

/**
 * 
 * 
 * @author Ric Wright
 */
public class BouncingTextScene extends GLScene
{
	public static final float AXIS_SIZE = 0.5f;

	TextRenderer3D		tr3;

	protected Random	random = new Random();

	public static final int NUM_ITEMS = 100;
	public static final int MAX_ITEMS = 200;
	private static int 	numItems = NUM_ITEMS;

	private ArrayList<TextInfo3D> textInfo = new ArrayList<TextInfo3D>();

	private GLU glu = new GLU();
	protected GLUquadric QUADRIC = glu.gluNewQuadric();

  	/**
   	 * The Rotation constructor.  Like all the scene derived classes, it 
   	 * takes a Composite parent and an AWT Frame object.
   	 * @param parent
   	 * @param glFrame
   	 */
	public BouncingTextScene( GLComposite parent )
	{
		super(parent);
	
		System.out.println("BouncingText - constructor");

		glComposite.getGrip().setOffsets(0.0f, 0.0f, -3.0f);
		glComposite.getGrip().setRotation(45.0f, -30.0f, 0.0f);
	}

	/**
	 * a default constructor used by the ClassInfo enumerator
	 */ 
	public BouncingTextScene()
	{
		super();
	}
	
	/** 
	 * Return the string that is the description of this scene
	 */
	public String getDescription()
	{
		return "A demonstration of 3D text animation";
	}

	/**
	 * Return the string that is the label for this string that
	 * will be shown in the "SelectDialog"
	 */
	public String getLabel()
	{
		return "Bouncing Text";
	}
	/**
	 * The scene's implementation of the init method.
	 */
	public void init ( GLAutoDrawable drawable ) 
	{
		System.out.println("XYZAxesScene - init");
		super.init(drawable);
		
		// Note that it has to be a TRUETYPE font - not OpenType.  Apparently, AWT can't
		// handle CFF glyphs
		tr3 = new TextRenderer3D(new Font("Times New Roman", Font.TRUETYPE_FONT, 3), 0.25f);   
		
		// Create random text
		textInfo.clear();
		for (int i = 0; i < numItems; i++)
		{
			textInfo.add(randomTextInfo());
		}

		glComposite.initAnimator(drawable, 100);

	}

	/**
	 * Here we actually draw the scene.
	 */
	public void display(GLAutoDrawable drawable) 
	{
		super.display(drawable);
	
		//System.out.println("BouncingTextScene - display");
		GL gl = drawable.getGL();

		try
		{
			gl.glEnable(GL.GL_LIGHTING);
			gl.glEnable(GL.GL_LIGHT0);

			glComposite.drawAxes(gl);
		
			for (Iterator<TextInfo3D> iter = textInfo.iterator(); iter.hasNext();)
			{
				TextInfo3D info = (TextInfo3D) iter.next();
				
				updateTextInfo( info );
					
				gl.glPushAttrib(GL.GL_TRANSFORM_BIT);
				gl.glMatrixMode(GL.GL_MODELVIEW);
				gl.glPushMatrix();
				gl.glEnable( GL.GL_NORMALIZE);
				
				gl.glTranslatef(info.position.x, info.position.y, info.position.z);
				gl.glRotatef(info.angle.x, 1, 0, 0);
				gl.glRotatef(info.angle.y, 0, 1, 0);
				gl.glRotatef(info.angle.z, 0, 0, 1);
				
				// System.out.println(" x,y,z: " + info.position.x + " " + info.position.y + " " + info.position.z + " angle: " + info.angle );
			
				gl.glColor4fv(info.material, 0);

				tr3.call(info.index);
				
				gl.glPopMatrix();
				gl.glPopAttrib();	
			}
			           
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void dispose()
	{
		System.out.println("BouncingTextScene - dispose");

		tr3.dispose();
	}
	
	//------------------ Private Stuff below here ------------------------------
	private static final float 	INIT_ANG_VEL_MAG = 0.3f;
	private static final float 	INIT_VEL_MAG = 0.25f;
	private static final float 	MAX_BOUNDS = 1.5f;
	private static final float  SCALE_FACTOR = 0.05f;

	// Information about each piece of text
	private static class TextInfo3D
	{
		Point3f	angularVelocity;
		Point3f	velocity;
		Point3f	position;
		Point3f	angle;
		int		index;		// display list index
		float[] material = new float[4];

		String 	text;
	}

	Point3f		tmp = new Point3f();
	
	private void updateTextInfo( TextInfo3D info )
	{
		// Update velocities and positions of all text
		float deltaT = 0.1f; 

		// Randomize things a little bit every little once in a while
		if (random.nextInt(10000) == 0)
		{
			info.angularVelocity = randomRotation(INIT_ANG_VEL_MAG, INIT_ANG_VEL_MAG, INIT_ANG_VEL_MAG);
			info.velocity = randomVelocity(INIT_VEL_MAG, INIT_VEL_MAG, INIT_VEL_MAG);
		}

		// Now update angles and positions
		tmp.set(info.angularVelocity);
		tmp.scale(deltaT*deltaT);
		info.angle.add(tmp);
		
		tmp.set(info.velocity);
		tmp.scale(deltaT);
		info.position.add(tmp);

		// Wrap angles and positions
		info.angle.x = clampAngle(info.angle.x);
		info.angle.y = clampAngle(info.angle.y);
		info.angle.z = clampAngle(info.angle.z);
	
		info.velocity.x = clampBounds(info.position.x, info.velocity.x );
		info.velocity.y = clampBounds(info.position.y, info.velocity.y );
		info.velocity.z = clampBounds(info.position.z, info.velocity.z );
	}

	private float clampBounds( float pos, float velocity )
	{
		if (pos < -MAX_BOUNDS || pos > MAX_BOUNDS)
		{
			velocity *= -1.0f;
		}
		
		return velocity;
	}
	
	private float clampAngle(float angle)
	{
		if (angle < 0)
		{
			angle += 360;
		}
		else if (angle > 360)
		{
			angle -= 360;
		}
		
		return angle;
	}
	
	private TextInfo3D randomTextInfo()
	{
		TextInfo3D info = new TextInfo3D();
		info.text = randomString();
		info.angle = randomRotation(INIT_ANG_VEL_MAG, INIT_ANG_VEL_MAG, INIT_ANG_VEL_MAG);
		info.position = randomVector(MAX_BOUNDS, MAX_BOUNDS, MAX_BOUNDS);
		
		
		Rectangle2D rect = tr3.getBounds(info.text, SCALE_FACTOR);

		float offX = (float) rect.getCenterX();
		float offY = (float) rect.getCenterY();
		float offZ = (float) (tr3.getDepth() / 2.0f);

		tr3.setDepth(0.1f + random.nextFloat() * 2.0f);
		info.index = tr3.compile(info.text, -offX, offY, -offZ, SCALE_FACTOR);

		info.angularVelocity = randomRotation(INIT_ANG_VEL_MAG, INIT_ANG_VEL_MAG, INIT_ANG_VEL_MAG);
		info.velocity = randomVelocity(INIT_VEL_MAG, INIT_VEL_MAG, INIT_VEL_MAG);

		Color c = randomColor();
		c.getColorComponents(info.material);
		// Color doesn't set the opacity,so set it to some random non-zero value
		info.material[3] = random.nextFloat() * 0.9f + 0.1f;
	
		return info;
	}

	private String randomString()
	{
		switch (random.nextInt(3))
		{
		case 0:
			return "OpenGL";
		case 1:
			return "Java3D";
		default:
			return "JOGL";
		}
	}

	private Point3f randomVector(float x, float y, float z)
	{
		return new Point3f(x * random.nextFloat(), y * random.nextFloat(), z * random.nextFloat());
	}

	private Point3f randomVelocity(float x, float y, float z)
	{
		return new Point3f(x * (random.nextFloat() - 0.5f), y * (random.nextFloat() - 0.5f), z * (random.nextFloat() - 0.5f));
	}

	private Point3f randomRotation(float x, float y, float z)
	{
		return new Point3f(random.nextFloat() * 360.0f, random.nextFloat() * 360.0f, random.nextFloat() * 360.0f);
	}
	
	private Color randomColor()
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
		} 
		while ((r < 0.6f && g < 0.6f && b < 0.6f) || s < 0.8f);
		
		return new Color(r, g, b);
	}
}

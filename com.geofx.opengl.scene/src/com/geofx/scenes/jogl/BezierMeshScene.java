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

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.geofx.opengl.scene.GLScene;
import com.geofx.opengl.view.GLComposite;
import com.sun.opengl.util.BufferUtil;

public class BezierMeshScene extends GLScene
{
	private FloatBuffer texptsBuf;

	private FloatBuffer ctrlpointsBuf;

	private static final int imageWidth = 64;

	private static final int imageHeight = 64;

	private static byte image[] = new byte[3 * imageWidth * imageHeight];

	private static ByteBuffer imageBuf = BufferUtil.newByteBuffer(image.length);

	float ambient[] = { 0.2f, 0.2f, 0.2f, 1.0f };

	float position[] = { 0.0f, 0.0f, 2.0f, 1.0f };

	float mat_diffuse[] = { 0.6f, 0.6f, 0.6f, 1.0f };

	float mat_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };

	float mat_shininess[] = { 50.0f };


   	/**
   	 * The Bezier Mesh constructor.  Like all the scene derived classes, it 
   	 * takes a Composite parent and an AWT Frame object.
   	 * @param parent
   	 * @param glFrame
   	 */
	public BezierMeshScene( GLComposite parent )
	{
		super(parent);

		buildFloatBuffers();
		
		System.out.println("BezierMeshScene - constructor");

		glComposite.getGrip().setOffsets(0.0f, 0.0f, -3.0f);
		glComposite.getGrip().setRotation(45.0f, -30.0f, 0.0f);
	}

	/**
	 * a default constructor used by the ClassInfo enumerator
	 */ 
	public BezierMeshScene()
	{
		super();
	}

	// Information about each piece of text	
	private void buildFloatBuffers()
	{
		float[][][] ctrlpoints =
	   	{
	   	    {
	   	        {-1.5f, -1.5f, 4.0f},
	   	        {-0.5f, -1.5f, 2.0f},
	   	        {0.5f, -1.5f, -1.0f},
	   	        {1.5f, -1.5f, 2.0f}
	   	    },
	   	    {
	   	        {-1.5f, -0.5f, 1.0f},
	   	        {-0.5f, -0.5f, 3.0f},
	   	        {0.5f, -0.5f, 0.0f},
	   	        {1.5f, -0.5f, -1.0f}
	   	    },
	   	    {
	   	        {-1.5f, 0.5f, 4.0f},
	   	        {-0.5f, 0.5f, 0.0f},
	   	        {0.5f, 0.5f, 3.0f},
	   	        {1.5f, 0.5f, 4.0f}
	   	    },
	   	    {
	   	        {-1.5f, 1.5f, -2.0f},
	   	        {-0.5f, 1.5f, -2.0f},
	   	        {0.5f, 1.5f, 0.0f},
	   	        { 1.5f, 1.5f, -1.0f }
	   	    }
	   	};

	   	float[][][] texpts =
	   	{ 
	   		{ 
	   			{ 0.0f, 0.0f }, 
	   			{ 0.0f, 1.0f } 
	   		},
	   		{ 
	   			{ 1.0f, 0.0f }, 
	   			{ 1.0f, 1.0f } 
	   		} 
	   	};
	   	      	  
       	texptsBuf = createFloatBuffer(texpts);
       	
       	ctrlpointsBuf = createFloatBuffer(ctrlpoints);
       	
	}

	/**
	 * Recursive routine to get the size of a rectangular array.
	 * Could be expanded to support ragged arrays, but not now
	 */
	protected int getArraySize ( Object  vec, int len )
	{
		if (vec.getClass().isArray())
		{
			if (len == 0)
				len = 1;
			
			len *= Array.getLength(vec);
			len = getArraySize( Array.get(vec, 0), len );
		}
		
		return len;
	}
	
	/**
	 * Recursively copy the rows from a multidimensional array
	 */
	protected void copyRow ( Object  vec, FloatBuffer buf )
	{	
		int k = 0;
		while (vec.getClass().isArray() && k < Array.getLength(vec))
		{
			Object row = Array.get(vec, k);
			if (row.getClass().isArray())
				copyRow( row, buf );
			else
			{		
				for (int i=0; i<Array.getLength(vec); i++  )
				{
					buf.put(Array.getFloat(vec, i));
				}
				
				return;
			}
			k++;
		}
	}
	
	/**
	 * Create a float buffer from a multidimensional float array
	 * using reflection
	 */
	protected FloatBuffer createFloatBuffer ( Object  obj )
	{
		int len = getArraySize( obj, 0 );
		
		if (len == 0)
			return null;
	
		FloatBuffer buf = BufferUtil.newFloatBuffer(len);
		
		copyRow(obj, buf);

		buf.rewind();
		
		return buf;
	}

	/**
	 * The scene's implementation of the init method.
	 */
	public void init( GLAutoDrawable drawable ) 
	{
		super.init(drawable);	
		
		System.out.println("BezierMesh - init");

		final GL gl = drawable.getGL();	

		gl.glMap2f(GL.GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 4, ctrlpointsBuf);
	    gl.glMap2f(GL.GL_MAP2_TEXTURE_COORD_2, 0, 1, 2, 2, 0, 1, 4, 2, texptsBuf);
	    gl.glEnable(GL.GL_MAP2_TEXTURE_COORD_2);
	    gl.glEnable(GL.GL_MAP2_VERTEX_3);
	    gl.glMapGrid2f(20, 0.0f, 1.0f, 20, 0.0f, 1.0f);
	    
	    makeImage();
	    
	    gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_DECAL);
	    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
	    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
	    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
	    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
	    gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, imageWidth, imageHeight, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, imageBuf);

	    gl.glEnable(GL.GL_TEXTURE_2D);
	    gl.glEnable(GL.GL_DEPTH_TEST);
	    gl.glShadeModel(GL.GL_FLAT);
	}
	
	/**
	 * Initialize the lights for this scene...
	 * @param gl
	 */
	public void initLights( GL gl )
	{
		gl.glEnable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_LIGHT0);

		gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, ambient, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, position, 0);

		gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, mat_diffuse, 0);
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, mat_specular, 0);
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SHININESS, mat_shininess, 0);
	}

	/**
	 * Somewhat funky routine to create the buffer that contains
	 * the "wave" itself.
	 * TODO:  Can this be simpler?
	 */
	private void makeImage()
	{
		float ti, tj;

		for (int i = 0; i < imageWidth; i++)
		{
			ti = 2.0f * (float) Math.PI * i / imageWidth;
			for (int j = 0; j < imageHeight; j++)
			{
				tj = 2.0f * (float) Math.PI * j / imageHeight;

				// image[3 * (imageHeight * i + j)] = (byte) (255 * (1.0 +
				// sin(ti)));
				// image[3 * (imageHeight * i + j) + 1] = (byte) (255 * (1.0 +
				// cos(2 *
				// tj)));
				// image[3 * (imageHeight * i + j) + 2] = (byte) (255 * (1.0 +
				// cos(ti +
				// tj)));
				// image[3 * (imageHeight * i + j) + 2] = (byte)0xff;
				imageBuf.put((byte) (127 * (1.0 + Math.sin(ti))));
				imageBuf.put((byte) (127 * (1.0 + Math.cos(2 * tj))));
				imageBuf.put((byte) (127 * (1.0 + Math.cos(ti + tj))));
			}
		}
		
		imageBuf.rewind();
	}
	
	/**
	 * This class' method to handle the reshape event.  Not sure it needs 
	 * to exist
	 */
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		System.out.println("BezierMesh - reshape");
		
		GL gl = drawable.getGL();

		gl.glViewport(0, 0, w, h);
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		
		if (w <= h)
			gl.glOrtho(-4.0, 4.0, -4.0 * (float) h / (float) w, 4.0 * (float) h / (float) w, -4.0, 4.0);
		else
			gl.glOrtho(-4.0 * (float) w / (float) h, 4.0 * (float) w / (float) h, -4.0, 4.0, -4.0, 4.0);
		
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glRotatef(85.0f, 1.0f, 1.0f, 1.0f);
	}
	 
	/**
	 * Here we actually draw the scene.
	 */
	public void display(GLAutoDrawable drawable) 
	{
		super.display(drawable);
	
		System.out.println("BezierMeshScene - display");
		GL gl = drawable.getGL();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		gl.glEvalMesh2(GL.GL_FILL, 0, 20, 0, 20);
		gl.glFlush();
	}

	/** 
	 * Return the string that is the description of this scene
	 */
	public String getDescription()
	{
		return "A simple demo of evalauating Bezier surfaces";
	}

	/** 
	 * Return the string that is the description of this scene
	 */
	public String getLabel()
	{
		return "Bezier Mesh Surface";
	}

	// ----------------------------------------------------------------------
	
}

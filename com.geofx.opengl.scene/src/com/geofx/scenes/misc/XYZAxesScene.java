/*******************************************************************************
 * 
 *******************************************************************************/
package com.geofx.scenes.misc;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.geofx.opengl.scene.GLScene;
import com.geofx.opengl.view.GLComposite;

/**
 * Draws a picture I needed for the article.
 * 
 * @author Bo Majewski
 */
public class XYZAxesScene extends GLScene
{
	public static final float AXIS_SIZE = 0.5f;

  	/**
   	 * The Rotation constructor.  Like all the scene derived classes, it 
   	 * takes a Composite parent and an AWT Frame object.
   	 * @param parent
   	 * @param glFrame
   	 */
	public XYZAxesScene( GLComposite parent )
	{
		super(parent);
	
		System.out.println("XYZAxesScene - constructor");

		glComposite.getGrip().setOffsets(0.0f, 0.0f, -3.0f);
		glComposite.getGrip().setRotation(45.0f, -30.0f, 0.0f);
	}

	/**
	 * a default constructor used by the ClassInfo enumerator
	 */ 
	public XYZAxesScene()
	{
		super();
	}
	
	/**
	 * The scene's implementation of the init method.
	 */
	public void init ( GLAutoDrawable drawable ) 
	{
		System.out.println("XYZAxesScene - init");
		super.init(drawable);	
	}

	/**
	 * Here we actually draw the scene.
	 */
	public void display(GLAutoDrawable drawable) 
	{
		super.display(drawable);
	
		System.out.println("XYZAxesScene - display");
		GL gl = drawable.getGL();

		try
		{
			glComposite.updateLighting(gl);
		
			glComposite.drawAxes(gl);        
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
		return "A simple set of XYZ axes to demonstrate the UI";
	}

	/**
	 * Return the string that is the label for this string that
	 * will be shown in the "SelectDialog"
	 */
	public String getLabel()
	{
		return "XYZ Axes";
	}
}

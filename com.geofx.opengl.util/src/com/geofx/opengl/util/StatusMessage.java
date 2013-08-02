

package com.geofx.opengl.util;

import java.awt.Font;
import java.awt.geom.Rectangle2D;

import javax.media.opengl.GLDrawable;
import javax.media.opengl.GLException;

import com.sun.opengl.util.j2d.TextRenderer;

/** A simple class which uses the TextRenderer to provide a status
 *  message overlaid on the scene
 */

public class StatusMessage
{
	// Placement constants
	public static final int UPPER_LEFT = 1;

	public static final int UPPER_RIGHT = 2;

	public static final int LOWER_LEFT = 3;

	public static final int LOWER_RIGHT = 4;

	private int textLocation = LOWER_LEFT;

	private GLDrawable drawable;

	private TextRenderer renderer;

	private String message;

	private int msgWidth;

	private int msgHeight;

	private int msgOffset;

	/** 
	 * Creates a new FPSCounter with the given font size. An OpenGL
	 * context must be current at the time the constructor is called.
	 */
	public StatusMessage(GLDrawable drawable, int textSize) throws GLException
	{
		this(drawable, new Font("SansSerif", Font.BOLD, textSize));
	}

	/** 
	 * Creates a new FPSCounter with the given font. An OpenGL context
	 * must be current at the time the constructor is called.
	 */
	public StatusMessage(GLDrawable drawable, Font font ) throws GLException
	{
		this(drawable, font, true, true);
	}

	/** 
	 * Creates a new FPSCounter with the given font and rendering
	 * attributes. An OpenGL context must be current at the time the
	 * constructor is called.
	 */
	public StatusMessage(GLDrawable drawable, Font font, boolean antialiased, boolean useFractionalMetrics)
			throws GLException
	{
		this.drawable = drawable;
		renderer = new TextRenderer(font, antialiased, useFractionalMetrics);
	}

	/** 
	 * Gets the relative location where the message of this FPSCounter
	 * will be drawn: one of UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, or
	 * LOWER_RIGHT. Defaults to LOWER_RIGHT. */
	public int getTextLocation()
	{
		return textLocation;
	}

	/** 
	 * Sets the relative location where the message of this FPSCounter
	 * will be drawn: one of UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, or
	 * LOWER_RIGHT. Defaults to LOWER_RIGHT. */
	public void setTextLocation(int textLocation)
	{
		if (textLocation < UPPER_LEFT || textLocation > LOWER_RIGHT)
		{
			throw new IllegalArgumentException("textLocation");
		}
		this.textLocation = textLocation;
	}

	/** 
	 * Changes the current color of this TextRenderer to the supplied
	 * one, where each component ranges from 0.0f - 1.0f. 
	 */
	public void setColor(float r, float g, float b, float a) throws GLException
	{
		renderer.setColor(r, g, b, a);
	}

	/** 
	 * Updates the message
	 */
	public void draw()
	{


		if (message != null)
		{
			renderer.beginRendering(drawable.getWidth(), drawable.getHeight());
			// Figure out the location at which to draw the message
			int x = 0;
			int y = 0;
			switch (textLocation)
			{
				case UPPER_LEFT:
					x = msgOffset;
					y = drawable.getHeight() - msgHeight - msgOffset;
					break;

				case UPPER_RIGHT:
					x = drawable.getWidth() - msgWidth - msgOffset;
					y = drawable.getHeight() - msgHeight - msgOffset;
					break;

				case LOWER_LEFT:
					x = msgOffset;
					y = msgOffset;
					break;

				case LOWER_RIGHT:
					x = drawable.getWidth() - msgWidth - msgOffset;
					y = msgOffset;
					break;
			}

			renderer.draw(message, x, y);
			renderer.endRendering();
		}
	}

	private void recomputeSize( String message )
	{
		Rectangle2D bounds = renderer.getBounds(message);
		msgWidth = (int) bounds.getWidth();
		msgHeight = (int) bounds.getHeight();
		msgOffset = (int) (msgHeight * 0.5f);
	
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
		recomputeSize(message);
	}
}

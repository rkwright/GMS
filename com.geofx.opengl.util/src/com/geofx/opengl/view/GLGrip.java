/*******************************************************************************
 * Copyright (c) 2005 Bo Majewski 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 *
 * Code from an article by Bo Majewski
 * http://www.eclipse.org/articles/Article-SWT-OpenGL/opengl.html
 *  
 * Contributors:
 *     Bo Majewski - initial API and implementation
 *     Ric Wright  - Ported to Eclipse 3.2 and native SWT OpenGL support
 *                   Fixed some minor bugs.
 *                 - Added support for rotation in Z-plane and incremntal update
 *                   on mouse drag
 *     Ric Wright - May 2008 - Ported to JOGL
 *******************************************************************************/
package com.geofx.opengl.view;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.media.opengl.GL;

import org.eclipse.swt.SWT;



/**
 * Implements a scene grip, capable of rotating and moving a GL scene with the
 * help of the mouse and keyboard.
 * 
 * @author Bo Majewski
 */
public class GLGrip implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener 
{
	private static final int 	MIN_MOUSE_MOVE	= 10;
	private static final float	ROT_INCR = 1.0f;
	private static final float	SHIFT_INCR = 0.1f;
	
	private GLComposite		glComposite;
	private float 			xrot;
	private float 			yrot;
	private float 			zrot;
	private float 			zoff;
	private float 			xoff;
	private float 			yoff;
	private float 			xcpy;
	private float 			ycpy;
	private boolean 		move;
	private int 			xdown;
	private int 			ydown;
	private int 			mouseDown;
	private boolean 		wasAnimating = false;

	public GLGrip(GLComposite scene)
	{
		this.glComposite = scene;

		scene.getCanvas().addKeyListener(this);
		scene.getCanvas().addMouseListener(this);
		scene.getCanvas().addMouseMotionListener(this);
		scene.getCanvas().addMouseWheelListener(this);

		this.init();
	}

	protected void init()
	{
		this.xrot = this.yrot = this.zrot = 0.0f;
		this.xoff = this.yoff = 0.0f;
		this.zoff = -5.0f;
	}

	public void dispose( GL gl )
	{
		//System.out.println("SceneGrip dispose");
		glComposite.getCanvas().removeMouseListener(this);
		glComposite.getCanvas().removeMouseMotionListener(this);
		glComposite.getCanvas().removeMouseWheelListener(this);
		glComposite.getCanvas().removeKeyListener(this);
	}

	public void keyPressed( KeyEvent e)
	{
		//System.out.println("SceneGrip.Key event: " + e + " keycode: " + Integer.toHexString(e.getKeyCode()) + " : " + keyCode(e.getKeyCode()) 
		//		+ " state: " + Integer.toHexString(e.getModifiers()));
		
		boolean bRender = true;
		
		if (glComposite.handleKeyEvent(e))
		{
			glComposite.render();
			return;
		}
		
		switch (e.getKeyCode())
		{
		case KeyEvent.VK_UP:
			if (e.isControlDown())
			{
				this.yrot -= ROT_INCR;
			}
			else
			{
				this.yoff += SHIFT_INCR;
			}
			break;
		case KeyEvent.VK_DOWN:
			if (e.isControlDown())
			{
				this.yrot += ROT_INCR;
			}
			else
			{
				this.yoff -= SHIFT_INCR;
			}
			break;
		case KeyEvent.VK_LEFT:
			if (e.isControlDown())
			{
				this.xrot -= ROT_INCR;
			}
			else
			{
				this.xoff -= SHIFT_INCR;
			}
			break;
		case KeyEvent.VK_RIGHT:
			if (e.isControlDown())
			{
				this.xrot += ROT_INCR;
			}
			else
			{
				this.xoff += SHIFT_INCR;
			}
			break;
		case KeyEvent.VK_PAGE_UP:
			if (e.isControlDown())
			{
				this.zrot -= ROT_INCR;
			}
			else
			{
				this.zoff += SHIFT_INCR;
			}
			break;
		case KeyEvent.VK_PAGE_DOWN:
			if (e.isControlDown())
			{
				this.zrot += ROT_INCR;
			}
			else
			{
				this.zoff -= SHIFT_INCR;
			}
			break;
		case KeyEvent.VK_HOME:
			this.init();
			break;
		default:
			{
				switch (e.getKeyChar())
				{
				case 'a':
					if (this.glComposite.animator != null)
					{
						if (this.glComposite.animator.isAnimating())
							this.glComposite.animator.stop();
						else
							this.glComposite.animator.start();
					}
					break;
	
				case 'b':
					this.glComposite.toggleBlending();
					break;

				case 'f':
					this.glComposite.toggleFilter();
					break;
	
				case 'l':
					this.glComposite.toggleLighting();
					break;
	
				default:
					bRender = false;
					return;
				}
			}
		}
		
		if (bRender)
		{
			glComposite.render();
		}
		
	}

	public void keyReleased(java.awt.event.KeyEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	public void keyTyped(java.awt.event.KeyEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	public void mouseClicked(java.awt.event.MouseEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	public void mouseEntered(java.awt.event.MouseEvent e)
	{
		//System.out.println("mouse entered" + e);
		
	}

	public void mouseExited(java.awt.event.MouseEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(java.awt.event.MouseEvent e)
	{
		// if we're animating, stop while we drag
		if (glComposite.animator != null && glComposite.animator.isAnimating())
		{
			wasAnimating  = true;
			glComposite.animator.stop();
		}
		
		//System.out.println("start of pressed - mousedown " + this.mouseDown);
				
		if (++this.mouseDown > 0)
		{	
			this.mouseDown = 1;
			if ((this.move = e.getButton() == java.awt.event.MouseEvent.BUTTON1)) 
			{
				this.xcpy = xoff;
				this.ycpy = yoff;
				glComposite.getCanvas().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.MOVE_CURSOR));
			}
			else
			{
				this.xcpy = xrot;
				this.ycpy = yrot;
				glComposite.getCanvas().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
			}

			this.xdown = e.getX();
			this.ydown = e.getY();
		}
	
		//System.out.println("pressed - mousedown " + this.mouseDown);
	}

	public void mouseReleased(java.awt.event.MouseEvent e)
	{
		//System.out.println("released - mousedown " + this.mouseDown );
		if (--this.mouseDown < 1)
		{
			this.mouseDown = 0;
			glComposite.getCanvas().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			glComposite.render();
		}
		
		if (wasAnimating)
		{
			wasAnimating  = false;
			glComposite.animator.start();
		}

	}

	public void mouseDragged(java.awt.event.MouseEvent e)
	{
		//System.out.println("dragged - mousedown " + this.mouseDown + " x,y: " + e.getX() + " " + e.getY() );
		
		Dimension p = glComposite.getCanvas().getSize();

		// System.out.println("moved - mousedown " + this.mouseDown);
		if (this.mouseDown > 0)
		{
			int dx = e.getX() - this.xdown;
			int dy = e.getY() - this.ydown;

			if (this.move)
			{
				yoff = this.ycpy + ((zoff + 1.0f) * dy) / (2.0f * (float)p.getHeight());
				xoff = this.xcpy - ((zoff + 1.0f) * dx) / (2.0f * (float)p.getWidth());
			}
			else
			{
				xrot = this.xcpy + dy / 2.0f;
				yrot = this.ycpy + dx / 2.0f;
			}
			
			if (Math.abs(dx) > MIN_MOUSE_MOVE || Math.abs(dy) > MIN_MOUSE_MOVE)
			{
				glComposite.render();
			}
		}
	}

	public void mouseMoved(java.awt.event.MouseEvent e) {}

	public void mouseWheelMoved(MouseWheelEvent arg0) {}

	//--------- geometry adjustments --------------------------------
	public void adjust( GL gl )
	{
		gl.glTranslatef(this.xoff, this.yoff, this.zoff);
		gl.glRotatef(this.xrot, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(this.yrot, 0.0f, 1.0f, 0.0f);
		gl.glRotatef(this.zrot, 0.0f, 0.0f, 1.0f);
	}

	public void setOffsets(float x, float y, float z)
	{
		this.xoff = x;
		this.yoff = y;
		this.zoff = z;
	}

	public void setRotation(float x, float y, float z)
	{
		this.xrot = x;
		this.yrot = y;
		this.zrot = z;
	}

	public float getXRot()
	{
		return xrot;
	}

	public void setXRot(float xrot)
	{
		this.xrot = xrot;
	}

	public void adjustXRot(float dxrot)
	{
		this.xrot += dxrot;
	}

	public float getYRot()
	{
		return yrot;
	}

	public void setYRot(float yrot)
	{
		this.yrot = yrot;
	}

	public void adjustYRot(float dyrot)
	{
		this.yrot += dyrot;
	}

	public float getZRot()
	{
		return zrot;
	}

	public void setZRot(float zrot)
	{
		this.zrot = zrot;
	}

	public void adjustZRot(float dzrot)
	{
		this.zrot += dzrot;
	}

	public float getZOff()
	{
		return zoff;
	}

	public void setZOff(float zoff)
	{
		this.zoff = zoff;
	}

	public float getXOff()
	{
		return xoff;
	}

	public void setXOff(float xoff)
	{
		this.xoff = xoff;
	}

	public float getYOff()
	{
		return yoff;
	}

	public void setYOff(float yoff)
	{
		this.yoff = yoff;
	}

	static String keyCode(int keyCode)
	{
		switch (keyCode)
		{

			/* Keyboard and Mouse Masks */
			case SWT.ALT:
				return "ALT";
			case SWT.SHIFT:
				return "SHIFT";
			case SWT.CONTROL:
				return "CONTROL";
			case SWT.COMMAND:
				return "COMMAND";

				/* Non-Numeric Keypad Keys */
			case SWT.ARROW_UP:
				return "ARROW_UP";
			case SWT.ARROW_DOWN:
				return "ARROW_DOWN";
			case SWT.ARROW_LEFT:
				return "ARROW_LEFT";
			case SWT.ARROW_RIGHT:
				return "ARROW_RIGHT";
			case SWT.PAGE_UP:
				return "PAGE_UP";
			case SWT.PAGE_DOWN:
				return "PAGE_DOWN";
			case SWT.HOME:
				return "HOME";
			case SWT.END:
				return "END";
			case SWT.INSERT:
				return "INSERT";

				/* Virtual and Ascii Keys */
			case SWT.BS:
				return "BS";
			case SWT.CR:
				return "CR";
			case SWT.DEL:
				return "DEL";
			case SWT.ESC:
				return "ESC";
			case SWT.LF:
				return "LF";
			case SWT.TAB:
				return "TAB";

				/* Functions Keys */
			case SWT.F1:
				return "F1";
			case SWT.F2:
				return "F2";
			case SWT.F3:
				return "F3";
			case SWT.F4:
				return "F4";
			case SWT.F5:
				return "F5";
			case SWT.F6:
				return "F6";
			case SWT.F7:
				return "F7";
			case SWT.F8:
				return "F8";
			case SWT.F9:
				return "F9";
			case SWT.F10:
				return "F10";
			case SWT.F11:
				return "F11";
			case SWT.F12:
				return "F12";
			case SWT.F13:
				return "F13";
			case SWT.F14:
				return "F14";
			case SWT.F15:
				return "F15";

				/* Numeric Keypad Keys */
			case SWT.KEYPAD_ADD:
				return "KEYPAD_ADD";
			case SWT.KEYPAD_SUBTRACT:
				return "KEYPAD_SUBTRACT";
			case SWT.KEYPAD_MULTIPLY:
				return "KEYPAD_MULTIPLY";
			case SWT.KEYPAD_DIVIDE:
				return "KEYPAD_DIVIDE";
			case SWT.KEYPAD_DECIMAL:
				return "KEYPAD_DECIMAL";
			case SWT.KEYPAD_CR:
				return "KEYPAD_CR";
			case SWT.KEYPAD_0:
				return "KEYPAD_0";
			case SWT.KEYPAD_1:
				return "KEYPAD_1";
			case SWT.KEYPAD_2:
				return "KEYPAD_2";
			case SWT.KEYPAD_3:
				return "KEYPAD_3";
			case SWT.KEYPAD_4:
				return "KEYPAD_4";
			case SWT.KEYPAD_5:
				return "KEYPAD_5";
			case SWT.KEYPAD_6:
				return "KEYPAD_6";
			case SWT.KEYPAD_7:
				return "KEYPAD_7";
			case SWT.KEYPAD_8:
				return "KEYPAD_8";
			case SWT.KEYPAD_9:
				return "KEYPAD_9";
			case SWT.KEYPAD_EQUAL:
				return "KEYPAD_EQUAL";

				/* Other keys */
			case SWT.CAPS_LOCK:
				return "CAPS_LOCK";
			case SWT.NUM_LOCK:
				return "NUM_LOCK";
			case SWT.SCROLL_LOCK:
				return "SCROLL_LOCK";
			case SWT.PAUSE:
				return "PAUSE";
			case SWT.BREAK:
				return "BREAK";
			case SWT.PRINT_SCREEN:
				return "PRINT_SCREEN";
			case SWT.HELP:
				return "HELP";
		}
		return character((char) keyCode);
	}

	static String character(char character)
	{
		switch (character)
		{
			case 0:
				return "'\\0'";
			case SWT.BS:
				return "'\\b'";
			case SWT.CR:
				return "'\\r'";
			case SWT.DEL:
				return "DEL";
			case SWT.ESC:
				return "ESC";
			case SWT.LF:
				return "'\\n'";
			case SWT.TAB:
				return "'\\t'";
		}
		return "'" + character + "'";
	}
}

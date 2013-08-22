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
 *     				 Added support for double-buffering, better disposition
 *                   of resources
 *     Ric Wright - May 2008 - ported to JOGL
 *******************************************************************************/
package com.geofx.opengl.scene;

import java.awt.event.KeyEvent;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import com.geofx.opengl.view.GLComposite;

/**
 * Creates an OpenGL scene. In order to draw on it, override the
 * <code>drawScene</code> method.
 * 
 * @author Bo Majewski and Ric Wright
 */
public abstract class GLScene implements GLEventListener
{
	protected GLComposite 		glComposite;

	/**
	 * Creates a new scene owned by the specified parent component.
	 * 
	 * @param parent   The Composite which holds the actual GLCanvas
	 */
	public GLScene( GLComposite composite )
	{
		System.out.println("GLScene - constructor");

		glComposite = composite;  //new GLComposite(parent);
		
		glComposite.initComposite();
		
		// replace the default event listener - we'll do it ourselves.
		glComposite.setGLEventListener(this);
	}

	// a default constructor used by the ClassInfo enumerator
	public GLScene()
	{		
	}

	public abstract String getLabel();
	
	public abstract String getDescription();
	
	// -------------------------------------------------------------------------
	// GLEventListener implementations
	// -------------------------------------------------------------------------
	
	public void init( GLAutoDrawable drawable ) 
	{	
		System.out.println("GLScene - init");

		glComposite.init(drawable);              
	}

	public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) 
	{
		glComposite.reshape(drawable, x, y, width, height);
	}
	
	public void display(GLAutoDrawable drawable) 
	{
		glComposite.display(drawable);	
	}
	
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) 
	{
		System.out.println("GLScene - displayChanged");
	}

	public void render()
	{
		glComposite.redraw();
	}

	public void dispose()
	{
		glComposite.dispose();
	}
	
	// -------------------------------------------------------------------------
	// KeyEventListener implementations
	// allows subclasses to get key events and do something interesting...
	// -------------------------------------------------------------------------
	protected boolean handleKeyEvent( KeyEvent e )
	{
		return false;
	}
	
}


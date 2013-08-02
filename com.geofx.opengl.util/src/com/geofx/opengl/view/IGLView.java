/*******************************************************************************
 * Copyright (c) 2009 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 *******************************************************************************/

package com.geofx.opengl.view;

import java.awt.event.KeyEvent;

import javax.media.opengl.GLAutoDrawable;


/**
 * @author riwright
 *
 */
public interface IGLView
{	
	public void init (GLAutoDrawable drawable);
	
	public void display(GLAutoDrawable drawable); 

	public void dispose();
	
	public boolean handleKeyEvent( KeyEvent e );
}

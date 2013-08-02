/*
 * Copyright (c) 2006 Erik Tollerud (erik.tollerud@gmail.com) All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * The names of Erik Tollerud, David Raccagni, Sun Microsystems, Inc. or the names of
 * contributors may not be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. ERIK TOLLERUD,
 * SUN MICROSYSTEMS, INC. ("SUN"), AND SUN'S LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL ERIK
 * TOLLERUD, SUN, OR SUN'S LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT
 * OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF ERIK
 * TOLLERUD OR SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 * 
 * 
 * Cleaned up, added correct normal calculations and removed extraneous stuff.
 * Also modified method signatures to match, as closely as reasonable, those
 * of TextRenderer.  Also added support for compiling the shapes to display lists
 * on the fly.  Note that the class is now dependent on Java3D for the vecmath
 * package.
 * - Ric Wright, rkwright@geofx.com, June 2008
 */

package com.geofx.opengl.util;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Iterator;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import com.geofx.opengl.util.Extruder;

/**
 * This class renders a TrueType Font into OpenGL
 * 
 * @author Davide Raccagni
 * @author Erik Tollerud
 * @created January 29, 2004
 */
public class TextRenderer3D extends Extruder
{
	private Font 	font;

	private int lastIndex = -1;
	private ArrayList<Integer> listIndex = new ArrayList<Integer>();

	/**
	 * Instantiates a new TextRenderer3D initially rendering in the specified
	 * font.
	 * 
	 * @param font - the initial font for this TextRenderer3D
	 *        depth - the extruded depth for the font
	 * @throws java.lang.NullPointerException
	 *             if the supplied font is null
	 */
	public TextRenderer3D(Font font, float depth) throws NullPointerException
	{
		this.depth = depth;
		if (font == null)
			throw new NullPointerException("Can't use a null font to create a TextRenderer3D");
		this.font = font;
	}
	
	/**
	 * Specifies which font to render with this TextRenderer3D
	 * 
	 * @param font
	 *            a font to use for rendering *
	 * @throws java.lang.NullPointerException
	 *             if the supplied font is null
	 */
	public void setFont(Font font) throws NullPointerException
	{
		if (font == null)
			throw new NullPointerException("Can't set a TextRenderer3D font to null");
		this.font = font;
	}

	/**
	 * Retrieves the Font currently associated with this TextRenderer3D
	 * 
	 * @return the Font in which this object renders strings
	 */
	public Font getFont()
	{
		return this.font;
	}


	public void fill( String str, float xOff, float yOff, float zOff, float scaleFactor )
	{		
		GlyphVector gv = font.createGlyphVector(new FontRenderContext(new AffineTransform(), true, true),
				new StringCharacterIterator(str));
		GeneralPath gp = (GeneralPath) gv.getOutline();
		
		this.extrude(gp, xOff, yOff, zOff, scaleFactor);
	}
	
	/**
	 * Renders a string into the specified GL object, starting at the (0,0,0)
	 * point in OpenGL coordinates.
	 * 
	 * @param str
	 *            the string to render.
	 * @param glu
	 *            a GLU instance to use for the text rendering (provided to
	 *            prevent continuous re-instantiation of a GLU object)
	 * @param gl
	 *            the OpenGL context in which to render the text.
	 */
	public void fill( String str )
	{
		GlyphVector gv = font.createGlyphVector(new FontRenderContext(new AffineTransform(), true, true),
				                                new StringCharacterIterator(str));
		GeneralPath gp = (GeneralPath) gv.getOutline();
		
		extrude(gp);
	}	

	
	/**
	 * Get the bounding box for the supplied string with the current font, etc.
	 * 
	 * @param str
	 * @return
	 */
	public Rectangle2D  getBounds( String str )
	{
		GlyphVector gv = font.createGlyphVector(new FontRenderContext(new AffineTransform(), true, true),
				                                new StringCharacterIterator(str));
		GeneralPath gp = (GeneralPath) gv.getOutline();
		
		return gp.getBounds2D();
	}	
	
	/**
	 * Get the bounding box for the supplied string for the current font and 
	 * specified scale factor.
	 * 
	 * @param str
	 * @param scaleFactor
	 * @return
	 */
	public Rectangle2D  getBounds( String str, float scaleFactor  )
	{
		gl = GLU.getCurrentGL();

		gl.glPushAttrib(GL.GL_TRANSFORM_BIT);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		
		gl.glScalef(scaleFactor, scaleFactor, scaleFactor);
		
		GlyphVector gv = font.createGlyphVector(new FontRenderContext(new AffineTransform(), true, true),
				                                new StringCharacterIterator(str));
		GeneralPath gp = (GeneralPath) gv.getOutline();

		Rectangle2D rect = gp.getBounds2D();

		gl.glPopMatrix();
		gl.glPopAttrib();	
		
		return rect;
	}	
	
	/**
	 * Creates the specified string as a display list.  Can subsequently be drawn 
	 * by calling "call".
	 * 
	 * @param str
	 * @param xOff
	 * @param yOff
	 * @param zOff
	 * @param scaleFactor
	 */
	public int compile( String str, float xOff, float yOff, float zOff, float scaleFactor )
	{		
		int index = gl.glGenLists(1);
		gl.glNewList( index, GL.GL_COMPILE);
		fill( str, xOff, yOff, zOff, scaleFactor);
		gl.glEndList();
		
		listIndex.add(index);
		return index;
	}
	
	/**
	 * Creates the specified string as a display list.  Can subsequently be drawn 
	 * by calling "call".
	 * 
	 * @param str
	 */
	public int compile( String str )
	{		
		int index = gl.glGenLists(1);
		gl.glNewList( index, GL.GL_COMPILE);
		fill( str );
		gl.glEndList();
		
		listIndex.add(index);
		
		return index;
	}
	
	/**
	 * Draws the current compiled string, if any.
	 *
	 */
	public void call()
	{
		if (lastIndex != -1)
			gl.glCallList(this.lastIndex);
	}
	
	/**
	 * Draws the specified compiled string, if any.
	 *
	 */
	public void call( int index )
	{
		gl.glCallList( index );
	}
	
	/**
	 * Disposes of the ALL the current compiled shapes, if any.
	 *
	 */
	public void dispose()
	{
		for (Iterator iter = listIndex.iterator(); iter.hasNext();)
		{
			int index = (Integer) iter.next();
			gl.glDeleteLists( index, 1);
		}
		
		lastIndex = -1;
	}
	
	/**
	 * Disposes of the specified compiled shapes, if it is in the list.
	 * If it is the last-compiled, that index is cleared (set to -1)
	 *
	 */
	public void dispose( int index )
	{
		for (Iterator iter = listIndex.iterator(); iter.hasNext();)
		{
			int i = (Integer) iter.next();
			if (i == index)
			{
				gl.glDeleteLists( index, 1);
				if (index == lastIndex)
					lastIndex = -1;
				break;
			}
		}
	}
}

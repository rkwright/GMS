/*******************************************************************************
 * Copyright (c) 2008-09 Phil Zoio and Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * This code is in part derived from Eclipse wizard code, but also originally 
 * written by Phil Zoio as detailed in the article:
 * http://www.realsolve.co.uk/site/tech/jface-text.php
 * 
 * Contributors:
 * 	   Phil Zoio - 2004 - Original implementation
 *     Ric Wright - 2008-2009 - Bug fixes and tweaks
 *     
 ********************************************************************************/

package com.geofx.xmleditor.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * 
 */
public class EscapedCharRule implements IRule
{

	IToken fToken;
	StringBuffer buffer = new StringBuffer();

	public EscapedCharRule(IToken token)
	{
		super();
		this.fToken = token;
	}

	/*
	 * @see IRule#evaluate(ICharacterScanner)
	 */
	public IToken evaluate(ICharacterScanner scanner)
	{

		buffer.setLength(0);

		int c = read(scanner);
		if (c == '&')
		{
		    
		    int i = 0;
			do
			{
				c = read(scanner);
				i++;
				
				if (c == '<' || c == ']')
				{
				    System.out.println("Char " + (char)c);
					for (int j= i-1; j > 0; j--)
						scanner.unread();
					return Token.UNDEFINED;
				}
			}
			while (c != ';');
			return fToken;
		}

		scanner.unread();
		return Token.UNDEFINED;
	}
	

	private int read(ICharacterScanner scanner)
	{
		int c = scanner.read();
		buffer.append((char) c);
		return c;
	}

	
}
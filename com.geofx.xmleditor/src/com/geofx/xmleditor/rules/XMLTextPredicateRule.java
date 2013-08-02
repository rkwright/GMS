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
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * Extra rule which will return specified token if sequence of characters matches
 * 
 */
public class XMLTextPredicateRule implements IPredicateRule
{

	private IToken token;
	private int charsRead;
	private boolean whiteSpaceOnly;
	boolean inCdata;

	public XMLTextPredicateRule(IToken text)
	{
		this.token = text;
	}

	public IToken getSuccessToken()
	{
		return token;
	}

	public IToken evaluate(ICharacterScanner scanner, boolean resume)
	{
		return evaluate(scanner);
	}

	public IToken evaluate(ICharacterScanner scanner)
	{

		reinit();
		
		int c = 0;

		//carry on reading until we find a bad char
		//int chars = 0;
		while (isOK(c = read(scanner), scanner))
		{
			//add character to buffer
			if (c == ICharacterScanner.EOF)
			{
				return Token.UNDEFINED;
			}

			whiteSpaceOnly = whiteSpaceOnly && (Character.isWhitespace((char) c));
		}

		unread(scanner);

		//if we have only read whitespace characters, go back to where evaluation started and return undefined token
		if (whiteSpaceOnly)
		{
			rewind(scanner, charsRead);
			return Token.UNDEFINED;
		}

		return token;

	}


	private boolean isOK(int cc, ICharacterScanner scanner)
	{
		
		char c = (char) cc;

		if (!inCdata)
		{
			if (c == '<')
			{

				int cdataCharsRead = 0;

				for (int i = 0; i < "![CDATA[".length(); i++)
				{
					//whiteSpaceOnly = false;

					c = (char) read(scanner);
					cdataCharsRead++;
					
					if (c != "![CDATA[".charAt(i))
					{
						
						//we don't have a match - wind back only the cdata characters
						rewind(scanner, cdataCharsRead);
						inCdata = false;
						return false;
					}
				}

				inCdata = true;
				return true;

				//return false;
			}
		}
		else
		{

			if (c == ']')
			{

				for (int i = 0; i < "]>".length(); i++)
				{

					c = (char) read(scanner);

					if (c != "]>".charAt(i))
					{
						//we're still in the CData section, so just continue processing
						return true;
					}
				}

				//we found all the matching characters at the end of the CData section, so break out of this
				inCdata = false;

				//we're still in XML text
				return true;

			}
		}

		return true;

	}
	
	

	private void rewind(ICharacterScanner scanner, int theCharsRead)
	{
		while (theCharsRead > 0)
		{
			theCharsRead--;
			unread(scanner);
		}
	}
	
	private void unread(ICharacterScanner scanner)
	{
		scanner.unread();
		charsRead--;
	}
	private int read(ICharacterScanner scanner)
	{
		int c = scanner.read();
		charsRead++;
		return c;
	}
	

	private void reinit()
	{
		charsRead = 0;
		whiteSpaceOnly = true;
	}

}
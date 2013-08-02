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
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;

public class StartTagRule extends MultiLineRule
{

	public StartTagRule(IToken token)
	{
		this(token, false);
	}	
	
	protected StartTagRule(IToken token, boolean endAsWell)
	{
		super("<", endAsWell ? "/>" : ">", token);
	}

	protected boolean sequenceDetected(ICharacterScanner scanner, char[] sequence, boolean eofAllowed)
	{
		int c = scanner.read();
		if (sequence[0] == '<')
		{
			if (c == '?')
			{
				// processing instruction - abort
				scanner.unread();
				return false;
			}
			if (c == '!')
			{
				scanner.unread();
				// comment - abort
				return false;
			}
		}
		else if (sequence[0] == '>')
		{
			scanner.unread();
		}
		return super.sequenceDetected(scanner, sequence, eofAllowed);
	}
}
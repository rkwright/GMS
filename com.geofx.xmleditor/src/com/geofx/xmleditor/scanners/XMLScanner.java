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

package com.geofx.xmleditor.scanners;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;

import com.geofx.xmleditor.ColorManager;
import com.geofx.xmleditor.IXMLColorConstants;
import com.geofx.xmleditor.XMLWhitespaceDetector;


public class XMLScanner extends RuleBasedScanner
{

	public XMLScanner(ColorManager manager)
	{
		IToken procInstr = new Token(new TextAttribute(manager.getColor(IXMLColorConstants.PROC_INSTR)));
		IToken docType = new Token(new TextAttribute(manager.getColor(IXMLColorConstants.DOCTYPE)));

		IRule[] rules = new IRule[3];
		//Add rule for processing instructions and doctype
		rules[0] = new MultiLineRule("<?", "?>", procInstr);
		rules[1] = new MultiLineRule("<!DOCTYPE", ">", docType);
		// Add generic whitespace rule.
		rules[2] = new WhitespaceRule(new XMLWhitespaceDetector());

		setRules(rules);
	}
}

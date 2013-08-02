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

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

import com.geofx.xmleditor.rules.NonMatchingRule;
import com.geofx.xmleditor.rules.StartTagRule;
import com.geofx.xmleditor.rules.XMLTextPredicateRule;


public class XMLPartitionScanner extends RuleBasedPartitionScanner
{

	public final static String XML_DEFAULT = "__xml_default";
	public final static String XML_COMMENT = "__xml_comment";
	public final static String XML_PI = "__xml_pi";
	public final static String XML_DOCTYPE = "__xml_doctype";
	public final static String XML_CDATA = "__xml_cdata";
	public final static String XML_START_TAG = "__xml_start_tag";
	public final static String XML_END_TAG = "__xml_end_tag";
	public final static String XML_TEXT = "__xml_text";

	public XMLPartitionScanner()
	{

		IToken xmlComment = new Token(XML_COMMENT);
		IToken xmlPI = new Token(XML_PI);
		IToken startTag = new Token(XML_START_TAG);
		IToken endTag = new Token(XML_END_TAG);
		IToken docType = new Token(XML_DOCTYPE);
		IToken text = new Token(XML_TEXT);

		IPredicateRule[] rules = new IPredicateRule[7];

		rules[0] = new NonMatchingRule();
		rules[1] = new MultiLineRule("<!--", "-->", xmlComment);
		rules[2] = new MultiLineRule("<?", "?>", xmlPI);
		rules[3] = new MultiLineRule("</", ">", endTag);
		rules[4] = new StartTagRule(startTag);
		rules[5] = new MultiLineRule("<!DOCTYPE", ">", docType);
		rules[6] = new XMLTextPredicateRule(text);

		setPredicateRules(rules);
	}
}
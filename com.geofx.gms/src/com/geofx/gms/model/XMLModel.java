/*******************************************************************************
 * Copyright (c) 2009 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 ****************************************************************************/
package com.geofx.gms.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import com.geofx.gms.plugin.Constants;

public abstract class XMLModel extends DefaultHandler
{
	
	public static String 	VALIDATION = "http://xml.org/sax/features/validation";
	public static String 	NAMESPACES = "http://xml.org/sax/features/namespaces";
	public static String 	PREFIXES   = "http://xml.org/sax/features/namespace-prefixes";

	protected String 		characters;
	
	protected PrintWriter 	writer;
	protected OutputStream 	outStream;
	
	protected AttributesImpl attributes = new AttributesImpl();	
	
	/**
	 * This is the public method that gets called to parse the document.  The subclass
	 * should implement startElement, characters, and endElement and build out the 
	 * model as appropriate
	 */
	public void parse( InputStream stream )
	{
		// allow subclasses to initialize
		initParse();
		
		// get a factory
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try
		{
			// get a new instance of parser
			SAXParser sp = spf.newSAXParser();

			setFeatures(sp);

			// see what properties are set
			//checkFeatures(sp);

			// parse the file and also register this class for call backs
			sp.parse(stream, this);

		}
		catch (SAXException se)
		{
			se.printStackTrace();
		}
		catch (ParserConfigurationException pce)
		{
			pce.printStackTrace();
		}
		catch (IOException ie)
		{
			ie.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();		}
	}

	/**
	 * Just a stub to allow subclasses to initialize
	 */
	public abstract void initParse();

	/**
	 * Check for the variuous namespace and VALIDATION features
	 */
	void checkFeatures(SAXParser sp)
	{
		XMLReader reader;

		try
		{
			reader = sp.getXMLReader();

			System.out.println("Parser is " + (sp.isNamespaceAware() ? "" : " NOT ") + "configured to understand NAMESPACES.");
			System.out.println("Parser is " + (sp.isValidating() ? "" : " NOT ") + "configured to validate XML.");
			System.out.println("Parser is " + (sp.isXIncludeAware() ? "" : " NOT ") + "configured process XIncludes.");

			checkFeature(reader, VALIDATION);
			checkFeature(reader, NAMESPACES);
			checkFeature(reader, PREFIXES);

			System.out.println();
		}
		catch (SAXException e)
		{
			e.printStackTrace();
		}
	}

	/** 
	 * Check for a specific feature and report the result
	 */
	void checkFeature(XMLReader reader, String feature)
	{
		try
		{
			System.out.println(feature + " = " + reader.getFeature(feature));
		}
		catch (SAXNotRecognizedException e)
		{
			System.out.println("Property '" + feature + "' not recognized.");
		}
		catch (SAXNotSupportedException e)
		{
			System.out.println("Property '" + feature + "' apparently not supported");
		}
	}

	/**
	 * Turn on the VALIDATION and namespace support
	 */
	void setFeatures(SAXParser sp)
	{
		XMLReader reader;

		try
		{
			reader = sp.getXMLReader();

			reader.setFeature(VALIDATION, true);
			reader.setFeature(NAMESPACES, true);
			reader.setFeature(PREFIXES, true);
		}
		catch (SAXException e)
		{
			e.printStackTrace();
		}
	}

	//----------- Event Handlers -----------------------------------------
	
	/**
	 * These methods MUST be implemented by subclasses
	 * @throws SAXException 
	 */
	public abstract void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException; 

	public void dumpAttributes()
	{
	 	for ( int i = 0; i < attributes.getLength(); i++ ) 
		{
			  String uriStr = attributes.getURI(i);
			  String localNameStr = attributes.getLocalName(i);
			  String value = attributes.getValue(i);
			  String qNameStr = attributes.getQName(i);
			  String type = attributes.getType(i);
			  
			  System.out.println(i + " uri: '" + uriStr + "' localName: '" + localNameStr + "' qName: '" + qNameStr + "' value: '" + value + "' type: '" + type);
		}		
	}
	
	public abstract void characters(char[] ch, int start, int length) throws SAXException;

	public abstract void endElement(String uri, String localName, String qName) throws SAXException;

	public void startPrefixMapping(String prefix, String uri) throws SAXException
	{
		// System.out.println("startPrefixMapping. prefix: '" + prefix + "' uri: '" + uri + "'" );		
	}

	public void endPrefixMapping(String prefix) throws SAXException
	{
		// System.out.println("endPrefixMapping. prefix: '" + prefix + "'" );				
	}

	//============== Serializer Section ================================

	protected Stack<OpenElement> 	openElements = new Stack<OpenElement>();
	protected String				defaultNamespace;
	protected boolean				openElement = false;	

	public static class OpenElement 
	{
		String 			localName;
		String 			namespace;
		String 			prefix;
		String 			priorDefaultNamespace;
		
		protected AttributesImpl	attributes;

		public OpenElement(String namespace, String prefix, String localName, Attributes attributes ) 
		{
			this.localName  = localName;
			this.namespace  = namespace;
			this.prefix     = prefix;
			if (attributes != null)
				this.attributes = new AttributesImpl(attributes);
		}

		public AttributesImpl getAttributes()
		{
			return attributes;
		}

		public String getLocalName()
		{
			return localName;
		}

		public String getNamespace()
		{
			return namespace;
		}
	}

	protected  void serialize( OutputStream stream )
	{
		this.outStream = stream;
	}

	protected void resetSerialization()
	{
		// reset all the variables
		openElements.clear();
		defaultNamespace = null;
		openElement = false;
	}

	public void beginDocument(String version, String encoding)
	{
		if (encoding == null)
			encoding = "utf-8";
		try
		{
			writer = new PrintWriter(new OutputStreamWriter(outStream, encoding));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new IllegalArgumentException("unsupported encoding: '" + encoding + "'");
		}
		
		writer.println("<?xml version=\"" +  version + "\" encoding=\"" + encoding + "\"?>");
	}

	public void finishDocument()
	{
		while (!openElements.isEmpty())
		{
			OpenElement e = (OpenElement) openElements.pop();
			finishElement(e.namespace, e.prefix, e.localName);
		}
		
		newLine();
		writer.close();
	}

	public void beginElement( String namespace, String prefix, String name, Attributes attributes )
	{
		closeTagIfNeeded(false);

		OpenElement e = new OpenElement(namespace, prefix, name, null);
		openElements.push(e);
		
		insertTabs(1);
		
		writer.print("<");
		
		String newDefaultNS = null;
		if (!namespace.equals(defaultNamespace))
		{			
			if (e.prefix == null)
			{
				e.priorDefaultNamespace = defaultNamespace;
				defaultNamespace = namespace;
				newDefaultNS = namespace;
			}
			else
			{
				writer.print(e.prefix + ":");
			}
		}
		
		writer.print(name);
		
		if (newDefaultNS != null)
		{
			writer.print(" xmlns=\"" +  escapeAttributes(newDefaultNS) + "\"");
		}
		
		if (attributes != null)
		{
			for ( int i = 0; i < attributes.getLength(); i++ ) 
			{
				String nom = attributes.getLocalName(i);
				if (nom.length() == 0)
						nom = attributes.getQName(i);
				
				if (!(nom.length() == 0))
					writer.print(" " + nom + "=\"" + attributes.getValue(i) + "\"");
			}
		}
		
		openElement = true;
	}

	private void insertTabs( int extra )
	{
		for (int i=0; i<openElements.size()-extra; i++ )
		{
			writer.print("\t");			
		}
	}

	public void finishElement( String namespace, String prefix, String name )
	{
		OpenElement e = (OpenElement) openElements.peek();
		if (!sameNamespace(e.namespace, namespace) || !e.localName.equals(name))
		{
			int i, len = openElements.size();
			for (i = len - 2; i >= 0; i--)
			{
				e = (OpenElement) openElements.elementAt(i);
				if (sameNamespace(e.namespace, namespace) && e.localName.equals(name))
					break;
			}
			
			if (i < 0)
				return;
			
			while (true)
			{
				e = (OpenElement) openElements.peek();
				if (sameNamespace(e.namespace, namespace) && e.localName.equals(name))
					break;
				finishElement(e.namespace, e.prefix,e.localName);
			}
		}
		
		
		openElements.pop();
		if (openElement)
		{
			writer.print("/>");
			openElement = false;
		}
		else
		{
			insertTabs(0);
			
			writer.print("</");
			if (e.prefix != null)
			{
				writer.print(e.prefix + ":");
			}
			
			writer.print(e.localName +  ">");
		}
		
		newLine();
		
		if (e.priorDefaultNamespace != null)
			defaultNamespace = e.priorDefaultNamespace;
	}
	
	private void closeTagIfNeeded( boolean isText ) 
	{
		if (openElement) 
		{
			writer.print(">");
			openElement = false;
			if (!isText)
				newLine();
		}
	}
	
	private boolean sameNamespace(String ns1, String ns2) 
	{
		if (ns1 == null || ns2 == null)
			return false;
		
		return ns1.equals(ns2);
	}

	private String escapeAttributes( String value )
	{
		value = value.replaceAll("&", "&amp;");
		value = value.replaceAll("<", "&lt;");
		value = value.replaceAll(">", "&gt;");
		value = value.replaceAll("\"", "&quot;");
		
		return value;
	}

	public void newLine()
	{
		text(Constants.NEWLINECHAR, 0, 1);
	}

	public void text( String str)
	{
		text(str.toCharArray(), 0, str.length());
	}
		
	public void text(char[] text, int offset, int len)
	{
		closeTagIfNeeded(true);
		
		int end = offset + len;
		String ent = null;
		for (int i = offset; i < end; i++ )
		{
			char c = text[i];
			switch (c)
			{
				case '&':
					ent = "&amp;";
					break;
				case '<':
					ent = "&lt;";
					break;
				case '>':
					ent = "&gt;";
					break;
				default:
					continue;
			}
			if (i > offset)
				writer.write(text, offset, i - offset);
			offset = i + 1;
			writer.print(ent);
		}
		
		if (end > offset)
			writer.write(text, offset, end - offset);
	}
}

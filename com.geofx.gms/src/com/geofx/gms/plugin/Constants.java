/*******************************************************************************
 * Copyright (c) 2008 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 *******************************************************************************/

package com.geofx.gms.plugin;

import java.io.File;

import org.eclipse.core.runtime.QualifiedName;

public class Constants
{
	public static final String		GMS_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<gms xmlns=\"http://com.geofx.gms/2008\" >\n<metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" />\n<manifest/>\n<layers/>\n</gms>";

	public static final String		NS_DUBLINCORE = "http://purl.org/dc/elements/1.1/";
	
	public static final String 		CONTAINER_XML = "<?xml version=\"1.0\"?>\n<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">\n<rootfiles>\n<rootfile full-path=\"project.gms\" media-type=\"application/gms-package+xml\"/>\n</rootfiles>\n</container>";
	
	public static final String 		GMS_PROJECT = "project.gms";

	public static final String 		GMS_PROJECT_TEMP = "project.tmp";
	
	public static final String		GAZ_MIMETYPE = "application/gms+gaz";
	
	//namespace URI for the properties
	public static final String		PROPERTY_NAMESPACE		= "http://www.geofx.com/gms";
	
	// property name for the unique identifier the project
	public static final String		PROJECT_UUID			= "project_uuid";

	// property qualified name for the unique identified for the project
	public static final QualifiedName	UUID_PROPERTY_NAME	= new QualifiedName(PROPERTY_NAMESPACE, PROJECT_UUID);

	// property name for the create date the project
	public static final String		PROJECT_DATE			= "project_date";

	// property qualified name for the unique identified for the project
	public static final QualifiedName	DATE_PROPERTY_NAME	= new QualifiedName(PROPERTY_NAMESPACE, PROJECT_DATE);

	
	public static String NS_OASIS = "urn:oasis:names:tc:opendocument:xmlns:container";
	public static String GMS_MIMETYPE = "application/gms-package+xml";
	public static String GMS_PROJECT_NAME = "project.gms";
	public static String OCF_CONTAINERFILE = "container.xml";
	public static String OCF_CONTAINER = "container";
	public static String OCF_ROOTFILES = "rootfiles";
	public static String OCF_ROOTFILE = "rootfile";
	public static String FULL_PATH = "full-path";
	public static String MEDIA_TYPE = "media-type";
	public static String XMLNS = "xmlns";
	public static String METADATA = "metadata";
	public static String  TITLE = "title";
	public static String  DATE = "date";
	public static String  DESCRIPTION = "description";
	public static String  IDENTIFIER = "identifier";
	public static String  NAME = "name";
	public static String  PATH = "path";
	public static String NS_OPF = "http://www.idpf.org/2007/opf";
	public static String PACKAGE = "package";
	public static String MANIFEST = "manifest";
	public static String ITEM = "item";
	public static String ID = "id";
	public static String VERSION = "version";
	public static String HREF = "href";
	public static String SPINE = "spine";
	public static String ITEMREF = "itemref";
	public static String IDREF = "idref";
	public static String NS_DUBLINCORE_TERMS = "http://purl.org/dc/terms/";
	public static String NS_XMLSCHEMA = "http://www.w3.org/2001/XMLSchema-instance";
	public static String CDATA = "http://www.w3.org/2001/XMLSchema-instance";
	
	public static String SEQUENCE = "sequence";
	public static String MODULE = "module";
	public static String OBJECT = "object";
	public static String TAB = "tab";
	public static String INIT = "init";
	public static String INPUTS = "inputs";
	public static String INPUT = "input";
	public static String ITERATOR = "iterator";
	public static String OUTPUTS = "outputs";
	public static String OUTPUT = "output";
	public static String SET = "set";
	public static String FIELD = "field";
	public static String VALUE = "value";
	public static String TYPE = "type";
	public static String SAMPLE = "sample";

	

	public static char[] 	NEWLINECHAR = {'\n'};

	public static String NS_GMS = "http://www.geofx.com/2009/gms";
	public static String GMS_VERSION = "1.0.0";
	
	public static String DATASET = "dataset";
	public static String ARRAY = "array";
	public static String ELEMENT = "dataset";
	public static String PREFIX_XMLSCHEMA = "xmlns:xsi";
	public static String XSI_TYPE = "xsi:type";
	public static String XSI_DECIMAL = "decimal";
	
	// toolbar icons
	public static String TOOLBAR_PATH = "icons" + File.pathSeparator + "toolbar" + File.pathSeparator;
	
	public static String RESTART_EN = "restart_en";
	public static String RESUME_EN = "resume_en";
	public static String SUSPEND_EN = "suspend_en";
	public static String TERMINATE_EN = "terminate_en";
	public static String TERMINATE_DIS = "terminate_dis";
	public static String STEPOVER_EN = "stepover_en";
	public static String STEPOVER_DIS = "stepover_dis";

}

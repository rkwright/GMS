/*****************************************************************************
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

import org.xml.sax.Attributes;

/**
 * Private class to handle the manifest entries. Not much to it.
 * 
 */
public class ManifestEntry 
{
	public String	id;
	public String 	href;
	public String 	mediaType;

	public ManifestEntry (String id, String href, String mediaType )
	{
		this.id = id;
		this.href = href;
		this.mediaType = mediaType;			
	}

	public ManifestEntry ( Attributes attributes )
	{
		this.id = attributes.getValue("", "id");
		this.href = attributes.getValue("", "href");
		this.mediaType = attributes.getValue("", "media-type");
	}
}




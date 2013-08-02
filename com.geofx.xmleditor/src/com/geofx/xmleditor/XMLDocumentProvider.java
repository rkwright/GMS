/*******************************************************************************
 * Copyright (c) 2008 Phil Zoio and Ric Wright 
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
 * 	   Phil Zoio - 2006 - Original implementation
 *     Ric Wright - 2008-2009 - Bug fixes and tweaks
 *     
 ********************************************************************************/

package com.geofx.xmleditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import com.geofx.xmleditor.scanners.XMLPartitionScanner;


public class XMLDocumentProvider extends FileDocumentProvider
{

protected IDocument createDocument(Object element) throws CoreException
{
    IDocument document = super.createDocument(element);
    if (document != null)
    {
        IDocumentPartitioner partitioner = new XMLPartitioner(new XMLPartitionScanner(), new String[]
        {
                XMLPartitionScanner.XML_START_TAG,
                XMLPartitionScanner.XML_PI,
                XMLPartitionScanner.XML_DOCTYPE,
                XMLPartitionScanner.XML_END_TAG,
                XMLPartitionScanner.XML_TEXT,
                XMLPartitionScanner.XML_CDATA,
                XMLPartitionScanner.XML_COMMENT
        });
        partitioner.connect(document);
        document.setDocumentPartitioner(partitioner);
    }
    return document;
}
}
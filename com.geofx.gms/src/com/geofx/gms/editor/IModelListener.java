/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.geofx.gms.editor;

/**
 * @author dejan
 * 
 *         To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Generation - Code and Comments
 */
public interface IModelListener
{
	String ADDED = "__added"; //$NON-NLS-1$
	String REMOVED = "__removed"; //$NON-NLS-1$
	String CHANGED = "__changed"; //$NON-NLS-1$

	void modelChanged( String type );
}
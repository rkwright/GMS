/*******************************************************************************
 * Copyright (c) 2008 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 *     
 ****************************************************************************/
package com.geofx.opengl.util;

import java.util.Scanner;

public class Scanf
{
	protected static Scanner	scanner = null;
	
	public static void  read ( String format, String... args )
	{
		if (scanner == null)
		{
			scanner = new Scanner(format);
		}
		
		
	}
}

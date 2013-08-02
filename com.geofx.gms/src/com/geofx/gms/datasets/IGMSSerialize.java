/*******************************************************************************
  * Copyright (c) 2009 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 *******************************************************************************/
package com.geofx.gms.datasets;

import java.nio.ByteBuffer;

public interface IGMSSerialize
{
	public void serializeOut ( ByteBuffer outBuffer );
	public void serializeIn ( ByteBuffer inBuffer );
	
	public int serializeSize();
	public boolean compareTo( Object obj );
}

/*******************************************************************************
 * Copyright (c) 2011 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 *******************************************************************************/

package com.geofx.gms.controller;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.geofx.gms.controller.Clock.ClockState;



public class PauseSimHandler extends SimHandler
{

	public PauseSimHandler()
	{
		super();
		
		handlerClass = "PauseSimHandler";
		enabledStates.add(ClockState.RUNNING);
	}

	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		super.execute(event);
		
		if (controller != null)
		{
			controller.pause();

			updateEnabled(event);
		}

		return null;
	}
}

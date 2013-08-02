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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

import com.geofx.gms.controller.Clock.ClockState;
import com.geofx.gms.editor.GMSEditor;
import com.geofx.gms.plugin.GMSPlugin;

public class ClockStateProvider extends AbstractSourceProvider
{
	public final static String 	CLOCK_STATE_ID = "com.geofx.gms.commands.clockState"; 
	private ClockState 			clockState = ClockState.PAUSED;

	@Override
	public Map<String,String> getCurrentState()
	{
		Map<String, String> currentState = new HashMap<String, String>(1);

		currentState.put(CLOCK_STATE_ID,  getClockStateStr());
		
        return currentState; 
	}

	@Override
	public String[] getProvidedSourceNames()
	{
		return new String[] { CLOCK_STATE_ID  };
	}

	public void setClockState ( ClockState clockState )
	{
		// if no change, just bail
		if (this.clockState == clockState)
			return;

		this.clockState = clockState;
		
		fireSourceChanged(ISources.WORKBENCH, CLOCK_STATE_ID, getClockStateStr()); 
	}

	@Override
	public void dispose(){}
	
	private String getClockStateStr()
	{
		String			state = Clock.ClockStateStr[this.clockState.ordinal()];
		GMSEditor		editor = GMSPlugin.getEditor();
		if (editor!= null)
		{
			Controller  	controller = editor.getController();
			if (controller != null)
		 		state = Clock.ClockStateStr[controller.getClockState().ordinal()];
		}
		return state;
	}
}

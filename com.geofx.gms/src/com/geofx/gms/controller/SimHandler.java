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

import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.ISourceProviderService;

import com.geofx.gms.controller.Clock.ClockState;
import com.geofx.gms.editor.GMSEditor;
import com.geofx.gms.plugin.GMSPlugin;

public abstract class SimHandler extends AbstractHandler
{
	protected boolean 		isEnabled = true;
	protected String		handlerClass;
	protected ArrayList<ClockState>	enabledStates = new ArrayList<ClockState>();
	protected GMSEditor		editor;
	protected Controller  	controller;

	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		// System.out.println(handlerClass + ":execute");

		editor = GMSPlugin.getEditor();
		if (editor!= null)
		{
			controller = editor.getController();
		}
		
		return null;
	}
	
	@Override
	public boolean isEnabled()
	{
		ClockState state = getClockState();
		isEnabled = false;
		for ( int i=0; i<enabledStates.size(); i++ )
		{
			isEnabled = enabledStates.get(i) == state;
			if (isEnabled)
				break;
		}
		
		return isEnabled;  
	}

	public void updateEnabled( ExecutionEvent event )
	{	
		isEnabled();
		
		// System.out.println(handlerClass + ":updateEnabled - state: " + getClockState() + " enabled: " + isEnabled);
		
		// get the window (which is a IServiceLocator)
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		// get the service
		ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
		// get our source provider by querying by the variable name
		ClockStateProvider clockStateProvider = (ClockStateProvider) service.getSourceProvider(ClockStateProvider.CLOCK_STATE_ID);
		// set the value
		clockStateProvider.setClockState(getClockState()); 
		
		ICommandService command = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		command.refreshElements("com.geofx.gms.commands.RestartSim", null);
		command.refreshElements("com.geofx.gms.commands.PauseSim", null);
		command.refreshElements("com.geofx.gms.commands.ResumeSim", null);
		command.refreshElements("com.geofx.gms.commands.SingleStepSim", null);
		command.refreshElements("com.geofx.gms.commands.TerminateSim", null);
		
	}

	public ClockState getClockState()
	{
		ClockState 		state = ClockState.UNDEFINED;
		GMSEditor		editor = GMSPlugin.getEditor();
		if (editor!= null)
		{
			Controller  	controller = editor.getController();
			if (controller != null)
		 		state = controller.getClockState();
		}
	
		return state;	
	}

}
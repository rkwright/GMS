package com.geofx.gms.controller;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.geofx.gms.controller.Clock.ClockState;

public class ResumeSimHandler extends SimHandler
{

	public ResumeSimHandler()
	{
		super();
		
		handlerClass = "ResumeSimHandler";
		enabledStates.add(ClockState.PAUSED);
	}

	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		super.execute(event);
		
		if (controller != null)
		{
			controller.resume();

			updateEnabled(event);
		}

		return null;
	}
}

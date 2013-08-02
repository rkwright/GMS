package com.geofx.gms.controller;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.geofx.gms.controller.Clock.ClockState;

public class SingleStepSimHandler extends SimHandler
{
	public SingleStepSimHandler()
	{
		super();
		
		handlerClass = "SingleStepSimHandler";
		enabledStates.add(ClockState.PAUSED);
	}

	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		super.execute(event);
		
		if (controller != null)
		{
			controller.singleStep();

			updateEnabled(event);
		}

		return null;
	}
}

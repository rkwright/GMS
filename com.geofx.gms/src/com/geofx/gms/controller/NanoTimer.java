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

package com.geofx.gms.controller;

import java.util.ArrayList;

/**
 * This class implements a simple timer.  It spawns a single thread in which its
 * sub-class. TimerLoop (which implements Runnable) simply loops continuously.
 * TimerLoop calls the event listeners at the end of every frame.
 *
 */
public class NanoTimer
{
	private static final double TIME_CLAMP  = 0.25;
	private static final int	SLEEPY_TIME	= 2;

	// listeners that will receive timing events
	private ArrayList<ITimerListener> listeners = new ArrayList<ITimerListener>();

	protected Thread		thread;
	protected boolean		stop = false;
	protected double 		currentTime;
	protected double 		accumulator;
	protected double 		dt = 1.0 / 60.0;	// 60 FPS, by default

	public NanoTimer()
	{
		thread = new Thread(new TimerLoop());
	}
	
	/**
	 * Set the resolution in nanoseconds. Input is in frames/sec
	 */
	public void setFPS(double fps)
	{
		dt = 1.0 / fps;
	}


	/**
	 * Simply starts the thread and resets the clock.
	 */
	public void start()
	{
		System.out.println("NanoTimer:start");
		this.stop = false;
		currentTime = System.nanoTime() / 1e9;
		thread.start();
	}

	
	synchronized public void stop()
	{
		System.out.println("NanoTimer:stop");
		this.stop = true;
	}

	synchronized boolean shouldStop()
	{
		return stop;
	}
	
	/**
	 * Adds a TimingEventListener to the set of listeners that receive timing
	 * events from this TimingSource.
	 * 
	 * @param listener
	 *            the listener to be added.
	 */
	public final void addEventListener(ITimerListener listener)
	{
		synchronized (listeners)
		{
			if (!listeners.contains(listener))
			{
				listeners.add(listener);
			}
		}
	}

	/**
	 * Removes a TimingEventListener from the set of listeners that receive
	 * timing events from this TimingSource.
	 * 
	 * @param listener
	 *            the listener to be removed.
	 */
	public final void removeEventListener(ITimerListener listener)
	{
		synchronized (listeners)
		{
			listeners.remove(listener);
		}
	}
	
	/**
	 * Subclasses call this method to post timing events to this object's
	 * {@link TimingEventListener} objects.
	 */
	protected final void timingEvent()
	{
		synchronized (listeners)
		{
			for (ITimerListener listener : listeners)
			{
				listener.timingEvent();
			}
		}
	}
	
	/**
	 * 
	 * @author riwright
	 *
	 */
	private class TimerLoop implements Runnable
	{

		public void run()
		{
			while (shouldStop() == false)
			{
				try
				{
					double newTime = System.nanoTime() / 1e9;
					double deltaTime = newTime - currentTime;
					currentTime = newTime;

					if (deltaTime > TIME_CLAMP)
						deltaTime = TIME_CLAMP;

					accumulator += deltaTime;

					while (accumulator >= dt)
					{
						accumulator -= dt;
					
						// call the event listener(s)
						timingEvent();
						
						// then sleep for a couple of ticks
						Thread.sleep(SLEEPY_TIME); 
					}
		
			}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			
			System.out.println("NanoTimerLoop exiting");

		}
	}
}

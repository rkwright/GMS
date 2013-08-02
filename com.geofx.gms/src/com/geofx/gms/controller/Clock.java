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

/**
 * A general class for timing of animations and related uses.  Note that
 * it supports two precisions:
 * 
 * 	 NANO, where the returned values are in nanoseconds since the start of the clock
 * 	 MILLI, where the return values are in milliseconds since either the start of the
 *           clock (where type is RELATIVE) or the start of UNIX time (1 Jan 1970).
 *           
 * Two types, RELATIVE and ABSOLUTE are supported, though only RELATIVE is supported
 * for nanosecond precision since the nanosecond clock has no support for absolute time.
 * 
 * The relative mode of the clock also supports "pausing". This allows animations to be
 * paused.  Pausing is only supported in RELATIVE mode.  When the clock is unpaused, the 
 * start value is adjusted so no time appears to have passed while paused.
 * 
 * Finally, there is also an incremental mode which steps forward by whatever increment was
 * supplied to the constructor every time the method time() is called.  The current clock time 
 * can also be read WITHOUT causing the step by using peekTime().
 * 
 * Clocks are always instantiated in paused mode, if type is RELATIVE
 * 
 * State: 	Stopped
 * 		paused = -1
 * 		current = begin
 * 		start = begin
 * 
 * State: Paused
 * 		paused = current when paused was called
 * 		current = variable 
 * 		start = begin 
 * 
 * State: Running
 * 		paused = 0
 * 		current = variable
 * 		start = begin
 * 		
 *
 */
public class Clock
{
	public enum ClockType { RELATIVE, ABSOLUTE, INCREMENTAL };
	public enum ClockPrecision { NANO, MILLI };
	public enum ClockState { UNDEFINED, RUNNING, PAUSED, STOPPED };
	public final static String[] ClockStateStr = { "undefined", "running", "paused", "stopped" };
	
	protected ClockState		currentState;
	protected long				zot;
    protected long				start;
    protected long 				current;
    protected long 				paused;
    protected ClockType 		type = ClockType.RELATIVE;
    protected ClockPrecision 	precision = ClockPrecision.NANO;
    protected double 			begin = 0;
    protected double			end = 0;
    protected double 			scale = 1.0 / 1e9; 
    protected boolean			incrementalStart = true;
 
    /**
     * Default ctor, which defaults the type of clock to
     * RELATIVE and precision to NANO
     */
    public Clock()
    {
    	reset();
     }

    /**
     * Allows caller to set the clock type and precision.  Note that only RELATIVE 
     * is allowed if precision is NANO.
     * 
     * @param type
     */
    public Clock( ClockType type, double begin, double end, double scale )
    {
    	this.type = type;
    	
    	if ( type == ClockType.RELATIVE || type == ClockType.INCREMENTAL)
    	{
    		this.precision = ClockPrecision.NANO;
    		this.begin = begin;
    		this.end = end;
    		this.scale = scale / (type == ClockType.INCREMENTAL ? 1 : 1e9);
    	}
    	else if ( type == ClockType.ABSOLUTE )
    	{
    	   	this.precision = ClockPrecision.MILLI;
    	   	this.begin = 0;
    	   	this.end = 0;
    	   	this.scale = 1 / 1e3;
    	}
     	
    	reset();
    }

    /**
	 * Reset everything. and also pauses the clock, if in RELATIVE mode
	 */
	protected void reset()
	{		
	    if ( type == ClockType.ABSOLUTE)
	    {
	       	paused = start = 0;
	       	current = System.currentTimeMillis();
	    }
	    else if (type == ClockType.INCREMENTAL)
		{
	    	start = (long) begin;
	    	current = start;
	    	paused = 0;
	    	incrementalStart = true;
		}
	    else
	    {
	    	paused = start = current = System.nanoTime();
	    }
	    
	    currentState = ClockState.STOPPED;
	}

	/**
     * Returns the time as a long, in ms or ns, as appropriate, either relative to the last 
     * start of the clock, or since the start of UNIX time (1970) if precision is MILLI 
     * and type is ABSOLUTE
     */
	public long timeRaw()
	{
		update();
   
	    return current - start;
	}

    /**
	 * Just updates the counters with the current time
	 */
	protected void update()
	{
		if (type != ClockType.INCREMENTAL)
		{
			current = precision == ClockPrecision.NANO ? System.nanoTime() : System.currentTimeMillis();
		}
	}

	/**
     *  Returns the time in seconds, either relative to the last start of the clock, or
     *  since the start of UNIX time (1970) if precision is MILLI and type is ABSOLUTE
     *  
     *  Note that calling this for an INCREMENTAL clock causes the clock to step forward.
     *  To avoid this, use peekTime().
     * 
     */
	public double time()
	{
		if (type == ClockType.INCREMENTAL)
		{
			long curTime = current;
			
			if (currentState != ClockState.PAUSED && currentState != ClockState.STOPPED && !incrementalStart)
				current += scale;

			incrementalStart = false;
			
			// System.out.println("Clock:time: " + getState() + String.format(" current: %8d, curTime: %8d", current, curTime));

			return curTime;
		}
	
		update();
		 
		return paused == 0 ? (begin + (current - start) * scale) : (begin + (paused - start) * scale);
	}	
	
	/**
	 * Returns the time.  Same as time() EXCEPT that for INCREMENTAL clocks, this does not cause
	 * the time to be incremented.
	 * 
	 * @return
	 */
	public double peekTime()
	{
		if (type == ClockType.INCREMENTAL)
		{
			return current;
		}
	
		update();
		 
		return paused == 0 ? (begin + (current - start) * scale) : (begin + (paused - start) * scale);
	}
	

    /**
     * This restarts the clock. 
     */
    public void restart()
    {
    	reset();
     	resume();
    	
    	// System.out.println("Clock:restart: " + getState() + " current: " + current + " pause: " + paused);
    }
    /**
     * This effectively unpauses the clock. Only applicable if in
     * RELATIVE time.  If resuming from a pause it adds the difference 
     * between the paused time and now to the start value. 
     */
    public void resume()
    {
    	update();
    	
    	if (type == ClockType.RELATIVE && isPaused() )
    	{    	
    		start += current - paused;
    		paused = 0;
    	}
    	else if (type == ClockType.INCREMENTAL && currentState == ClockState.PAUSED)   // || isStopped()) )
    	{
    		paused = 0;
    	}
    	
     	currentState = ClockState.RUNNING;

    	// System.out.println("Clock:resume: " + getState() + " current: " + current + " pause: " + paused);
    }

    /**
	 * This effectively pauses or unpauses the clock. Only applicable if in
	 * RELATIVE time and clock not already paused.  If pausing the clock, 
	 * it stores the current time.
	 */
	public void pause()
	{
		update();
		
		if ((type == ClockType.RELATIVE || type == ClockType.INCREMENTAL) && currentState != ClockState.PAUSED)
		{
			paused = current;
			currentState = ClockState.PAUSED;
		}
	
		// System.out.println("pause: " + getState());
	}

	/**
     * This stops the clock, which has no effect in ABSOLUTE time. In INCREMENTAL time,
     * it resets  the start value to initial values and sets paused to 0. Only applicable if in
     * RELATIVE time.  
     */
    public void stop()
    {
       	update();
   	
    	/*
 
    	if (type == ClockType.INCREMENTAL)
    	{    	
    		current = (long) begin;
      		paused = -1;
      	}
    	 */
    	
    	reset();
    	
    	// System.out.println("Clock:stop: " + getState());
   }

	/**
     * Just checks if the clock is currently paused.
     * 
     */
    public boolean isPaused ()
    {
    	/*
    	if (type == ClockType.RELATIVE && paused == current)
			return true;
    	else if (type == ClockType.INCREMENTAL && paused == current && current != start)
    		return true;
	else 
		return false;
		*/
    	
    	return currentState == ClockState.PAUSED;
    }

	/**
     * Just checks if the clock is currently stopped.
     * 
     */
    public boolean isStopped ()
    {
    	return currentState == ClockState.STOPPED;  // paused < 0;
    }

    /**
     * Just checks for the current clock state
     * 
     */
    public ClockState getState ()
    {
    	/*
    	if (isPaused())
			return ClockState.PAUSED;
       	else if (isStopped())
    		return ClockState.STOPPED;
     	else if (current >= 0)
    		return ClockState.RUNNING;
     	else
    		return ClockState.UNDEFINED;
    	*/
    	
    	return currentState;
    }
    
	public String getClockStateStr()
	{		
		return ClockStateStr[currentState.ordinal()];
	}
    /**
	 * Just returns the current ClockType
	 * @return
	 */
	public ClockType getClockType()
	{
		return type;
	}

	/**
	 * Sets the ClockType. Also causes a reset and pause.
	 * 
	 * @param clockType
	 */
	public void setClockType ( ClockType type )
	{
		this.type = type;
		reset();
	}

	/**
	 * Just returns the current precision
	 */
	public ClockPrecision getPrecision()
	{
		return precision;
	}

	/**
	 * Sets the current precision.  Also resets the clock and pauses it
	 * if not in ABSOLUTE mode.
	 */
	public void setPrecision(ClockPrecision precision)
	{
		this.precision = precision;
		reset();
	}

	public long getStart()
	{
		return start;
	}

	public void setStart(long start)
	{
		this.start = start;
	}

	public double getScale()
	{
		return scale;
	}

	public void setScale(double scale)
	{
		this.scale = scale;
	}


}

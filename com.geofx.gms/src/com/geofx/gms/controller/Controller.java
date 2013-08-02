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

import javax.vecmath.Point2d;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.xml.sax.Attributes;

import com.geofx.gms.controller.Clock.ClockState;
import com.geofx.gms.controller.Clock.ClockType;
import com.geofx.gms.model.Input;
import com.geofx.gms.model.ManifestEntry;
import com.geofx.gms.model.Module;
import com.geofx.gms.model.ProjectInfo;
import com.geofx.gms.plugin.GMSPlugin;
import com.geofx.gms.viewers.ViewInfo;
import com.geofx.opengl.view.GLComposite;

public class Controller implements ITimerListener
{
	public enum 	TimingAttributes 
	{ 
		begin, end, step, dur, rate, repeat, incremental;
	
		public static TimingAttributes convert(int i)
		{
			for (TimingAttributes current : TimingAttributes.values())
			{
				if (current.ordinal() == i)
				{
					return current;
				}
			}
			
			return null;
		}
		
	
	}

	Point2d	pt = new Point2d();
	
	private static final double FRAMERATE = 60.0;				// in frames per second

	protected String[]		timingAttribs = new String[7];

	protected enum 			TimeUnits { y, d, h, s };
	protected double[]		timeUnits = { 365*24*3600, 24*3600, 3600, 1 };
	
	ArrayList<Module>		viewModules = new ArrayList<Module>();
	ArrayList<String>		tabList = new ArrayList<String>();
	
	protected Clock 		modelClock;
	protected NanoTimer		nanoTimer;

	protected double      	begin;
	protected double        end;
	protected double        rate;
	protected double        step;
	protected double        dur;
	protected boolean       repeat = false;
	protected boolean 		incremental = false;
	protected boolean		singleStep = false;

	protected ProjectInfo	projectInfo;

	protected ArrayList<GLComposite>		glComposites = new ArrayList<GLComposite>();
	
	/**
	 * Master controller for the modeling process.
	 * 
	 * @param attributes
	 * @param projectInfo
	 */
	public Controller ( Attributes attributes, ProjectInfo projectInfo )
	{
		parseSequenceElement(attributes);
		
		modelClock = new Clock( ClockType.INCREMENTAL, begin, end, step );  ///FRAMERATE );

		//animator = new Animator(100000, this);
		nanoTimer = new NanoTimer();
		nanoTimer.setFPS(FRAMERATE);
		nanoTimer.addEventListener(this);
		nanoTimer.start();	
		
		//animator.setTimer(nanoTimer);
		
		this.projectInfo = projectInfo;
		
		initModules();	

		GMSPlugin.getEditor().setController(this);
	}
	

	protected void initModules()
	{
		for ( int i=0; i<projectInfo.modules.size(); i++ )
		{
			Module module = projectInfo.modules.get(i);
			
			if (module != null)
			{		
				module.setController(this, modelClock);

				module.initDatasets();

				module.init();

			}
		}		
	}
	
	protected void resetModules()
	{
		for ( int i=0; i<projectInfo.modules.size(); i++ )
		{
			Module module = projectInfo.modules.get(i);
			
			if (module != null)
			{		
				module.reset();
			}
		}		
	}

	@SuppressWarnings("unused")
	private void enumModules() throws InvalidRegistryObjectException
	{
		StringBuffer buffer = new StringBuffer();
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IConfigurationElement[] extensions = reg.getConfigurationElementsFor("com.geofx.gms.module");
		for (int i = 0; i < extensions.length; i++)
		{
			IConfigurationElement element = extensions[i];
			buffer.append(element.getAttribute("name"));
			buffer.append(" : ");
			String classe = "unknown";
			if (element.getAttribute("class") != null)
			{
				classe = element.getAttribute("class");
			}
			
			buffer.append(classe);
			
			buffer.append(" : ");

			String id = "unknown";
			if (element.getAttribute("id") != null)
			{
				id = element.getAttribute("id");
			}
			
			buffer.append(id);

			buffer.append("\n");
		
			System.out.println(buffer);

			Module module = createModule(element);
		}
	}
	
	protected Module createModule( IConfigurationElement element )
	{
		Module module = null;

		try
		{
			module = (Module) element.createExecutableExtension("class");
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}

		return module;
	}
	
	public void  getViews(ArrayList<ViewInfo> viewsInfo)
	{
		for ( int i=0; i<projectInfo.modules.size(); i++ )
		{
			Module module = projectInfo.modules.get(i);
			
			ViewInfo viewInfo = module.getViewInfo();
			if (viewInfo != null)
			{
				viewsInfo.add(viewInfo);
				viewModules.add(module);
			}
		}
		
		viewsCreated();
	}
	
	public void viewsCreated()
	{
		for ( int i=0; i<viewModules.size(); i++ )
		{
			viewModules.get(i).initView();
		}
	}

	/**
	 * Search the current datasets for the Dataset with the specified ID
	 * @param hRef
	 */
	public void resolveDataset( Input input )
	{
		for ( int i=0; i<projectInfo.modules.size(); i++ )
		{
			input.setModule(projectInfo.modules.get(i));
			input.setDataset(input.getModule().getDataset(input.getHRef()));
			if (input.getDataset() != null)
				return;
		}
	}

	/**
	 * Search the manifest for the specified ID
	 * 
	 * @param hRef
	 */
	public String resolveManifestItem(String idRef)
	{
		ManifestEntry entry = projectInfo.getManifest().get(idRef);
		
		return entry != null ? entry.href : "";
	}
	
	/**
	 * Parse out the animation times and controls
	 * 
	 * @param attributes
	 */
	protected void parseSequenceElement(Attributes attributes)
	{
		for (TimingAttributes current : TimingAttributes.values())
		{
			String	value = attributes.getValue("", current.toString());

			if (value != null && value.length() != 0)
			{
			    timingAttribs[current.ordinal()] = value;

				switch (current)
				{
					case begin:
					    begin = convertToSeconds(value);
						break;
					case end:
					    end = convertToSeconds(value);
						break;
					case step:
					    step = convertToSeconds(value);
						break;
					case dur:
					    dur = convertToSeconds(value);
						break;
					case rate:
					    rate = convertToSeconds(value);
						break;					
					case repeat:
						repeat = value.equals("true");
						break;
					case incremental:
						incremental = value.equals("true");
						break;
				}
			}
		}
	}
	
	/**
	 * Convert the time-value string to total time in seconds.  
	 * 
	 * Note that we currently don't support begin/end in wallclock terms
	 * 
	 * @param value
	 * @return
	 */
	protected double convertToSeconds(String value)
	{
		double numSec = 0;
		for (TimeUnits current : TimeUnits.values())
		{
			int index = value.indexOf(current.toString());
			if (index > 0)
			{
				numSec += Double.parseDouble(value.substring(0, index )) * timeUnits[current.ordinal()];
				value = value.substring(index+1);
			}
		}
		
		if (value.length() != 0)
			numSec += Double.parseDouble(value);
			
		return numSec;
	}
	
	//============ Animation section ==================================
	
	/**
	 * This initiates the model process
	 * @return
	 */
	public void restart()
	{
		resetModules();
		
		modelClock.restart();
	}
	
	/**
	 * This initiates the model process
	 * @return
	 */
	public void resume()
	{
		modelClock.resume();
	}
	/**
	 * This pauses the model process
	 * @return
	 */
	public void pause()
	{
		modelClock.pause();	
	}

	/**
	 * This returns whether the clock is running or not.
	 * @return
	 */
	public void stop()
	{
		modelClock.stop();
	}

	public void singleStep()
	{
		singleStep = true;		
	}
	
	public ClockState getClockState()
	{
		return modelClock.getState();
	}
	
	public ProjectInfo getProjectInfo()
	{
		return projectInfo;
	}


	protected void renderModels( double alpha )
	{
		System.out.println("renderModels");
	}
	
	protected void updateModels()
	{		
		if ((modelClock.getState() == ClockState.PAUSED  && !singleStep) || modelClock.getState() == ClockState.STOPPED)
			return;
		
		if (singleStep)
		{
			modelClock.resume();
		}

		//System.out.println("updateModels: " );

		modelClock.time();

		glComposites.clear();
		for ( int i=0; i<viewModules.size(); i++ )
		{
			viewModules.get(i).update();
		}
		
		if (singleStep)
		{
			modelClock.pause();
			singleStep = false;
		}
		
		//System.out.println(String.format("time: %9.3f", modelClock.peekTime()));
	}

	protected boolean isUniqueView( Module module )
	{
		GLComposite glC = module.getViewInfo().getComposite();
		if (glComposites.contains(glC) == false)
		{
			glComposites.add(glC);
			return true;
		}
		
		return false;
	}
	
	public String[] getTimingAttribs()
	{
		return timingAttribs;
	}

	public void begin()
	{
		System.out.println("Animator: begin event");		
	}

	public void end()
	{
		System.out.println("Animator: end event");		
	}

	public void repeat()
	{
		System.out.println("Animator: repeat event");		
		
	}

	public void timingEvent(float fraction)
	{
        this.updateModels();

		//System.out.println(String.format("Animator: elapsed: %8.4f  timingEvent: %9.4f", animator.getTotalElapsedTime()/1000.0, fraction));
	}

	public void dispose()
	{
		//animator.stop();
		nanoTimer.stop();
	}

	public void timingEvent()
	{
		updateModels();
	}

}

/*******************************************************************************
 * Copyright (c) 2008 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 ****************************************************************************/

package com.geofx.science.tempwave;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import com.geofx.gms.datasets.Grid;
import com.geofx.gms.model.Module;
import com.geofx.opengl.util.Constants;

public class TempWaveModule extends Module
{
	private static final double OMEGA_DAY = Constants.TWO_PI / (24.0 * 3600.0); // seconds in a day, so temp cycles in one day
	private static final double OMEGA_YEAR = Constants.TWO_PI / (24.0 * 3600.0 * 365.0); // seconds in a year, so temp cycles in one year
	
	protected double		curTime;  // Current time, in seconds
	
	// the depths input array, supplied as a dataset
	protected double[] 		depths;
	protected int			nDepths;
	
	// public fields
	public double 			diffusivity;
	public double    		dailyAmplitude;
	public double    		annualAmplitude;
	public double    		meanTemp;
	
	// calculated values
	protected double    	airTemperature = 0.0;			// today's air temperature
	protected double    	diurnalAmplitude = 0.0;			// today's temperature amplitude, i.e. (max-min)/2
	protected double    	damping        = Math.sqrt(2.0 * diffusivity / OMEGA_DAY);
	
	public TempwaveData 	tempwaveData;
	
	public TempWaveModule()
	{
		
	}

	public void updateSim ()
	{
		curTime = clock.peekTime();
		
		// System.out.println("TempWaveModule:updateSim: " + String.format("curTime: %8.0f", curTime));
		
		// calculate today's air temperature
		updateAirTemp();
		
		// then all the depths for this time
		updateAllDepths();
	}
	
	// update the air temp based on diurnal and annual cycle. Note returns delta from mean temperature
	private void updateAirTemp ()
	{
		double diurnal_proportion = 0.3333;
		double annual_proportion = 0.05;
		
		diurnalAmplitude = dailyAmplitude * ((1.0-diurnal_proportion/2.0) + Math.random() * diurnal_proportion);
		airTemperature   = meanTemp + ((1.0-annual_proportion/2.0) + Math.random() * annual_proportion) * 
									annualAmplitude * Math.cos(curTime * OMEGA_YEAR);
	}

	/**	 
	 * Update the temperature for each depth at the current time* 
	 *
	 */
	private void updateAllDepths ()
	{
		tempwaveData.time = curTime;

		for ( int j=0; j<nDepths; j++ )
		{
			double dampingDepth    = depths[j] / damping;
			double dampedAmplitude = diurnalAmplitude * Math.exp(-depths[j] / damping);
			tempwaveData.temps[j] = airTemperature + dampedAmplitude * Math.cos(curTime * OMEGA_DAY - dampingDepth);
		}		
		
		tempwaveData.setLastModified(clock.peekTime());

		//dumpData();
	}

	@Override
	public void reset()
	{
		tempwaveData.reset();
		tempwaveData.setLastModified(clock.peekTime());
	}

	@Override
	public boolean update()
	{
		if (super.update())
		{
			// System.out.println("Updating TempWaveModule");
			updateSim();
			return true;
		}
		else
			return false;
	}

	@Override
	public void initDatasets()
	{
		super.initDatasets();
		
		// find the depths dataset
		Grid gridD = (Grid) inputs.get(0).getDataset();
		if (gridD == null)
		{
			System.err.println("Unable to load input dataset: " + inputs.get(0).getHRef());
			return;
		}
		
		Field fieldPtr;
		try
		{
			fieldPtr = getClass().getDeclaredField("depths");
			fieldPtr.set(this, gridD.getArray());
			nDepths = Array.getLength(gridD.getArray());
			
			tempwaveData = new TempwaveData(nDepths);
			
			// set the clock to the far future so the first pass gets updated
			tempwaveData.setLastModified(0.0);

			outputs.get(0).setDataset(tempwaveData);

			damping = Math.sqrt(2.0 * diffusivity / OMEGA_DAY);
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
	
	@SuppressWarnings("unused")
	private void dumpData()
	{
		System.out.print("Time:" + String.format("%6.1f", tempwaveData.time/3600.0) + " air: " + String.format("%6.1f",airTemperature));
		for ( int j=0; j<nDepths; j++ )
		{
			System.out.print(String.format("  %6.2f",tempwaveData.temps[j]));
		}
		System.out.println();
	}


}

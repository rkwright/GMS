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

package com.geofx.gms.viewers;

import java.lang.reflect.Array;

import com.geofx.gms.datasets.Dataset;
import com.geofx.gms.datasets.Grid;
import com.geofx.gms.model.Module;
import com.geofx.gms.model.Output;
import com.geofx.opengl.util.Axis.AxisPos;

/**
 * @author riwright
 *
 */
public class LineGraphModule extends Module
{
	private static final String VIEWLABEL = "Line Graph";
	private static final String VIEWNAME  = "com.geofx.gms.viewers.LineGraph";
	
	protected Grid 			dummy = new Grid();
	protected Dataset		xDataset;
	protected Dataset		yDataset;
	protected Object		array;
	protected boolean		init = false;
	
	// public strings that are filled in from the project file at runtime
	public String			viewPort;
	public int				frameColor;
	public int				symbolColor;
	public String 			leftAxis;
	public String 			rightAxis;
	public String 			topAxis;
	public String 			bottomAxis;
	
	protected LineGraph		lineGraph;
	
	/**
	 * @see com.geofx.gms.model.Module#reset()
	 */
	@Override
	public void reset()
	{
		dummy.setLastModified(clock.peekTime());
	}

	@Override
	public void init()
	{
	}
	
	/**
	 * @see com.geofx.gms.model.Module#update()
	 */
	@Override
	public boolean update()
	{
		if (super.update())
		{
			//System.out.println("Updating LineGraphModule");
			repaintGraph();
			dummy.setLastModified(clock.peekTime());

			return true;
		}
		else
			return false;
	}

	private void repaintGraph()
	{
		if (init == false && xDataset.getObject() != null && Array.getLength(xDataset.getObject()) > 0)
			//if (initView() == false)
				return;

		viewInfo.getComposite().render();
	}

	@Override
	public void initDatasets()
	{
		super.initDatasets();
		
		xDataset = (Dataset) inputs.get(0).getDataset();
		yDataset = (Dataset) inputs.get(1).getDataset();
				
		// create a dummy output so we can track the mod time
		outputs.add(new Output());
		outputs.get(0).setDataset(dummy);
		dummy.setLastModified(0.0);
	}

	public boolean initView()
	{
		lineGraph = (LineGraph) viewInfo.getView();
		if (lineGraph!= null)
		{
			lineGraph.setXDataset(xDataset);
			lineGraph.setYDataset(yDataset);
			
			lineGraph.setViewPort( viewPort);
			lineGraph.setAxis(AxisPos.LEFT, leftAxis);
			lineGraph.setAxis(AxisPos.RIGHT, rightAxis);
			lineGraph.setAxis(AxisPos.TOP, topAxis);
			lineGraph.setAxis(AxisPos.BOTTOM, bottomAxis);
			
			lineGraph.setFrameColor(frameColor);
			
			init = true;
		}
		
		return init;
	}
	
	/**
	 * Subclasses that are view modules override the super class, which returns null.
	 * 
	 * @return
	 */
	public ViewInfo getViewInfo()
	{
		if (viewInfo == null)
			viewInfo = new ViewInfo( this, tab, VIEWNAME, VIEWLABEL );
		return viewInfo;
	}
}

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

import java.util.ArrayList;

import com.geofx.gms.datasets.Grid;
import com.geofx.gms.datasets.RollingGrid;
import com.geofx.gms.model.Module;
import com.geofx.gms.model.Output;
import com.geofx.opengl.util.Axis.AxisPos;

/**
 * @author riwright
 *
 */
public class PolylineGraphModule extends Module
{
	private static final String VIEWLABEL = "Polyline Graph";
	private static final String VIEWNAME  = "com.geofx.gms.viewers.PolylineGraph";
	
	protected Grid 				dummy = new Grid();
	protected RollingGrid		rollingGrid;
	protected ArrayList<Object>	array;
	protected boolean			init = false;
	
	// public strings that are filled in from the project file at runtime
	public String			viewPort;
	public int				frameColor;
	public int[]			symbolColors;
	public String 			leftAxis;
	public String 			rightAxis;
	public String 			topAxis;
	public String 			bottomAxis;
	private double 			span;
	
	/**
	 * @see com.geofx.gms.model.Module#reset()
	 */
	@Override
	public void reset()
	{
		dummy.setLastModified(clock.peekTime());
	}

	/**
	 * @see com.geofx.gms.model.Module#update()
	 */
	@Override
	public boolean update()
	{
		if (super.update())
		{
			//System.out.println("Updating PolylineGraphModule");
			repaintGraph();
			dummy.setLastModified(clock.peekTime());
			return true;
		}
		else
			return false;
	}

	private void repaintGraph()
	{
		if (init == false && array.size() > 0)
				return;

		viewInfo.getComposite().render();
	}

	@Override
	public void initDatasets()
	{
		super.initDatasets();
		
		rollingGrid = (RollingGrid) inputs.get(0).getDataset();
		array = rollingGrid.getArray();
				
		// create a dummy output so we can track the mod time
		outputs.add(new Output());
		outputs.get(0).setDataset(dummy);
		dummy.setLastModified(0.0);
		
		// compute the span of the graph
		span = clock.getScale() * (rollingGrid.getSize() - 1);
	}

	public boolean initView()
	{
		PolylineGraph graph = (PolylineGraph) viewInfo.getView();
		if (graph != null)
		{
			graph.setArray(array);
			graph.setSpan(span);
		
			graph.setViewPort( viewPort);
			
			graph.setAxis(AxisPos.LEFT, leftAxis);
			graph.setAxis(AxisPos.RIGHT, rightAxis);
			graph.setAxis(AxisPos.TOP, topAxis);
			graph.setAxis(AxisPos.BOTTOM, bottomAxis);
			
			graph.setFrameColor(frameColor);
			graph.setColors(symbolColors);
			init = true;
		}
		
		return init;
	}
	
	/**
	 * Subclasses that are view modules MUST over-ride the super class, which returns null.
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

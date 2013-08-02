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

import com.geofx.gms.model.Module;
import com.geofx.opengl.view.GLComposite;
import com.geofx.opengl.view.IGLView;

/**
 * @author riwright
 *
 */
public class ViewInfo
{
	protected String		viewName;

	protected IGLView	 	view = null;

	protected String		label;
	protected GLComposite 	composite = null;

	protected Module		module = null;
	protected String		tab;
	
	public ViewInfo ( Module module, String tab, String viewName, String label )
	{
		this.module = module;
		this.tab = tab;
		this.viewName = viewName;
		this.label = label;
	}

	public String getViewName()
	{
		return viewName;
	}

	public void setViewName(String viewName)
	{
		this.viewName = viewName;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public GLComposite getComposite()
	{
		return composite;
	}

	public void setComposite(GLComposite composite)
	{
		this.composite = composite;
	}

	public Module getModule()
	{
		return module;
	}

	public void setModule(Module module)
	{
		this.module = module;
	}

	public String getTab()
	{
		return tab;
	}

	public void setTabNum(String tab)
	{
		this.tab = tab;
	}

	public IGLView getView()
	{
		return view;
	}

	public void setView(IGLView view)
	{
		this.view = view;
	}

}

/*******************************************************************************
  * Copyright (c) 2008 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 *******************************************************************************/
package com.geofx.gms.editor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.geofx.gms.model.ManifestEntry;
import com.geofx.gms.model.ProjectInfo;

public class DatasetsFileInput implements IStructuredContentProvider
{
	private ProjectInfo projectInfo;

	public DatasetsFileInput(ProjectInfo projectInfo)
	{
		this.projectInfo = projectInfo;
	}

	public Object[] getElements(Object inputElement)
	{
		HashMap<String, ManifestEntry> manifest = projectInfo.getManifest();
		
		ManifestEntry[] manifests = new ManifestEntry[manifest.size()];
		
		try
		{
			Set<?> set = manifest.entrySet();
			Iterator<?> iter = set.iterator();

			int i = 0;
			while (iter.hasNext())
			{
				Map.Entry me = (Map.Entry) iter.next();

				ManifestEntry entry = (ManifestEntry) me.getValue();

				manifests[i++] = entry;
			}
		}
		catch (Exception e )
		{
			e.printStackTrace();
		}
		return manifests;
	}

	public void dispose()
	{
		// TODO Auto-generated method stub
		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}


}
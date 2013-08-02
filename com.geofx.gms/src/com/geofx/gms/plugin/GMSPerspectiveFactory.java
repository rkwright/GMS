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

package com.geofx.gms.plugin;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class GMSPerspectiveFactory implements IPerspectiveFactory
{

	@Override
	public void createInitialLayout(IPageLayout layout)
	{
		// Get the editor area.
		 String editorArea = layout.getEditorArea();

		 // Top left: Project Explorer view and Bookmarks view placeholder
		 IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.25f, editorArea);
		 topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);

		 // Bottom: Outline view 
		 IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.80f, editorArea);
		 bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
		 bottom.addView( "org.eclipse.ui.console.ConsoleView");
		 
		// Right: Outline view 
		 IFolderLayout topRight = layout.createFolder("topRight", IPageLayout.RIGHT, 0.75f, editorArea);
		 topRight.addView(IPageLayout.ID_OUTLINE);
	}

}

package com.geofx.opengl.scene;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.geofx.opengl.plugin.Activator;
import com.geofx.opengl.view.GLComposite;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class SceneView extends ViewPart
{

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.geofx.opengl.scene.views.GLScene";

	private GLComposite 		glComposite;
	private GLScene				scene;
	private String 				sceneName;

	/**
	 * The constructor.
	 */
	public SceneView()
	{
		Activator.setView( this );
	}

	public void createPartControl( Composite parent )
	{		
		sceneName = Activator.getSceneName();

		glComposite = new GLComposite(parent); 
		
		this.scene = Activator.constructScene(sceneName, glComposite);
		
		glComposite.addView(sceneName);
		
		// we need to explicitly request the focus or we never get it
		glComposite.getFrame().requestFocus();		


		//this.scene.render();
		
	}

	public void dispose()
	{
		System.out.println("SceneView.dispose called");
		this.scene.dispose();
	}
	
	/**
	 * A public method to update the view.  
	 *
	 */
	public void updateView()
	{
		if (!sceneName.equals(Activator.getSceneName()))
		{
			sceneName = Activator.getSceneName();
			
			System.out.println("SceneView - disposing old scene: "+ scene.getLabel());
			this.scene.dispose();
			
			System.out.println("SceneView - constructing new scene: " + sceneName);
			this.scene = Activator.constructScene(sceneName, glComposite);
			
			glComposite.addView(sceneName);		
		}
		
		this.scene.render();
	}


	/**
	 * We don't need this but we have to implement it
	 */
	@Override
	public void setFocus() 
	{
		System.out.println("SceneView - requestFocus");
	}
}
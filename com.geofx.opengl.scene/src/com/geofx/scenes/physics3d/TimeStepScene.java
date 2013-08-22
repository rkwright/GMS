/*******************************************************************************
 * 
 * 
 * Code from the JOGL Demos:  https://jogl-demos.dev.java.net/
 * 
 * Contributors:
 *    
 *     Ric Wright - May 2008 - Ported to Eclipse 3.2, minor tweaks
 *******************************************************************************/
package com.geofx.scenes.physics3d;

import java.awt.event.KeyEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;

import com.geofx.opengl.scene.GLScene;
import com.geofx.opengl.view.GLComposite;

public class TimeStepScene extends GLScene 
{
	float[]			sphere_material = { 1.0f, 1.0f, 0.0f, 1.0f };

	public TimeStepScene( GLComposite parent )
	{
		super(parent);

		System.out.println("TimeStepScene - constructor");

		glComposite.getGrip().setOffsets(0.0f, 0.0f, -3.0f);
		glComposite.getGrip().setRotation(45.0f, -30.0f, 0.0f);
	}

	// a default constructor used by the ClassInfo enumerator
	public TimeStepScene()
	{
		super();
	}


	public void init ( GLAutoDrawable drawable ) 
	{
		super.init(drawable);	
		
		System.out.println("TimeStepScene - init");

		final GL gl = drawable.getGL();	

		float[] 	LightDiffuse =	 { 1.0f, 1.0f, 1.0f, 1.0f };
		float[] 	LightAmbient =	 { 0.5f, 0.5f, 0.5f, 1.0f };
		float[] 	LightPosition =	 { 1.0f, 1.0f, 1.0f, 0.0f };
		float[]		mat_specular = { 1.0f, 1.0f, 1.0f, 1.0f };
		float[]     mat_shininess = { 50.0f };	
        
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, LightAmbient, 0);						
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, LightDiffuse, 0);				

		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, mat_specular, 0);
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SHININESS, mat_shininess, 0);

		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, LightPosition, 0);			

		gl.glEnable(GL.GL_DEPTH_TEST);
	    gl.glDepthFunc(GL.GL_LESS);    
	
	    glComposite.initAnimator(drawable, 60);

	    glComposite.setLightingEnabled(true);	              
	}

	public void display(GLAutoDrawable drawable) 
	{
		super.display(drawable);
	
		//System.out.println("TimeStepScene - display");
		
		GL gl = drawable.getGL();

		try
		{
			glComposite.updateLighting(gl);
		
			glComposite.drawAxes(gl);

			timeStep(gl);
           
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void  enableLighting( GL gl )
	{
	    gl.glEnable(GL.GL_LIGHT0);
	}
	
	public String getDescription()
	{
		return "Demo of the time step algorithm";
	}

	public String getLabel()
	{
		return "Time Step Demo";
	}

	// allows subclasses to get key events and do something interesting...
	protected boolean handleKeyEvent( KeyEvent e )
	{
		boolean	handled = true;
		switch (e.getKeyChar())
	
		{
			case 'r':
				reset();
				break;
			default:
				handled = false;
		}
		
		return handled;
	}

	//======================= Time Step Section ========================================
	
	private static final double 	TIME_CLAMP = 0.250;
	private static final double 	TIME_STEP = 0.05;
	
	State current = new State(1.0,1.0);
	
	State previous = new State();

	double t = 0;
	double dt = TIME_STEP;
	
	double currentTime = time();
	double accumulator = 0;
	double k = 10.0;
	double b = 0.1;

	private class State
	{
		double x;
		double v;
		
		public State ()
		{
			this.x = 0.0;
			this.v = 0.0;
		}

		public State (double x, double v)
		{
			this.x = x;
			this.v = v;
		}
	};

	private class Derivative
	{
		double dx;
		double dv;
		
		public Derivative ()
		{
			this.dx = 0.0;
			this.dv = 0.0;
		}
	};

	State interpolate( State previous, State current, double alpha)
	{
		State state = new State();
		state.x = current.x*alpha + previous.x*(1-alpha);
		state.v = current.v*alpha + previous.v*(1-alpha);
		return state;
	}

	double acceleration( State state, double t)
	{
		return -k*state.x - b*state.v*t;
	}

	Derivative evaluate( State initial, double t)
	{
		Derivative output = new Derivative();
		output.dx = initial.v;
		output.dv = acceleration(initial, t);
		return output;
	}

	Derivative evaluate( State initial, double t, double dt,  Derivative d)
	{
		State state = new State();
		state.x = initial.x + d.dx*dt;
		state.v = initial.v + d.dv*dt;
		Derivative output = new Derivative();
		output.dx = state.v;
		output.dv = acceleration(state, t+dt);
		return output;
	}

	protected State integrate(State state, double t, double dt)
	{
		Derivative a = evaluate(state, t);
		Derivative b = evaluate(state, t, dt*0.5f, a);
		Derivative c = evaluate(state, t, dt*0.5f, b);
		Derivative d = evaluate(state, t, dt, c);
		
		double dxdt = 1.0/6.0 * (a.dx + 2.0*(b.dx + c.dx) + d.dx);
		double dvdt = 1.0/6.0 * (a.dv + 2.0*(b.dv + c.dv) + d.dv);
		
		state.x = state.x + dxdt*dt;
		state.v = state.v + dvdt*dt;
		
		return state;
	}

    static long	start     = 0;

	protected double time()
	{
	    if (start == 0)
	    {
	        start = System.nanoTime();
	        return 0.0;
	    }
	    
	    return (double) (System.nanoTime() - start) / 1e09;
	}
	
	private void reset()
	{
		start = 0;
		t = 0;
		accumulator = 0;
		current.x = 2.0;
		current.v = 1.0;
		currentTime = time();
	}

	protected int timeStep( GL gl )
	{	
			
		double newTime = time();
		double deltaTime = newTime - currentTime;
		currentTime = newTime;

		if (deltaTime > TIME_CLAMP)
			deltaTime = TIME_CLAMP;

		accumulator += deltaTime;

		while (accumulator >= dt)
		{
			accumulator -= dt;
			
			previous.x = current.x;
			previous.v = current.v;
			
			integrate(current, t, dt);
			t += dt;
			
			System.out.println("Wowser");
			//System.err.println("integrate: time: " + String.format("%6.2f", t) + "  currentX: " + 
			//		String.format("%6.2f", current.x) + "   currentV: " + String.format("%6.2f", current.v));
		}

		interpolate(previous, current, accumulator / dt);	
	
		drawSphere( gl, current );
		
		//System.out.println("t: " + t + "  currentT: " + currentTime + "  accum: " + accumulator + "  deltaT: " +  deltaTime + "x: " + current.x + "v: " + current.v);
		
		return 0;
	}

	private void drawSphere(GL gl, State current)
	{
		glComposite.getGLU().gluQuadricDrawStyle(glComposite.getQuadric(), GLU.GLU_FILL);
		glComposite.getGLU().gluQuadricNormals(glComposite.getQuadric(), GLU.GLU_SMOOTH);

		gl.glColor4f(1f, 1f, 0.0f, 1f);

		gl.glPushAttrib(GL.GL_TRANSFORM_BIT);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glEnable(GL.GL_NORMALIZE);

		gl.glTranslated(current.x, 0.0, 0.0);

		gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, sphere_material, 0);

		glComposite.getGLU().gluSphere(glComposite.getQuadric(), 0.1f, 20, 20);
		
		gl.glPopMatrix();
		gl.glPopAttrib();
		
	}
}

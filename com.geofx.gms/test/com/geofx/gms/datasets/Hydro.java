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

package com.geofx.gms.datasets;

import java.nio.ByteBuffer;


public class Hydro implements IGMSSerialize
{
	public static final int  	SNOWPACK = 0;     	//  snow pack storage      
	public static final int 	CHANNEL  = 1;       //  channel storage      
	public static final int 	VADOSE   = 2;       //  vadose zone            
	public static final int 	GW       = 3;       //  ground water         

	public static final int 	PRECIP   = 0;		//  Total precip         		
	public static final int 	TA       = 1;  		//  air temp             		
	public static final int 	TW       = 2;       //  wet bulb temp        		

	public static final int 	QRAW     = 0;		//  Observed runoff      		
	public static final int 	QTIME    = 1;       //  time of observed Q   		
	public static final int 	STAGEHT  = 2;       //  stage height at Qt   		
	public static final int 	QOBS     = 3;       //  interpolated Q       		

	public static final int 	SNOWFALL = 0;		//  Snowfall             		
	public static final int 	RAIN     = 1;       //  Rainfall             		  
	public static final int 	MELT     = 2;       //  Snowmelt             		
	public static final int 	ETA      = 3;     	//  ET from non-sat surfaces	  
	public static final int 	ETS      = 4;       //  ET - from sat surfaces		   
	public static final int 	QAN      = 5;       //  Qan                  		   
	public static final int 	QAB      = 6;      	//  Qab                  		   
	public static final int 	QB       = 7;       //  Qb                   		  
	public static final int 	QS       = 8;       //  Qs                   		  
	public static final int 	SRO      = 9;       //  Surface Runoff       		  
	public static final int 	DPPT     = 10;      //  direct ppt into surf. res. 	
	public static final int 	TIME     = 11;      //  Time at each step    		   
	public static final int 	ETT      = 12;      //  ETt                  		   
	public static final int 	QA       = 13;     	//  Qa                   		   
	public static final int 	QT       = 14;      //  Qt  
	
	private static final int sizeofFields = 18 * 8;
	   
	
	public double		snowfall;		//  snowfall (mm WE) during current time-step	
	public double		rain;     		//  rainfall (mm) during current time-step		
	public double		melt;     		//  snowmelt (mm) during current time-step		
	public double		Ta;     		//  dry-bulb air temp at screen height          
	public double		Tw;     		//  wet-bulb air temp at screen height          
	public double		Rn;     		//  net radiation flux to surface	  			

	public double		ET;     		//  ET from surface								

	public double		Qa;     		//  net flow from vadose to channel				
	public double		Qan;     		//  net flow from vadose to channel				
	public double		Qab;     		//  net flow from vadose to GW					
	public double		Qb;     		//  net flow from GW to "output"				

	public double		Qs;     		//  net flow from channel to "output", per day  

	public double		Qst;     		//  estimated net flow from channel to "output" per
										//  interation for each WB element 
							 
	public double		Qsr;     		//  estimated. net flow from channel to "output"
										//  for each rout-step 
							 
	public double   	snowpack;    	//  current storage in snow pack (mm WE)  		  
	public double   	channel;   		//  current storage  at channel (mm)     		
	public double   	vadose;    		//  vadose zone storage (mm)          			  
	public double		gw;	          	//  ground (phreatic) water storage (mm) 		  

	protected double   	resTemp; 	   	//  tmp storage variable     					
	protected double   	tmpFlux;	   	//  tmp flux variable   
	
//	DoubleBuffer 		outBuffer;
//	byte[]				byteArray;

	public Hydro()
	{
		snowfall = Math.random();	
		rain = Math.random();		
		melt = Math.random();   		
		Ta = Math.random();            
		Tw = Math.random();            
		Rn = Math.random();     	  			

		ET = Math.random(); 							

		Qa = Math.random();     				
		Qan = Math.random();  			
		Qab = Math.random();  			
		Qb = Math.random();    				

		Qs = Math.random();      

		Qst = Math.random();    
								 
		Qsr = Math.random();     	
								 
		snowpack = Math.random();  	  
		channel = Math.random();  		
		vadose = Math.random();    	      			  
		gw = Math.random();	   
		
	//	byteArray = new byte[sizeofFields];
	//	outBuffer = ByteBuffer.wrap(byteArray).asDoubleBuffer();

	}

	public void serializeOut ( ByteBuffer outBuffer )
	{
		outBuffer.putDouble(snowfall);			
		outBuffer.putDouble(rain);			
		outBuffer.putDouble(melt);			
		outBuffer.putDouble(Ta);			
		outBuffer.putDouble(Tw);			
		outBuffer.putDouble(Rn);			
		outBuffer.putDouble(ET);			
		outBuffer.putDouble(Qa);			
		outBuffer.putDouble(Qan);			
		outBuffer.putDouble(Qab);			
		outBuffer.putDouble(Qb);			
		outBuffer.putDouble(Qs);			
		outBuffer.putDouble(Qst);			
		outBuffer.putDouble(Qsr);			
		outBuffer.putDouble(snowpack);			
		outBuffer.putDouble(channel);			
		outBuffer.putDouble(vadose);			
		outBuffer.putDouble(gw);	
	}

	public void serializeIn ( ByteBuffer inBuffer )
	{
		snowfall = inBuffer.getDouble();			
		rain = inBuffer.getDouble();			
		melt = inBuffer.getDouble();			
		Ta = inBuffer.getDouble();			
		Tw = inBuffer.getDouble();			
		Rn = inBuffer.getDouble();			
		ET = inBuffer.getDouble();			
		Qa = inBuffer.getDouble();			
		Qan = inBuffer.getDouble();			
		Qab = inBuffer.getDouble();			
		Qb = inBuffer.getDouble();			
		Qs = inBuffer.getDouble();			
		Qst = inBuffer.getDouble();			
		Qsr = inBuffer.getDouble();			
		snowpack = inBuffer.getDouble();			
		channel = inBuffer.getDouble();			
		vadose = inBuffer.getDouble();			
		gw = inBuffer.getDouble();	
	}

	public int serializeSize()
	{
		return sizeofFields;
	}

	public boolean compareTo( Object obj )
	{
		Hydro hydro = (Hydro)obj;
		
		if (snowfall == hydro.snowfall &&			
		    rain == hydro.rain &&		
		    melt == hydro.melt &&		
	     	Ta == hydro.Ta &&	
	    	Tw == hydro.Tw &&		
	    	Rn == hydro.Rn &&		
	    	ET == hydro.ET &&		
	    	Qa == hydro.Qa &&		
	    	Qan == hydro.Qan &&			
	    	Qab == hydro.Qab &&		
	    	Qb == hydro.Qb &&
	    	Qs == hydro.Qs &&	
	    	Qst == hydro.Qst &&			
	    	Qsr == hydro.Qsr &&		
	    	snowpack == hydro.snowpack &&			
	    	channel == hydro.channel &&		
	    	vadose == hydro.vadose &&	
	    	gw == hydro.gw )
				return true;
			else
				return false;
	}
}

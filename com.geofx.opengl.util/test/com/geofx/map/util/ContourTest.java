/*******************************************************************************
 * Copyright (c) 2009 Ric Wright All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors: Ric Wright - initial implementation
 ****************************************************************************/
package com.geofx.map.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StreamTokenizer;
import java.util.Vector;

import junit.framework.TestCase;

import org.eclipse.core.runtime.Path;

/**
 * @author riwright
 *
 */
public class ContourTest extends TestCase
{
	static String 			dataFile = "./test/data/GFDATA.DAT";
	Contour					contour;
	Vector<ContourVector>	contourVector;
	
	double[][]		array = {
								{ 1, 3, 1 },
								{ 0, 5, 1 },
								{ 1, 1, 3 },
							};
	
	double 			contInterval = 2.0;

	public ContourTest(String name)
	{
		super(name);
	}

	/**
	 * @throws java.lang.Exception
	 */
	protected void setUp() throws Exception
	{
		contour = new Contour();
	}

	/**
	 * @throws java.lang.Exception
	 */
	protected void tearDown() throws Exception
	{
	}

	public void testThreadContours()
	{
		contourVector = contour.ThreadContours(array, contInterval);
	}
	
	public void testThreadContoursLarge()
	{
		array = parseArrayFromFile();		

		contInterval = 100.0;
		contourVector = contour.ThreadContours(array, contInterval);

	}

	private double[][] parseArrayFromFile()
	{
		Path path = new Path(dataFile);
		
		FileInputStream stream;  
		
		StreamTokenizer st;
		double[][]	ray = null;

		try
		{
			stream = new FileInputStream(path.toPortableString());
			BufferedReader br = new BufferedReader(new FileReader(dataFile));
		    st = new StreamTokenizer(br);

			System.out.println("Stream = " + stream);
			int cols = -1;
			int rows = -1;
			int i = 0;
			int j = 0;
			
		    while(st.nextToken() != StreamTokenizer.TT_EOF) 
		    {
		        String s;
		        switch(st.ttype) 
		        {
		          case StreamTokenizer.TT_EOL:
		            s = new String("EOL");
		            break;
		          case StreamTokenizer.TT_NUMBER:
		            s = Double.toString(st.nval);
		            if (rows == -1)
		            	rows = (int) Math.round(st.nval);
		            else if (cols == -1)
		            {
		            	cols = (int) Math.round(st.nval);
		            	
		            	ray = new double[rows][cols];
		            }
		            else
		            {
		            	ray[i][j++] = st.nval;
		            	if (j >= cols)
		            	{
		            		j= 0;
		            		i++;
		            	}
		            	
		            }
		            	
		            break;
		          case StreamTokenizer.TT_WORD:
		            s = st.sval; // Already a String
		            break;
		          default: // single character in ttype
		            s = String.valueOf((char)st.ttype);
		        }	
		    }
		    
			System.out.println("Parse complete");
		}
		catch (FileNotFoundException e)
		{
			assertTrue(false);
			e.printStackTrace();
		}		
		catch (Exception e)
		{
			assertTrue(false);
			e.printStackTrace();
		}
		
		return ray;
	}
}

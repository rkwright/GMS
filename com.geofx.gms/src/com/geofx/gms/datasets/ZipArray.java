/*******************************************************************************
 * Copyright (c) 2008-09 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 *******************************************************************************/

package com.geofx.gms.datasets;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Stack;
import java.util.Vector;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.vecmath.Matrix4d;

import org.eclipse.core.runtime.IPath;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.geofx.gms.datasets.ClassUtil.ClassType;
import com.geofx.gms.datasets.Dataset.DatasetType;
import com.geofx.gms.model.XMLModel;
import com.geofx.gms.plugin.Constants;

/**
 * This class provides a seamless method for serializing Java arrays to a zipfile.
 * The structure of the file is as follows
 * <p>1. PK zipinfo</p> 
 * <p>2. Data set info</p>
 * <p>	- This is an XML file that has the form:</p>
 * <code>
 * 		&lt;dataset xmlns= "http://www.geofx.com/gms/2009" gms:type="float"&gt;<br/>
 * 		&nbsp;	&lt;dim&gt;size_of_dimenions_0&lt;/dim&gt;<br/>
 * 		&nbsp;	&lt;dim&gt;size_of_dimenions_1&lt;/dim&gt;<br/>
 * 		&nbsp;	... <br/>
 * 		&lt;/dataset&gt;<br/>
 * </code>
 * <p>	- the number of dimensions is not limited</p>
 * <p>	- gms:type can be one of Java native types or a class.  If it a class, then the 
 * 	  fields are serialized using reflection, so they must be public. Alternatively,
 * 	  the class can implement the GMSSerialize interface, which has a method, serializeObject, 
 *    which this class will call to serialize each object as it is passed in.</p>
 * </p>3. The binary data.</p>
 * <p>Note that the the binary data is stored/retrieved via Java NIO buffer methods so Endianess is handled automatically.
 *
 */
public class ZipArray extends XMLModel
{
    
	private static final String 	DATASET_XML = "dataset.xml";
	private static final String 	CTM_BIN = "ctm.bin";
	private static final String 	ARRAY_BIN = "array.bin";

	protected static final String 	ID = "id";
	protected static final String 	DATASETTYPE = "datasetType";
	protected static final String 	OBJECTNAME = "objectName";
	protected static final String 	CLASSTYPE = "classType";
	protected static final String 	DIMENSION = "dimension";
	protected static final String 	CREATED = "created";
	protected static final String 	MODIFIED = "modified";
	protected static final String 	ACCESSED = "accessed";

	private static final int 		CTM_LEN = 144;

	private File 		zipFile = null;
	
	private byte[] 		byteArray;
	private byte[] 		ctmArray  = new byte[CTM_LEN];

	protected DatasetType	datasetType;
	private ClassType 		classType;
	protected String 		objectName;
	protected String 		id;
	protected long			created;
	protected long			lastAccessed;
	protected long 			lastModified;
	
	protected Matrix4d		ctm;
	
	private int 			numElms;
	private int 			byteLen;
	private int 			dataSize;

	private Vector<Integer> dimensions = new Vector<Integer>();

	private Object 			array;

	private ByteBuffer 		byteBuffer;
	private CharBuffer 		charBuffer;
	private ShortBuffer 	shortBuffer;
	private IntBuffer 		intBuffer;
	private LongBuffer 		longBuffer;
	private FloatBuffer 	fltBuffer;
	private DoubleBuffer 	dblBuffer;

	protected Stack<Integer> 	dimStack = new Stack<Integer>();
	protected int[] 			dimVec;

	/*
	public boolean save ( IFile iFile, DataSet dataSet )
	{
		IPath 	location = iFile.getLocation();
		File 	file;
		if (location != null)
		{
			file = location.toFile();
		}
		
		return true;
	}
	*/
    /**
     * Given a dir and filename, create a zipfile containing the info and bytes that are
     * in the array
     * 
     * @param dir
     * @param zipName
     * @return
     * @throws IOException
     */
    public boolean save( IPath path, Grid grid ) throws IOException
    {
        FileOutputStream        fos = null;
        BufferedOutputStream    bos = null;
        ZipOutputStream         zos = null;

   		if (!grid.array.getClass().isArray())
			throw new RuntimeException("Object must be an array!");

   		reset();
   		
        this.array = grid.array;
        
        numElms = getArrayElms(array, 0);
		classType = grid.classType;

        getDataSize(array);
        getDimensions(array);
        byteLen = numElms * dataSize;
   
		byteArray = new byte[byteLen];
                   
        try
        {
            zipFile = createFile( path );

            //instantiate the ZipOutputStream
            fos = new FileOutputStream( zipFile );
            bos = new BufferedOutputStream(fos);
            zos = new ZipOutputStream(bos);

            // Create the XML info entry, which specifies the datatype and other metadata
            createInfoEntry(zos, grid, ZipEntry.DEFLATED );

            // Create the binary entry that holds the Matrix4d CTM
            createCTMEntry(zos, grid, ZipEntry.DEFLATED );

            // Create the actual array entry itself
            createArrayEntry(zos, ZipEntry.DEFLATED );
                        
            zos.finish();
        }
        catch (ZipException ze)
        {
            throw ze;
        }
        catch (FileNotFoundException fnfe)
        {
            throw fnfe;
        }

        catch (IOException ioe)
        {
            throw ioe;
        }
        finally
        {
            // close all the streams
            if (zos != null)
                zos.close();
 
            if (bos != null)
            {
            	bos.flush();
                bos.close();
            }
            
            if (fos != null)
                fos.close();
         }
        
        return true;
    }

	/**
     * Just a wrapper for the process of creating the actual zipfile
     * 
     * @param dir
     * @param zipName
     * @return the File object created
     * @throws IOException
     */
    public File createFile( IPath path ) throws IOException
    {
        File zipFile = new File(path.toPortableString());

        //check if it is a directory
        if (zipFile.isDirectory())
            throw new IOException("Invalid zip file [" + path.toPortableString() + "]");

        //check if it is read-only            
        if (zipFile.exists() )
        {
            if (!zipFile.canWrite())
                throw new IOException("Existing Zip file is ReadOnly [" + path.toPortableString() + "]");

            // enable overwriting the existing file
            zipFile.delete();
        }
        
        zipFile.createNewFile();
        
        return zipFile;
    }
 
    /**
     * Create the info entry which is the XML file contain the type
     * and dimensions of the array
     * 
     * @param zos
     * @param method
     */
	protected  void createInfoEntry( ZipOutputStream zos, Grid grid, int method )
	{
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		
        try
		{
			this.outStream = buffer;
			
			beginDocument("1.0", null);
	
			attributes.clear();
			
			attributes.addAttribute("", "", ID, Constants.CDATA, grid.getID());
			attributes.addAttribute("", "", DATASETTYPE, Constants.CDATA, grid.getDataSetType().toString());
			if (grid.objectName == null || grid.objectName.length() == 0)
				attributes.addAttribute("", "", OBJECTNAME, Constants.CDATA, grid.getClassType().toString());
			else
				attributes.addAttribute("", "", OBJECTNAME, Constants.CDATA, grid.getObjectName());
			attributes.addAttribute("", "", CLASSTYPE, Constants.CDATA, grid.getClassType().toString());
			attributes.addAttribute("", "", CREATED, Constants.CDATA, Double.toString(grid.getCreated()));
			attributes.addAttribute("", "", ACCESSED, Constants.CDATA, Double.toString(grid.getLastAccessed()));
			attributes.addAttribute("", "", MODIFIED, Constants.CDATA, Double.toString(grid.getLastModified()));
			
			beginElement(Constants.NS_GMS, null, Constants.DATASET, attributes);
			
			for ( int i=0; i<dimensions.size(); i++ )
			{
				beginElement(Constants.NS_GMS, null, DIMENSION, null );
			
				text( String.format("%d",dimensions.elementAt(i)));
	
				finishElement(Constants.NS_GMS, null, DIMENSION );
			}
	
			finishElement(Constants.NS_GMS, null, Constants.DATASET);
	
			finishDocument();	
			
			int		infoLen = buffer.size();
	        byte[] 	infoBytes = buffer.toByteArray();

	        // instantiate the CRC32
	        CRC32 crc = new CRC32();
	        crc.update(infoBytes, 0, infoLen);
	
	        // instantiate the ZipEntry the write it out
	        ZipEntry zentry = new ZipEntry( DATASET_XML);
	        zentry.setMethod( method );
	        zentry.setSize(infoLen);
	        zentry.setCrc(crc.getValue());
	
			zos.putNextEntry(zentry);
		
			// write all the info to the ZipOutputStream
	        zos.write(infoBytes, 0, infoLen);
	        zos.closeEntry();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected  void createCTMEntry( ZipOutputStream zos, Grid grid, int method ) throws ZipException, FileNotFoundException, IOException
	{
		
		try
		{
			dblBuffer = ByteBuffer.wrap(ctmArray).asDoubleBuffer();

			for (int i=0; i<4; i++ )
				for ( int j=0; j<4; j++ )
					dblBuffer.put(grid.getCtm().getElement(i,j));

			// instantiate the CRC32
			CRC32 crc = new CRC32();
			crc.update(ctmArray, 0, CTM_LEN);

			// instantiate the ZipEntry the write it out
			ZipEntry zentry = new ZipEntry(CTM_BIN);
			zentry.setMethod(method);
			zentry.setSize(CTM_LEN);
			zentry.setCrc(crc.getValue());

			zos.putNextEntry(zentry);

			// write all the info to the ZipOutputStream
			zos.write(ctmArray, 0, CTM_LEN);
			zos.closeEntry();
		}
		catch (ZipException ze)
		{
			throw ze;
		}
		catch (FileNotFoundException fnfe)
		{
			throw fnfe;
		}
		catch (IOException ioe)
		{
			throw ioe;
		}
	}

    /**
     * Create the entry which is the actual bits of the array.
     * 
     * @param zos
     * @param fileName
     * @throws ZipException
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void createArrayEntry ( ZipOutputStream zos, int method ) throws ZipException, FileNotFoundException, IOException
    { 
        try
        {
            // instantiate the CRC32
            CRC32 crc = new CRC32();
            crc.update(byteArray, 0, byteLen);

            // instantiate the ZipEntry the write it out
            ZipEntry zentry = new ZipEntry( ARRAY_BIN );
            zentry.setMethod( method );
            zentry.setSize(byteLen);
            zentry.setCrc(crc.getValue());
   
            zos.putNextEntry(zentry);
    		
    		if (classType == ClassType.Byte || classType == ClassType.Boolean)
    			writeByteData(array,false);
       		else if (classType == ClassType.Char)
    			writeCharData(array,false);
    		else if (classType == ClassType.Short)
    			writeShortData(array,false);
    		else if (classType == ClassType.Int)
    			writeIntData(array,false);
    		else if (classType == ClassType.Float)
    			writeFloatData(array,false);
    		else if (classType == ClassType.Long)
    			writeLongData(array,false);
    		else if (classType == ClassType.Double)
    			writeDoubleData(array,false);
    		else if (classType == ClassType.Object)
    			writeObjectData(zos, array, false);

         	// write all the info to the ZipOutputStream
            zos.write(byteArray, 0, byteLen);
            zos.closeEntry();
        }
        catch (ZipException ze)
        {
            throw ze;
        }
        catch (FileNotFoundException fnfe)
        {
            throw fnfe;
        }
        catch (IOException ioe)
        {
            throw ioe;
        }
    }

	/**
	 * Recursive routine to get the number of elms of a rectangular array.
	 * Could be expanded to support ragged arrays, but not now
	 */
	protected int getArrayElms ( Object  vec, int len )
	{
		if (vec.getClass().isArray())
		{
			if (len == 0)
				len = 1;
			
			len *= Array.getLength(vec);
			len = getArrayElms( Array.get(vec, 0), len );
		}
		
		return len;
	}

	/**
	 * Calculate the number of elms in an array (in the zipfile)
	 * based on the dimension data from the dataset.xml
	 */
	protected int getArrayElms()
	{
		int n = 1;
		for ( int i=0;  i<dimensions.size(); i++ )
		{
			n *= dimensions.get(i);
		}
		
		return n;
	}
	
	/**
	 * Calculate the number of elms in an array (in the zipfile)
	 * based on the dimension data from the dataset.xml
	 */
	protected int getArrayElms( int[] intVec )
	{
		int n = 1;
		for ( int i=0;  i<Array.getLength(intVec); i++ )
		{
			n *= intVec[i];
		}
		
		return n;
	}
	/** 
	 *  Recursive routine to determine the dimensions of the array.
	 *  Note that we can't support ragged arrays (yet)
	 *  @param vec
	 */
    private void getDimensions( Object vec )
	{
		if (vec.getClass().isArray())
		{
			dimensions.add(Array.getLength(vec));
			getDimensions( Array.get(vec, 0));
		}		
	}

	/**
	 * Recursive routine to copy all arrays of doubles to the buffer.
	 * Recurses as needed to copy all dimensions
	 * 
	 * @param vec
	 * @param wrapped
	 */
	private void writeDoubleData( Object vec, boolean wrapped )
	{
		if (!wrapped)
			dblBuffer = ByteBuffer.wrap(byteArray).asDoubleBuffer();

		int k = 0;
		while (vec.getClass().isArray() && k < Array.getLength(vec))
		{
			Object row = Array.get(vec, k);
			if (row.getClass().isArray())
				writeDoubleData(row, true);
			else
			{
				dblBuffer.put((double[]) vec, 0, Array.getLength(vec));
				return;
			}
			k++;
		}
	}
	   
	/**
	 * Recursive routine to copy all arrays of floats to the buffer.
	 * Recurses as needed to copy all dimensions
	 * 
	 * @param vec
	 * @param wrapped
	 */
	private void writeFloatData( Object vec, boolean wrapped )
	{
		if (!wrapped)
			fltBuffer = ByteBuffer.wrap(byteArray).asFloatBuffer();

		int k = 0;
		while (vec.getClass().isArray() && k < Array.getLength(vec))
		{
			Object row = Array.get(vec, k);
			if (row.getClass().isArray())
				writeFloatData(row, true);
			else
			{
				fltBuffer.put((float[]) vec, 0, Array.getLength(vec));
				return;
			}
			k++;
		}
	}
	
	/**
	 * Recursive routine to copy all arrays of floats to the buffer.
	 * Recurses as needed to copy all dimensions
	 * 
	 * @param vec
	 * @param wrapped
	 */
	private void writeIntData( Object vec, boolean wrapped )
	{
		if (!wrapped)
			intBuffer = ByteBuffer.wrap(byteArray).asIntBuffer();

		int k = 0;
		while (vec.getClass().isArray() && k < Array.getLength(vec))
		{
			Object row = Array.get(vec, k);
			if (row.getClass().isArray())
				writeIntData(row, true);
			else
			{
				intBuffer.put((int[]) vec, 0, Array.getLength(vec));
				return;
			}
			k++;
		}
	}

	/**
	 * Recursive routine to copy all arrays of floats to the buffer.
	 * Recurses as needed to copy all dimensions
	 * 
	 * @param vec
	 * @param wrapped
	 */
	private void writeByteData( Object vec, boolean wrapped )
	{
		if (!wrapped)
			byteBuffer = ByteBuffer.wrap(byteArray);

		int k = 0;
		while (vec.getClass().isArray() && k < Array.getLength(vec))
		{
			Object row = Array.get(vec, k);
			if (row.getClass().isArray())
				writeByteData(row, true);
			else
			{
				byteBuffer.put((byte[]) vec, 0, Array.getLength(vec));
				return;
			}
			k++;
		}
	}
	
	/**
	 * Recursive routine to copy all arrays of floats to the buffer.
	 * Recurses as needed to copy all dimensions
	 * 
	 * @param vec
	 * @param wrapped
	 */
	private void writeCharData( Object vec, boolean wrapped )
	{
		if (!wrapped)
			charBuffer = ByteBuffer.wrap(byteArray).asCharBuffer();

		int k = 0;
		while (vec.getClass().isArray() && k < Array.getLength(vec))
		{
			Object row = Array.get(vec, k);
			if (row.getClass().isArray())
				writeCharData(row, true);
			else
			{
				charBuffer.put((char[]) vec, 0, Array.getLength(vec));
				return;
			}
			k++;
		}
	}

	/**
	 * Recursive routine to copy all arrays of floats to the buffer.
	 * Recurses as needed to copy all dimensions
	 * 
	 * @param vec
	 * @param wrapped
	 */
	private void writeShortData( Object vec, boolean wrapped )
	{
		if (!wrapped)
			shortBuffer = ByteBuffer.wrap(byteArray).asShortBuffer();

		int k = 0;
		while (vec.getClass().isArray() && k < Array.getLength(vec))
		{
			Object row = Array.get(vec, k);
			if (row.getClass().isArray())
				writeShortData(row, true);
			else
			{
				shortBuffer.put((short[]) vec, 0, Array.getLength(vec));
				return;
			}
			k++;
		}
	}

	/**
	 * Recursive routine to copy all arrays of floats to the buffer.
	 * Recurses as needed to copy all dimensions
	 * 
	 * @param vec
	 * @param wrapped
	 */
	private void writeLongData( Object vec, boolean wrapped )
	{
		if (!wrapped)
			longBuffer = ByteBuffer.wrap(byteArray).asLongBuffer();

		int k = 0;
		while (vec.getClass().isArray() && k < Array.getLength(vec))
		{
			Object row = Array.get(vec, k);
			if (row.getClass().isArray())
				writeLongData(row, true);
			else
			{
				longBuffer.put((long[]) vec, 0, Array.getLength(vec));
				return;
			}
			k++;
		}
	}
	
	/**
	 * Recursive routine to copy all objects in the arrays to the buffer.
	 * Recurses as needed to copy all dimensions
	 * 
	 * @param vec
	 * @param wrapped
	 */
	private void writeObjectData ( ZipOutputStream zos, Object vec, boolean wrapped)
	{
		if (!wrapped)
			byteBuffer = ByteBuffer.wrap(byteArray);

		int k = 0;
		while (vec.getClass().isArray() && k < Array.getLength(vec))
		{
			Object row = Array.get(vec, k);
			if (row.getClass().isArray())
				writeObjectData(zos, row, true);
			else
			{
				for ( int j=0; j<Array.getLength(vec); j++ )
				{
					((IGMSSerialize)Array.get(vec, j)).serializeOut(byteBuffer);
				}

				return;
			}
			k++;
		}
	}

	/**
	 * Recursive routine to get the data type and size of the array
	 */
	/*
	protected ClassType getDataType ( Object  vec )
	{
		if (vec.getClass().isArray() && Array.get(vec, 0).getClass().isArray())
			return getDataType( Array.get(vec, 0) );
		
		Class classe = vec.getClass();
		Class dataType = classe.getComponentType();
		String	typeStr = dataType.toString();
		
		classType = ClassType.valueOf(typeStr);

		if (classType == ClassType.Object)
		{
			objectName = typeStr.substring(new String("class ").length());
			dataSize = ((IGMSSerialize)Array.get(vec, 0)).serializeSize();
		}
		else
		{
			objectName = classType.toString();
			dataSize = ClassUtil.ClassSize[classType.ordinal()];
		}
	
		return classType;
	}
	*/
	
	protected void getDataSize( Object vec )
	{
		if (vec.getClass().isArray() && Array.get(vec, 0).getClass().isArray())
		{
			getDataSize( Array.get(vec, 0) );
			return;
		}
		
		Class 	classe    = vec.getClass();
		Class 	component = classe.getComponentType();
		String	typeStr   = component.toString();

		if (classType == ClassType.Object)
		{
			objectName = typeStr.substring(new String("class ").length());
			Object object = ClassUtil.constructObject(objectName);
			if ((object instanceof IGMSSerialize) == false)
				throw new RuntimeException("Cannot construct array if object doesn't implement IGMSSerialize!");
			
			dataSize = ((IGMSSerialize) object).serializeSize();

           // dataSize = ((IGMSSerialize)Array.get(vec, 0)).serializeSize();
		}
		else
		{
			objectName = classType.toString();
			dataSize = ClassUtil.ClassSize[classType.ordinal()];
		}
	}


	
   //================== EXTRACTION SECTION ===============================
	
	/**
	 * Method to create a new array of the specified type
	 * 
	 */
	public Object create( ClassType dataType, String objectName, int[] dims, Object array )
	{
		this.classType = dataType;
		dimVec = dims;
		this.objectName = objectName;
		
		if (dataType == ClassType.Object)
		{
			Object object = ClassUtil.constructObject(objectName);
			if ((object instanceof IGMSSerialize) == false)
				throw new RuntimeException("Cannot construct array if object doesn't implement IGMSSerialize!");
			
			dataSize = ((IGMSSerialize) object).serializeSize();
		}
		else
		{
			this.objectName = dataType.toString();
			dataSize = ClassUtil.ClassSize[dataType.ordinal()];
		}

		numElms = getArrayElms( dimVec);
		byteLen = numElms * dataSize;

		byteArray = new byte[byteLen];
			
		return array == null ? allocateArray() : array;
	}

    /**
     * Main entry point to create an array from a zipfile 
     * 
     * @param dir - the destination to which the files are written
     * @param zipName - the name of the ePub file that is being extracted
     * @return
     */
    public Grid load( IPath path )
    {
    	Grid grid;
    	
        try
        {
        	reset();
        	
            String pth = path.toPortableString();
            ZipFile    zipfile = new ZipFile(path.toPortableString() );
 
            ZipEntry entry = zipfile.getEntry(DATASET_XML);
 
            extractInfo(zipfile, entry);
 
            entry = zipfile.getEntry(CTM_BIN);
                   
            loadCTMFromZip( zipfile, entry );

            entry = zipfile.getEntry(ARRAY_BIN);
            
            loadArrayFromZip( zipfile, entry );
            
            grid = createGrid();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        return grid;
    }

    private Grid createGrid()
	{
    	Grid grid;
    	if (datasetType.compareTo(DatasetType.Grid) == 0)
    		grid = new Grid(DatasetType.Grid);
    	else
    		throw new RuntimeException("Only Grid datatype currently supported in ZipArray!");
    	
    	grid.setId(id);
    	grid.setLastAccessed(lastAccessed);
    	grid.setCreated(created);
    	grid.setLastModified(lastModified);
    	grid.setObjectName(objectName);
    	grid.setClassType(classType);
    	
    	grid.setDims(dimVec);
    	
    	grid.setCtm(ctm);
    	
    	grid.setArray(array);
    	
		return grid;
	}

	/**
     * Reset all the objects so we're back to zero
     */
    private void reset()
	{
		dimensions.clear();
		dimStack.clear();		
	}

	/**
 	 * Create the array by reading the data from array.bin and building out
 	 * the array itself.
 	 * 
 	 * @param zipfile
 	 * @param entry
 	 * @return
 	 */
    private Object loadArrayFromZip ( ZipFile zipfile, ZipEntry entry )
	{
		Object	object = new Object();
		try
		{
			byte[] buffer = new byte[4096];

			byteBuffer = ByteBuffer.wrap(byteArray);
			InputStream stream = zipfile.getInputStream(entry);

			int len;
			int tot = 0;
			while ((len = stream.read(buffer)) > 0)
			{
				byteBuffer.put(buffer, 0, len);
				tot += len;
			}

			for ( int i=dimensions.size()-1; i>=0; i-- )
			{
				dimStack.add(dimensions.get(i));
			}
			
			allocateArray();
			
			getData( array );
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return object;
	}

	/**
 	 * Create the array by reading the data from array.bin and building out
 	 * the array itself.
 	 * 
 	 * @param zipfile
 	 * @param entry
 	 * @return
 	 */
    private Matrix4d loadCTMFromZip ( ZipFile zipfile, ZipEntry entry )
	{
		ctm = new Matrix4d();
		
		try
		{
			byte[] buffer = new byte[4096];

			byteBuffer = ByteBuffer.wrap(ctmArray);
			InputStream stream = zipfile.getInputStream(entry);

			int len;
			int tot = 0;
			while ((len = stream.read(buffer)) > 0)
			{
				byteBuffer.put(buffer, 0, len);
				tot += len;
			}

			dblBuffer = ByteBuffer.wrap(ctmArray).asDoubleBuffer();

			for (int i=0; i<4; i++ )
				for ( int j=0; j<4; j++ )
					ctm.setElement(i,j,dblBuffer.get());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return ctm;
	}
    
    /** 
     *  Wrapper method to dispatch to the type-specific
     *  allocators. Note that primitive elements actually 
     *  get allocated too, unlike Object arrays (below). 
     */
    private Object allocateArray()
    {
    	if (classType == ClassType.Byte || classType == ClassType.Boolean)
    		allocateByteArray();
       	else if (classType == ClassType.Char)
       		allocateCharArray();
       	else if (classType == ClassType.Short)
       		allocateShortArray();
       	else if (classType == ClassType.Int)
       		allocateIntArray();
       	else if (classType == ClassType.Float)
       		allocateFloatArray();
       	else if (classType == ClassType.Double)
       		allocateDoubleArray();
       	else if (classType == ClassType.Long)
       		allocateLongArray();
       	else if (classType == ClassType.Object)
    		allocateObjectArray();
       	else
       		throw new RuntimeException("Whoa!  Unknown type: '" + classType + "'  in allocateArray()!");
    	
    	return array;
    }
    
    /**
     *   Routine to allocate the byte array. 
     */
	private void allocateByteArray()
    {
		byteBuffer = ByteBuffer.wrap(byteArray);
		
 		array = ClassUtil.constructObjectArray(byte.class, dimVec);

    }

	/**
     *   Routine to allocate the short array. 
     */
	private void allocateShortArray()
    {
		shortBuffer = ByteBuffer.wrap(byteArray).asShortBuffer();
		
 		array = ClassUtil.constructObjectArray(short.class, dimVec);

    }

	/**
     *   Routine to allocate the short array. 
     */
	private void allocateCharArray()
    {
		charBuffer = ByteBuffer.wrap(byteArray).asCharBuffer();
		
 		array = ClassUtil.constructObjectArray(char.class, dimVec);

    }

	/**
     *   Routine to allocate the int array. 
     */
	private void allocateIntArray()
    {
		intBuffer = ByteBuffer.wrap(byteArray).asIntBuffer();
		
 		array = ClassUtil.constructObjectArray(int.class, dimVec);

    }

	/**
     *   Routine to allocate the short array. 
     */
	private void allocateFloatArray()
    {
		fltBuffer = ByteBuffer.wrap(byteArray).asFloatBuffer();
		
 		array = ClassUtil.constructObjectArray(float.class, dimVec);

    }

	/**
     *   Routine to allocate the long array. 
     */
	private void allocateLongArray()
    {
		longBuffer = ByteBuffer.wrap(byteArray).asLongBuffer();
		
 		array = ClassUtil.constructObjectArray(long.class, dimVec);

    }

    /**
     *   Routine to allocate the double array. 
     */
	private void allocateDoubleArray()
    {
		dblBuffer = ByteBuffer.wrap(byteArray).asDoubleBuffer();
		
 		array = ClassUtil.constructObjectArray(double.class, dimVec);

    }

    /**
     *  Allocate the array for objects.  Note that the
     *  routine constructObjectArray does only that.  Unlike
     *  new, it doesn't allocate the objects themselves.
     *  That's done when the array is filled in getObjectData().
     */
    private void allocateObjectArray()
	{
		byteBuffer = ByteBuffer.wrap(byteArray);
			
		array = ClassUtil.constructObjectArray(objectName, dimVec);
		
		// now allocate the actual objects in the array
		allocateObjects(array);
	}

    /**
     * Recursive routine to fetch the data from the buffer obtained from
     * the file and actually fill out the arrays.
     * @param vec
     */
	private void allocateObjects(Object vec)
	{
		int k = 0;
		while (vec.getClass().isArray() && k < Array.getLength(vec))
		{		
			Object row = Array.get(vec, k);
			
			// if row is null then we have a one-dimensional array, which we handle specially
			if (row == null)
			{
				for (int i=0; i<Array.getLength(vec); i++ )
				{
					Object obj = ClassUtil.constructObject(objectName);
					Array.set(vec, i, obj);
				}	
				return;
			}
			else
			{
				if (row.getClass().isArray())
				{
					if (Array.get(row,0) == null)
					{
						for (int i=0; i<Array.getLength(row); i++ )
						{
							Object obj = ClassUtil.constructObject(objectName);
							Array.set(row, i, obj);
						}
					}
				
					allocateObjects(row);
				}
			}

			k++;
		}
	}
	
    /** 
     * Wrapper routine to dispatch to the right "type" of
     * routine.
     * 
     * @param vec
     */
	private void getData(Object vec)
	{
	   	if (classType == ClassType.Byte || classType == ClassType.Boolean)
    		getByteData(vec);
	   	else if (classType == ClassType.Char)
    		getCharData(vec);
	   	else if (classType == ClassType.Short)
    		getShortData(vec);
	   	else if (classType == ClassType.Int)
    		getIntData(vec);
	   	else if (classType == ClassType.Float)
    		getFloatData(vec);
	   	else if (classType == ClassType.Long)
    		getLongData(vec);
	   	else if (classType == ClassType.Double)
    		getDoubleData(vec);
    	else if (classType == ClassType.Object)
    		getObjectData(vec);
	}

    /**
     * Recursive routine to fetch the data from the buffer obtained from
     * the file and actually fill out the arrays.
     * @param vec
     */
    private void getByteData(Object vec)
	{
		int k = 0;
		while (vec.getClass().isArray() && k < Array.getLength(vec))
		{
			Object row = Array.get(vec, k);
			if (row.getClass().isArray())
			{
				getByteData(row);
			}
			else
			{
				byteBuffer.get((byte[]) vec, 0, Array.getLength(vec));
				return;
			}
			k++;
		}
	}

    /**
     * Recursive routine to fetch the data from the buffer obtained from
     * the file and actually fill out the arrays.
     * @param vec
     */
    private void getShortData(Object vec)
	{
		int k = 0;
		while (vec.getClass().isArray() && k < Array.getLength(vec))
		{
			Object row = Array.get(vec, k);
			if (row.getClass().isArray())
			{
				getShortData(row);
			}
			else
			{
				shortBuffer.get((short[]) vec, 0, Array.getLength(vec));
				return;
			}
			k++;
		}
	}
  
    /**
     * Recursive routine to fetch the data from the buffer obtained from
     * the file and actually fill out the arrays.
     * @param vec
     */
    private void getCharData(Object vec)
	{
		int k = 0;
		while (vec.getClass().isArray() && k < Array.getLength(vec))
		{
			Object row = Array.get(vec, k);
			if (row.getClass().isArray())
			{
				getCharData(row);
			}
			else
			{
				charBuffer.get((char[]) vec, 0, Array.getLength(vec));
				return;
			}
			k++;
		}
	}
    
  
    /**
     * Recursive routine to fetch the data from the buffer obtained from
     * the file and actually fill out the arrays.
     * @param vec
     */
    private void getIntData(Object vec)
	{
		int k = 0;
		while (vec.getClass().isArray() && k < Array.getLength(vec))
		{
			Object row = Array.get(vec, k);
			if (row.getClass().isArray())
			{
				getIntData(row);
			}
			else
			{
				intBuffer.get((int[]) vec, 0, Array.getLength(vec));
				return;
			}
			k++;
		}
	}
    
    /**
     * Recursive routine to fetch the data from the buffer obtained from
     * the file and actually fill out the arrays.
     * @param vec
     */
    private void getFloatData(Object vec)
	{
		int k = 0;
		while (vec.getClass().isArray() && k < Array.getLength(vec))
		{
			Object row = Array.get(vec, k);
			if (row.getClass().isArray())
			{
				getFloatData(row);
			}
			else
			{
				fltBuffer.get((float[]) vec, 0, Array.getLength(vec));
				return;
			}
			k++;
		}
	}
    
    /**
     * Recursive routine to fetch the data from the buffer obtained from
     * the file and actually fill out the arrays.
     * @param vec
     */
    private void getLongData(Object vec)
	{
		int k = 0;
		while (vec.getClass().isArray() && k < Array.getLength(vec))
		{
			Object row = Array.get(vec, k);
			if (row.getClass().isArray())
			{
				getLongData(row);
			}
			else
			{
				longBuffer.get((long[]) vec, 0, Array.getLength(vec));
				return;
			}
			k++;
		}
	}
    
    /**
     * Recursive routine to fetch the data from the buffer obtained from
     * the file and actually fill out the arrays.
     * @param vec
     */
    private void getDoubleData(Object vec)
	{
		int k = 0;
		while (vec.getClass().isArray() && k < Array.getLength(vec))
		{
			Object row = Array.get(vec, k);
			if (row.getClass().isArray())
			{
				getDoubleData(row);
			}
			else
			{
				dblBuffer.get((double[]) vec, 0, Array.getLength(vec));
				return;
			}
			k++;
		}
	}
    
    /**
     * Recursive routine to fetch the data from the buffer obtained from
     * the file and actually fill out the arrays.
     * @param vec
     */
	private void getObjectData(Object vec)
	{
		int k = 0;
		while (vec.getClass().isArray() && k < Array.getLength(vec))
		{
			Object row = Array.get(vec, k);
			if (row.getClass().isArray())
			{
				getObjectData(row);
			}
			else
			{
				for ( int j=0; j<Array.getLength(vec); j++ )
				{
					((IGMSSerialize)Array.get(vec, j)).serializeIn(byteBuffer);
				}

				return;
			}
			k++;
		}
	}
    
	/**
	 *  Extract the type and dimensions from the dataset.xml.  Reads
	 *  the file from the zipfile then parses it via SAX.
	 *  
	 * @param zipfile
	 * @param entry
	 */
	private void extractInfo ( ZipFile zipfile, ZipEntry entry )
	{
		InputStream stream;
		try
		{
			stream = zipfile.getInputStream(entry);
			parse(stream);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			System.err.println("DataType " + classType);
			e.printStackTrace();
		}
		
	}

	/**
	 * Handler for SAX parsing callback. We just push the info onto the stack.  
	 * We'll handle it in the end element call.

	 */
	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes)
			throws SAXException
	{
		//System.out.println("StartElm.' localName: '" + localName + "' uri: '"+ uri + "' name: '" + name + "'" );

		OpenElement e = new OpenElement(uri, "", localName, attributes);
		openElements.push(e);
		openElement = true;
	}


	/**
	 * Handler for SAX parsing callback. Just store the characters for later use
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		characters = new String(ch, start, length);		
	}

	/**
	 * Handler for SAX parsing callback.
	 */
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException
	{
		OpenElement e = (OpenElement) openElements.pop();
		openElement = false;

		if (e.getLocalName().compareTo(Constants.DATASET) == 0)
		{
			id = e.getAttributes().getValue(e.getAttributes().getIndex(ID));
			lastAccessed = Long.parseLong(e.getAttributes().getValue(e.getAttributes().getIndex(ACCESSED)));
			lastModified = Long.parseLong(e.getAttributes().getValue(e.getAttributes().getIndex(MODIFIED)));
			created = Long.parseLong(e.getAttributes().getValue(e.getAttributes().getIndex(CREATED)));
			
			datasetType = DatasetType.valueOf(e.getAttributes().getValue(e.getAttributes().getIndex(DATASETTYPE)));
			objectName = e.getAttributes().getValue(e.getAttributes().getIndex(OBJECTNAME));
			
			classType = ClassType.valueOf(e.getAttributes().getValue(e.getAttributes().getIndex(CLASSTYPE)));

			if (classType == ClassType.Object)
			{
				Object object = ClassUtil.constructObject(objectName);
	            dataSize = ((IGMSSerialize)object).serializeSize();
			}
			else
			{
				objectName = classType.toString();
				dataSize = ClassUtil.ClassSize[classType.ordinal()];
			}
			
			numElms = getArrayElms();	
			byteLen = numElms * dataSize;
		   
			byteArray = new byte[byteLen];

			dimVec = new int[dimensions.size()];
			for ( int i=0; i<dimensions.size(); i++ )
			{
				dimVec[i] = dimensions.get(i);
			}
		}
		else if (e.getLocalName().compareTo(DIMENSION) == 0)
		{
			dimensions.add(Integer.parseInt(characters));
		}
	}

	@Override
	public void initParse()
	{
	}
}

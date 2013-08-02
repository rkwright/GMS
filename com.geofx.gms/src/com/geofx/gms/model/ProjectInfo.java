package com.geofx.gms.model;

/*****************************************************************************
 * Copyright (c) 2009 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 ****************************************************************************/

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.geofx.gms.controller.Controller;
import com.geofx.gms.controller.Controller.TimingAttributes;
import com.geofx.gms.datasets.Dataset;
import com.geofx.gms.plugin.Constants;


public class ProjectInfo extends XMLModel 
{
	private Controller							controller;
	
	protected HashMap<String, String>			metadata = new HashMap<String, String>();
	protected HashMap<String, ManifestEntry>	manifest = new HashMap<String, ManifestEntry>();

	public ArrayList<Module>					modules = new ArrayList<Module>();

	protected ArrayList<Input> 					inputElements = new ArrayList<Input>();
	protected ArrayList<Output> 				outputElements = new ArrayList<Output>();
	protected ArrayList<FieldValue> 			fieldValues = new ArrayList<FieldValue>();
	protected ArrayList<String> 				itemValues = new ArrayList<String>();
	
	protected ArrayList<Dataset> 				datasets = new ArrayList<Dataset>();
	
	private ArrayList<IModelListener>			modelListeners = new ArrayList<IModelListener>();
		
	/**
	 * Default ctor.  Just add the TimeValue attributes to the vector
	 */
	public ProjectInfo()
	{
	}
	
	//=================== Parsing Section =====================================
	
	public void initParse()
	{
		// clear all the model elements every time we re-parse
		metadata.clear();
		manifest.clear();
		modules.clear();
		inputElements.clear();
		outputElements.clear();
		fieldValues.clear();
		itemValues.clear();
		datasets.clear();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		System.out.println("StartElm.' qName: '" + qName + "' uri: '"+ uri + "' localName: '" + localName + "'" );

		//	dumpAttributes(attributes);
		
		OpenElement e = new OpenElement(uri, "", localName, attributes);
		openElements.push(e);
		openElement = true;
	}
	
	protected void dumpAttributes (Attributes attributes )
	{
		for ( int i=0; i<attributes.getLength(); i++ )
		{
			System.out.println(String.format("%2d: ",i) + 
								String.format("  uri: %s", attributes.getURI(i)) +
								String.format("  qName: %s", attributes.getQName(i)) + 
								String.format("  localName: %s", attributes.getLocalName(i)) + 
								String.format("  type: %s", attributes.getType(i)) + 
								String.format("  value: %s", attributes.getValue(i)));
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		characters = new String(ch, start, length);
		//System.out.println("characters: " + characters);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		OpenElement e = (OpenElement) openElements.pop();
		openElement = false;

		//System.out.println("endElement: localName: " + localName + "  e.LocalName: " + e.getLocalName());
		
		if (e.getLocalName().equals(Constants.TITLE) || e.getLocalName() == Constants.DESCRIPTION)
		{
			metadata.put(e.getLocalName(), characters);
		}	
		else if (e.getLocalName().equals(Constants.ITEM) && e.getNamespace().equals(Constants.NS_OPF))
		{
			ManifestEntry	entry = new ManifestEntry( e.getAttributes() );
			manifest.put(entry.id, entry);
		}
		else if (e.getLocalName().equals(Constants.SET))
		{
			fieldValues.add(new FieldValue(e.getAttributes(), itemValues));
		}	
		else if (e.getLocalName().equals(Constants.INPUT))
		{
			inputElements.add(new Input(e.getAttributes()));
		}	
		else if (e.getLocalName().equals(Constants.OUTPUT))
		{
			outputElements.add(new Output(e.getAttributes()));
		}	
		else if (e.getLocalName().equals(Constants.ITEM))
		{	
			itemValues.add(	e.getAttributes().getValue("", Constants.VALUE) );
		}	
		else if (e.getLocalName().equals(Constants.MODULE))
		{
			modules.add( createModule( e.getAttributes(), inputElements, outputElements, fieldValues ) );
		}
		else if (e.getLocalName().equals(Constants.SEQUENCE))
		{
			controller = new Controller( e.getAttributes(), this );
		}	
	}

	/**
	 * Create the specified Module.
	 * 
	 * @param attributes
	 * @param inputs
	 * @param outputs
	 * @param fieldValue
	 * @return
	 */
	protected Module createModule( Attributes attributes, ArrayList<Input> inputs, 
									ArrayList<Output> outputs, ArrayList<FieldValue> fieldValues )
	{

		String objectName = attributes.getValue(attributes.getIndex(Constants.OBJECT));
		//Module module = (Module) ClassUtil.constructObject(objectName);
		Module module = (Module) createModule(objectName);
		
		if (module != null)
		{
			module.setValues(attributes, inputs, outputs, fieldValues);
		}

		clearValues();

		return module;
	}
	
	private Module createModule ( String moduleClass ) throws InvalidRegistryObjectException
	{
		Module module = null;

		try
		{
			IExtensionRegistry reg = Platform.getExtensionRegistry();
			IConfigurationElement[] extensions = reg.getConfigurationElementsFor("com.geofx.gms.module");
			for (int i = 0; i < extensions.length; i++)
			{
				IConfigurationElement element = extensions[i];
				String classe = element.getAttribute("class");
				//System.out.println(" class: " + classe);
				if (classe.equals(moduleClass))
				{
					//System.out.println(" Creating module: class: " + classe);
					module = (Module) element.createExecutableExtension("class");
					return module;
				}
			}
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}

		throw new RuntimeException("The module '" + moduleClass + "' does not seem to exist.  Missing from plugin.xml?");
	}
	
	/**
	 * Clear out the current lists of inputs, outputs and fieldValues
	 */
	private void clearValues()
	{
		this.inputElements.clear();
		this.outputElements.clear();
		this.fieldValues.clear();
	}

	//============== Serializer Section ================================
	
	
	public void serialize ( OutputStream stream )
	{
		super.serialize(stream);

		// reset the relevant helper vars
		resetSerialization();	

		beginDocument("1.0", null);

		beginElement(Constants.NS_OPF, null, Constants.PACKAGE, null);
	
		serializeMetadata();

		serializeManifest();
		
		serializeSequence();
	
		finishElement(Constants.NS_OPF, null, Constants.PACKAGE);

		finishDocument();
	}

	private void serializeManifest()
	{
		beginElement(Constants.NS_OPF, null, Constants.MANIFEST, null);

		Set<?> set = manifest.entrySet();

		Iterator<?> iter = set.iterator();

		while (iter.hasNext())
		{
			Map.Entry me = (Map.Entry) iter.next();

			ManifestEntry entry = (ManifestEntry) me.getValue();

			attributes.clear();
			attributes.addAttribute("", Constants.ID, "", Constants.CDATA, entry.id);
			attributes.addAttribute("", Constants.HREF, "", Constants.CDATA, entry.href);
			attributes.addAttribute("", Constants.MEDIA_TYPE, "", Constants.CDATA, entry.mediaType);

			beginElement(Constants.NS_OPF, null, Constants.ITEM, attributes);
			finishElement(Constants.NS_OPF, null, Constants.ITEM);
		}

		finishElement(Constants.NS_OPF, null, Constants.MANIFEST);
	}

	private void serializeMetadata()
	{
		attributes.clear();
		attributes.addAttribute("", "", "xmlns:dc", Constants.CDATA, Constants.NS_DUBLINCORE);
		attributes.addAttribute("", "", "xmlns:dcterms", Constants.CDATA, Constants.NS_DUBLINCORE_TERMS);
		attributes.addAttribute("", "", "xmlns:xsi", Constants.CDATA, Constants.NS_XMLSCHEMA);
		beginElement(Constants.NS_OPF, null, Constants.METADATA, attributes);
	
		Iterator<String> it = metadata.keySet().iterator(); 
		while(it.hasNext()) 
		{ 
			String key = (String) it.next(); 
			String val = metadata.get(key); 

			attributes.clear();

			if (key.equals(Constants.DATE))
			{
				attributes.addAttribute("", "", "xsi:type", Constants.CDATA, "dcterms:W3CDTF");
			}
			else if  (key.equals(Constants.IDENTIFIER))
			{
				attributes.addAttribute("", Constants.ID, "", Constants.CDATA, "projectid");
			}
	
			beginElement("", "dc", key, attributes);
			text(val.toCharArray(), 0, val.length());
			finishElement("", "dc", key);
		}
		
		finishElement(Constants.NS_OPF, null, Constants.METADATA);
	}

	private void serializeSequence()
	{
		if (controller == null)
			return;
		
		attributes.clear();
		
		String[] timingAttribs = controller.getTimingAttribs();
		
		for ( int i=0; i<Array.getLength(timingAttribs); i++ )
		{
			if (timingAttribs[i] != null && timingAttribs[i].length() > 0)
			{
				attributes.addAttribute("", TimingAttributes.convert(i).toString(), "", 
						Constants.CDATA, timingAttribs[i]);				
			}
		}
		
		beginElement(Constants.NS_GMS, null, Constants.SEQUENCE, attributes);
		
		for (int i=0; i<modules.size(); i++ )
		{
			Module module = modules.get(i);
			
			serializeModule(module);
		}
		
		finishElement(Constants.NS_GMS, null, Constants.SEQUENCE);

	}

	/**
	 * Serialize out the module, being the field-values, inputs and outputs
	 * @param module
	 */
	private void serializeModule(Module module)
	{
		attributes.clear();
		attributes.addAttribute("", Constants.OBJECT, "", Constants.CDATA, module.objectName);
		if (module.tab != null && !module.tab.isEmpty())
			attributes.addAttribute("", Constants.TAB, "", Constants.CDATA, module.tab);

		beginElement(Constants.NS_GMS, null, Constants.MODULE, attributes);
		
		serializeFieldValues(module.fieldValues);
		serializeInputs(module.inputs);
		serializeOutputs(module.outputs);

		finishElement(Constants.NS_GMS, null, Constants.MODULE);
	}
	
	/**
	 * Serialize out the field values
	 * 
	 * @param fieldValues
	 */
	private void serializeFieldValues( ArrayList<FieldValue> fieldValues )
	{
		if (fieldValues.size() < 1)
			return;
		
		attributes.clear();
		beginElement(Constants.NS_GMS, null, Constants.INIT, attributes);

		for ( int i=0; i<fieldValues.size(); i++ )
		{
			FieldValue fieldValue = fieldValues.get(i);
			
			attributes.clear();
			attributes.addAttribute("", Constants.FIELD, "", Constants.CDATA, fieldValue.field);
			attributes.addAttribute("", Constants.TYPE, "", Constants.CDATA, fieldValue.type);
			
			if (fieldValue.values.size() > 1)
			{
				beginElement(Constants.NS_GMS, null, Constants.SET, attributes);
				
				for ( int j=0; j<fieldValue.values.size(); j++ )
				{
					attributes.clear();
					attributes.addAttribute("", Constants.VALUE, "", Constants.CDATA, fieldValue.values.get(j));
					beginElement(Constants.NS_GMS, null, Constants.ITEM, attributes);
					finishElement(Constants.NS_GMS, null, Constants.ITEM);					
				}
			}
			else
			{
				attributes.addAttribute("", Constants.VALUE, "", Constants.CDATA, fieldValue.values.get(0));
				beginElement(Constants.NS_GMS, null, Constants.SET, attributes);
			}

			finishElement(Constants.NS_GMS, null, Constants.SET);			
		}

		finishElement(Constants.NS_GMS, null, Constants.INIT);
	}

	/**
	 * Serialize out the inputs for the current module
	 * 
	 * @param inputs
	 */
	private void serializeInputs(ArrayList<Input> inputs)
	{
		if (inputs.size() < 1)
			return;
		
		attributes.clear();
		beginElement(Constants.NS_GMS, null, Constants.INPUTS, attributes);
	
		for ( int i=0; i<inputs.size(); i++ )
		{
			Input input = inputs.get(i);
			
			attributes.clear();
			attributes.addAttribute("", Constants.FIELD, "", Constants.CDATA, input.field);
			attributes.addAttribute("", Constants.TYPE, "", Constants.CDATA, input.type);
			
			if (input.href != null && input.href.length() > 0)
				attributes.addAttribute("", Constants.HREF, "", Constants.CDATA, input.href);
			else if (input.idref != null && input.idref.length() > 0)
				attributes.addAttribute("", Constants.IDREF, "", Constants.CDATA, input.idref);				
			
			beginElement(Constants.NS_GMS, null, Constants.INPUT, attributes);
			finishElement(Constants.NS_GMS, null, Constants.INPUT);		
		}
		
		finishElement(Constants.NS_GMS, null, Constants.INPUTS);
	}

	/**
	 * Serialize out the outputs for the current module
	 * 
	 * @param outputs
	 */
	private void serializeOutputs(ArrayList<Output> outputs)
	{
		if (outputs.size() < 1)
			return;
		
		attributes.clear();
		beginElement(Constants.NS_GMS, null, Constants.OUTPUTS, attributes);
	
		for ( int i=0; i<outputs.size(); i++ )
		{
			Output output = outputs.get(i);
			
			// if the id is null, then this is a placeholder output, so don't serialize it
			if (output.id == null)
				continue;
			
			attributes.clear();

			if (output.field != null && output.field.length() > 0)
				attributes.addAttribute("", Constants.FIELD, "", Constants.CDATA, output.field);
			
			attributes.addAttribute("", Constants.TYPE, "", Constants.CDATA, output.type);
			attributes.addAttribute("", Constants.ID, "", Constants.CDATA, output.id);				
			
			beginElement(Constants.NS_GMS, null, Constants.OUTPUT, attributes);
			finishElement(Constants.NS_GMS, null, Constants.OUTPUT);		
		}
		
		finishElement(Constants.NS_GMS, null, Constants.OUTPUTS);
	}


	/**
	 * Create the container file and serialize out the data into it.  The contents
	 * are essentially fixed at this point
	 * 
	 * @param folder
	 */
	public void serializeContainerFile(IFolder folder)
	{
		final IFile 	file = folder.getFile(new Path(Constants.OCF_CONTAINERFILE));
		IPath path = file.getFullPath().makeAbsolute();

		try
		{
			// create the META-INF folder in the right place
			folder.create(false, true, null);
			
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
			IPath location = root.getLocation();
			
			String fullPath = location.toPortableString() + path.toPortableString();
			System.out.println("path: " + fullPath);
			
			FileOutputStream stream = new FileOutputStream(fullPath);
			
			super.serialize(stream);

			// write out the prolog
			beginDocument("1.0", null);

			// then the package element
			attributes.clear();
			attributes.addAttribute("", Constants.VERSION, "", Constants.CDATA, "1.0");

			beginElement(Constants.NS_OASIS, null, Constants.OCF_CONTAINER, attributes);

			// then the rootfiles element
			beginElement(Constants.NS_OASIS, null, Constants.OCF_ROOTFILES, null);

			// then the list of rootfile(s) of which we only have one
			attributes.clear();
			attributes.addAttribute("", Constants.FULL_PATH, "", Constants.CDATA, Constants.GMS_PROJECT_NAME);
			attributes.addAttribute("", Constants.MEDIA_TYPE, "", Constants.CDATA, Constants.GMS_MIMETYPE);

			beginElement(Constants.NS_OASIS, null, Constants.OCF_ROOTFILE, attributes);
			
			finishElement(Constants.NS_OASIS, null, Constants.OCF_ROOTFILE);

			finishElement(Constants.NS_OASIS, null, Constants.OCF_ROOTFILES);

			finishElement(Constants.NS_OASIS, null, Constants.OCF_CONTAINER);
			
			// and finish up the doc, which closes the document and writes out the contents
			finishDocument();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
	}
	


	//==================== Public Routines ====================================
	
	public void setMetadataItem( String key, String value )
	{
		metadata.put(key, value);
	}
	
	public String getMetadataItem ( String key )
	{
		return metadata.get(key);
	}

	public HashMap<String, ManifestEntry> getManifest()
	{
		return manifest;
	}
	
	public void addManifestEntry ( String id, String href, String mediaType )
	{
		ManifestEntry	entry = new ManifestEntry( id, href, mediaType );
		manifest.put(id, entry);
		fireModelChanged(IModelListener.CHANGED);
	}

	public void addModelListener( IModelListener modelListener )
	{
		modelListeners.add(modelListener);
	}
	
	public void removeModelListener(IModelListener listener)
	{
		modelListeners.remove(listener);
	}

	public void fireModelChanged( String type)
	{
		for (int i = 0; i < modelListeners.size(); i++)
		{
			((IModelListener) modelListeners.get(i)).modelChanged(type);
		}
	}

	public Controller getController()
	{
		return controller;
	}

}

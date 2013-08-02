
package com.geofx.gms.plugin;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Strings
{
	private static final String BUNDLE_NAME = "com.geofx.gms.plugin.strings"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private Strings()
	{
	}

	public static String getString(String key)
	{
		try
		{
			return RESOURCE_BUNDLE.getString(key);
		}
		catch (MissingResourceException e)
		{
			return '!' + key + '!';
		}
	}
}

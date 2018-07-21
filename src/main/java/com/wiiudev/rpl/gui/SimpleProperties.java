package com.wiiudev.rpl.gui;

import java.io.*;
import java.util.Properties;

class SimpleProperties
{
	private Properties properties;
	private String propertiesFileName;

	SimpleProperties()
	{
		propertiesFileName = "config.properties";
		properties = new Properties();

		try
		{
			if (new File(propertiesFileName).exists())
			{
				InputStream propertiesReader = new FileInputStream(propertiesFileName);
				properties.load(propertiesReader);
			}
		} catch (IOException exception)
		{
			exception.printStackTrace();
		}
	}

	@SuppressWarnings("SameParameterValue")
	void put(String key, String value)
	{
		properties.setProperty(key, value);
	}

	void writeToFile()
	{
		try
		{
			OutputStream propertiesWriter = new FileOutputStream(propertiesFileName);
			properties.store(propertiesWriter, null);
		} catch (IOException exception)
		{
			exception.printStackTrace();
		}
	}

	@SuppressWarnings("SameParameterValue")
	String get(String key)
	{
		return (String) properties.get(key);
	}
}

package com.wiiudev.rpl;

import lombok.val;

import java.util.LinkedList;

public enum ExecutableFileExtension
{
	RPL,
	RPX;

	public static boolean isExecutable(String fileName)
	{
		val executableFileExtensions = values();
		fileName = fileName.toLowerCase();

		for (val executableFileExtension : executableFileExtensions)
		{
			val extension = executableFileExtension.name().toLowerCase();
			if (fileName.endsWith("." + extension))
			{
				return true;
			}
		}

		return false;
	}

	public static String[] getExtensions()
	{
		val executableFileExtensions = values();
		val extensions = new LinkedList<String>();

		for (val executableFileExtension : executableFileExtensions)
		{
			extensions.add(executableFileExtension.name().toLowerCase());
		}

		return extensions.toArray(new String[0]);
	}
}

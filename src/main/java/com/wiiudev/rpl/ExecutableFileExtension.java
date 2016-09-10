package com.wiiudev.rpl;

import java.util.LinkedList;
import java.util.List;

public enum ExecutableFileExtension
{
	RPL, RPX;

	public static boolean isExecutable(String fileName)
	{
		ExecutableFileExtension[] executableFileExtensions = values();
		fileName = fileName.toLowerCase();

		for (ExecutableFileExtension executableFileExtension : executableFileExtensions)
		{
			String extension = executableFileExtension.name().toLowerCase();
			if (fileName.endsWith("." + extension))
			{
				return true;
			}
		}

		return false;
	}

	public static String[] getExtensions()
	{
		ExecutableFileExtension[] executableFileExtensions = values();
		List<String> extensions = new LinkedList<>();

		for (ExecutableFileExtension executableFileExtension : executableFileExtensions)
		{
			extensions.add(executableFileExtension.name().toLowerCase());
		}

		return extensions.stream().toArray(String[]::new);
	}
}
package com.wiiudev.rpl.gui.utilities;

public class FileNameUtilities
{
	public static String getBaseFileName(String fileName)
	{
		return fileName.split("\\.")[0];
	}
}
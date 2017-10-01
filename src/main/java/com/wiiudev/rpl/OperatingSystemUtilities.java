package com.wiiudev.rpl;

public class OperatingSystemUtilities
{
	public static boolean isRunningWindows()
	{
		String operatingSystem = System.getProperty("os.name").toLowerCase();

		return operatingSystem.startsWith("windows");
	}
}
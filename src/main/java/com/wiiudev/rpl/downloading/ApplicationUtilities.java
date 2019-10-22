package com.wiiudev.rpl.downloading;

import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class ApplicationUtilities
{
	// http://stackoverflow.com/a/19005828/3764804
	public static boolean isProcessRunning(String processName) throws IOException
	{
		val processBuilder = new ProcessBuilder("tasklist");
		val process = processBuilder.start();
		val tasksList = toString(process.getInputStream());

		return tasksList.contains(processName);
	}

	// http://stackoverflow.com/a/5445161/3764804
	private static String toString(InputStream inputStream)
	{
		try (val scanner = new Scanner(inputStream, "UTF-8").useDelimiter("\\A"))
		{
			return scanner.hasNext() ? scanner.next() : "";
		}
	}
}
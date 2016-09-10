package com.wiiudev.rpl;

import com.wiiudev.rpl.downloading.DownloadingUtilities;
import com.wiiudev.rpl.downloading.ZipUtilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RPXTool
{
	private String filePath;
	private String downloadURL;

	public RPXTool()
	{
		this("wiiurpxtool.exe",
				"https://github.com/0CBH0/wiiurpxtool/releases/download/v1.1/wiiurpxtool.zip");
	}

	private RPXTool(String filePath, String downloadURL)
	{
		this.filePath = filePath;
		this.downloadURL = downloadURL;
	}

	public void initialize() throws IOException
	{
		Path library = Paths.get(filePath);

		if (!Files.exists(library))
		{
			Path downloadedZipFile = DownloadingUtilities.download(downloadURL);
			ZipUtilities.unZip(downloadedZipFile);
		}
	}

	public void unpack(String inputFile) throws Exception
	{
		runProcess(filePath, "d", inputFile);
	}

	public void repack(String inputFile) throws Exception
	{
		runProcess(filePath, "c", inputFile);
	}

	private void runProcess(String filePath, String argument, String inputFile) throws IOException, InterruptedException
	{
		ProcessBuilder processBuilder = new ProcessBuilder().inheritIO();
		processBuilder.command(filePath, "-" + argument, inputFile);
		Process process = processBuilder.start();
		process.waitFor();
	}
}
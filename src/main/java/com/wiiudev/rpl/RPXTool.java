package com.wiiudev.rpl;

import com.wiiudev.rpl.downloading.DownloadingUtilities;
import com.wiiudev.rpl.downloading.ZipUtilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class RPXTool
{
	public static final String APPLICATION_NAME = "wiiurpxtool";
	private static final String EXECUTABLE_FILE_PATH = APPLICATION_NAME + ".exe";

	private String filePath;
	private String downloadURL;
	private boolean initialized;

	public static RPXTool getInstance() throws IOException
	{
		String downloadURL = getRedirectedURL("https://github.com/0CBH0/" + APPLICATION_NAME + "/releases/latest") + "/" + APPLICATION_NAME + ".zip";

		return new RPXTool(EXECUTABLE_FILE_PATH, downloadURL);
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

		initialized = true;
	}

	public void unpack(String inputFile) throws Exception
	{
		runProcess(filePath, "d", inputFile);
		Path targetPath = Paths.get(inputFile);
		rename(targetPath, getDecompressedFileName(inputFile));
	}

	public void repack(String inputFile) throws Exception
	{
		runProcess(filePath, "c", inputFile);
		Path targetPath = Paths.get(inputFile);
		rename(targetPath, getCompressedFileName(inputFile));
	}

	private String getCompressedFileName(String inputFile)
	{
		String extension = getExtension(inputFile);
		inputFile = inputFile.replace(extension, "");
		extension = extension.substring(1); // Drop the "d"
		return inputFile + extension;
	}

	public static String getDecompressedFileName(String inputFile)
	{
		String extension = getExtension(inputFile);

		return inputFile.replace(extension, "") + "d" + extension;
	}

	private void rename(Path oldName, String newNameString) throws IOException
	{
		Files.move(oldName,
				oldName.resolveSibling(newNameString),
				StandardCopyOption.REPLACE_EXISTING);
	}

	private static String getExtension(String fileName)
	{
		String extension = "";

		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex > 0)
		{
			extension = fileName.substring(dotIndex + 1);
		}

		return extension;
	}

	private void runProcess(String filePath, String argument, String inputFile) throws IOException, InterruptedException
	{
		ProcessBuilder processBuilder = new ProcessBuilder().inheritIO();
		processBuilder.command(filePath, "-" + argument, inputFile);
		Process process = processBuilder.start();
		process.waitFor();
	}

	private static String getRedirectedURL(String url) throws IOException
	{
		URLConnection urlConnection = new URL(url).openConnection();
		urlConnection.connect();

		try (InputStream ignored = urlConnection.getInputStream())
		{
			return urlConnection.getURL().toString();
		}
	}

	public boolean isInitialized()
	{
		return initialized;
	}
}
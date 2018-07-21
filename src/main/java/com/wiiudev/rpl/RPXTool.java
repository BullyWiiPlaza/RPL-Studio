package com.wiiudev.rpl;

import com.wiiudev.rpl.downloading.DownloadingUtilities;
import com.wiiudev.rpl.downloading.ZipUtilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class RPXTool
{
	public static final String APPLICATION_NAME = "wiiurpxtool";
	private static final String RPL_2_ELF = "rpl2elf";
	private static final String WII_U_RPX_TOOL_FILE_PATH = APPLICATION_NAME + ".exe";

	private String filePath;
	private String downloadURL;
	private boolean initialized;

	public static RPXTool getInstance() throws IOException
	{
		String downloadURL = getRedirectedURL("https://github.com/0CBH0/"
				+ APPLICATION_NAME + "/releases/latest") + "/" + APPLICATION_NAME + ".zip";

		return new RPXTool(WII_U_RPX_TOOL_FILE_PATH, downloadURL);
	}

	private RPXTool(String filePath, String downloadURL)
	{
		this.filePath = filePath;
		this.downloadURL = downloadURL;
	}

	public void initialize() throws IOException
	{
		Path library = Paths.get(filePath);

		if (!exists(library))
		{
			Path downloadedZipFile = DownloadingUtilities.download(downloadURL);
			ZipUtilities.unZip(downloadedZipFile);
		}

		initialized = true;
	}

	public void unpack(String inputFile, UnpackExecutable selectedItem) throws Exception
	{
		switch (selectedItem)
		{
			case WII_U_RPX_TOOL:
				decompressUsingWiiURPXTool(inputFile);
				break;

			case RPL_2_ELF:
				decompressUsingRPL2ELF(inputFile);
				break;

			default:
				throw new IllegalStateException("Unhandled unpack tool: " + selectedItem);
		}
	}

	private void decompressUsingWiiURPXTool(String inputFile) throws IOException, InterruptedException
	{
		runProcess(filePath, "d", inputFile);
		Path targetPath = Paths.get(inputFile);
		String decompressedFileName = getDecompressedFileName(inputFile);
		rename(targetPath, decompressedFileName);
	}

	private void decompressUsingRPL2ELF(String inputFile) throws IOException, InterruptedException
	{
		Class<? extends RPXTool> currentClass = this.getClass();
		try (InputStream resourceAsStream = currentClass.getResourceAsStream("/" + RPL_2_ELF + ".exe"))
		{
			if (resourceAsStream == null)
			{
				throw new IllegalStateException(RPL_2_ELF + " not found in classpath resources");
			}

			byte[] executableBytes = readInputStreamToByteArray(resourceAsStream);
			Path temporaryFile = createTempFile("prefix", "suffix");

			try
			{
				write(temporaryFile, executableBytes);
				ProcessBuilder processBuilder = new ProcessBuilder().inheritIO();
				String decompressedFileName = getDecompressedFileName(inputFile);
				processBuilder.command(temporaryFile.toString(), inputFile, decompressedFileName);
				Process process = processBuilder.start();
				process.waitFor();
			} finally
			{
				delete(temporaryFile);
			}
		}
	}

	private byte[] readInputStreamToByteArray(InputStream inputStream) throws IOException
	{
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[16384];

		while ((nRead = inputStream.read(data, 0, data.length)) != -1)
		{
			buffer.write(data, 0, nRead);
		}

		buffer.flush();

		return buffer.toByteArray();
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
		Path target = oldName.resolveSibling(newNameString);
		move(oldName, target, REPLACE_EXISTING);
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

	@SuppressWarnings("SameParameterValue")
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

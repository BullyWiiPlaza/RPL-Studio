package com.wiiudev.rpl;

import lombok.Getter;
import lombok.val;
import lombok.var;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.wiiudev.rpl.downloading.DownloadingUtilities.download;
import static com.wiiudev.rpl.downloading.ZipUtilities.unZip;
import static java.nio.file.Files.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.io.IOUtils.toByteArray;

public class RPXTool
{
	public static final String APPLICATION_NAME = "wiiurpxtool";
	private static final String RPL_2_ELF = "rpl2elf";
	private static final String WII_U_RPX_TOOL_FILE_PATH = APPLICATION_NAME + ".exe";

	private String filePath;
	private String downloadURL;

	@Getter
	private boolean initialized;

	public static RPXTool getInstance() throws IOException
	{
		val downloadURL = getRedirectedURL("https://github.com/0CBH0/"
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
		val library = Paths.get(filePath);

		if (!exists(library))
		{
			val downloadedZipFile = download(downloadURL);
			unZip(downloadedZipFile);
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

	private void decompressUsingWiiURPXTool(String inputFile) throws Exception
	{
		runProcess(filePath, "d", inputFile);
		val targetPath = Paths.get(inputFile);
		val decompressedFileName = getDecompressedFileName(inputFile);
		rename(targetPath, decompressedFileName);
	}

	private void decompressUsingRPL2ELF(String inputFile) throws IOException, InterruptedException
	{
		val currentClass = this.getClass();
		try (val resourceInputStream = currentClass.getResourceAsStream("/" + RPL_2_ELF + ".exe"))
		{
			if (resourceInputStream == null)
			{
				throw new IllegalStateException(RPL_2_ELF + " not found in classpath resources");
			}

			val executableBytes = toByteArray(resourceInputStream);
			val temporaryFile = createTempFile("prefix", "suffix");

			try
			{
				write(temporaryFile, executableBytes);
				val processBuilder = new ProcessBuilder().inheritIO();
				val decompressedFileName = getDecompressedFileName(inputFile);
				processBuilder.command(temporaryFile.toString(), inputFile, decompressedFileName);
				val process = processBuilder.start();
				process.waitFor();
			} finally
			{
				delete(temporaryFile);
			}
		}
	}

	public void repack(String inputFile) throws Exception
	{
		runProcess(filePath, "c", inputFile);
		val targetPath = Paths.get(inputFile);
		rename(targetPath, getCompressedFileName(inputFile));
	}

	private String getCompressedFileName(String inputFile)
	{
		var extension = getExtension(inputFile);
		inputFile = inputFile.replace(extension, "");
		extension = extension.substring(1); // Drop the "d"
		return inputFile + extension;
	}

	public static String getDecompressedFileName(String inputFile)
	{
		val extension = getExtension(inputFile);
		return inputFile.replace(extension, "") + "d" + extension;
	}

	private void rename(Path oldName, String newNameString) throws IOException
	{
		val target = oldName.resolveSibling(newNameString);
		move(oldName, target, REPLACE_EXISTING);
	}

	private static String getExtension(String fileName)
	{
		var extension = "";
		val dotIndex = fileName.lastIndexOf('.');
		if (dotIndex > 0)
		{
			extension = fileName.substring(dotIndex + 1);
		}

		return extension;
	}

	private void runProcess(String filePath, String argument, String inputFile) throws Exception
	{
		val processBuilder = new ProcessBuilder().inheritIO();
		processBuilder.command(filePath, "-" + argument, inputFile);
		Process process = processBuilder.start();
		process.waitFor();
	}

	@SuppressWarnings("SameParameterValue")
	private static String getRedirectedURL(String url) throws IOException
	{
		val urlConnection = new URL(url).openConnection();
		urlConnection.connect();

		try (val ignored = urlConnection.getInputStream())
		{
			return urlConnection.getURL().toString();
		}
	}
}

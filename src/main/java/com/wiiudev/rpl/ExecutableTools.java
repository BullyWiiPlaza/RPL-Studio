package com.wiiudev.rpl;

import com.wiiudev.rpl.gui.utilities.FileNameUtilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public enum ExecutableTools
{
	RPL_2_ELF("Tools\\rpl2elf.exe"),
	MAKERPL64("system\\bin\\tool\\makerpl\\makerpl64.exe"),
	HXD("Tools\\HxD\\HxD.exe");

	private String name;

	ExecutableTools(String name)
	{
		this.name = name;
	}

	public static String convertRPLToELF(String inputFile) throws IOException, InterruptedException
	{
		ProcessBuilder processBuilder = new ProcessBuilder().inheritIO();
		String programFilePath = ExecutableTools.RPL_2_ELF.toString();
		String baseName = FileNameUtilities.getBaseFileName(inputFile);
		String elfFileName = baseName + FileExtensions.ELF;
		processBuilder.command(programFilePath, inputFile, elfFileName);
		Process process = processBuilder.start();
		process.waitFor();

		return elfFileName;
	}

	public static void openWithHexEditor(String elfFileName) throws IOException, InterruptedException
	{
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(HXD.toString(), elfFileName);
		processBuilder.start();
	}

	public static void repack(ExecutableFileType executableFileType, String elfFileName, String cafeRootDirectory) throws IOException, InterruptedException
	{
		ProcessBuilder processBuilder = new ProcessBuilder();
		Map<String, String> environmentVariables = processBuilder.environment();
		environmentVariables.put("CAFE_ROOT", cafeRootDirectory);
		List<String> commands = buildExecutableRepackingCommand(executableFileType, elfFileName, cafeRootDirectory);
		processBuilder.command(commands);
		Process process = processBuilder.start();
		process.waitFor();

		deleteGeneratedArchFile(elfFileName);
	}

	private static void deleteGeneratedArchFile(String elfFileName) throws IOException
	{
		String archFile = FileNameUtilities.getBaseFileName(elfFileName) + FileExtensions.A;
		Files.delete(Paths.get(archFile));
	}

	public static List<String> buildExecutableRepackingCommand(ExecutableFileType executableFileType, String elfFileName, String cafeRootDirectory)
	{
		String programFilePath = cafeRootDirectory + File.separator + ExecutableTools.MAKERPL64.toString();
		List<String> commands = new ArrayList<>();
		commands.add(programFilePath);

		if (executableFileType == ExecutableFileType.RPX)
		{
			commands.add("-f");
		}

		commands.add("-old");
		commands.add(elfFileName);

		return commands;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
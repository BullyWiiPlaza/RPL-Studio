package com.wiiudev.rpl.downloading;

import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.wiiudev.rpl.downloading.DownloadingUtilities.*;

public class ApplicationLauncher
{
	@Getter
	private Path installedExecutablePath;

	@Getter
	private String downloadURL;

	@Getter
	private String name;

	@Getter
	private boolean unZip;

	public ApplicationLauncher(String installedExecutablePath,
	                           String downloadURL,
	                           String name,
	                           boolean unZip)
	{
		this.installedExecutablePath = Paths.get(installedExecutablePath);
		this.downloadURL = downloadURL;
		this.name = name;
		this.unZip = unZip;
	}

	public String getSetupFileName()
	{
		return getFileName(downloadURL);
	}
}
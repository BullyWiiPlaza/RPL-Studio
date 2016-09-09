package com.wiiudev.rpl;

public enum ExecutableFileType
{
	RPL, RPX;

	public static ExecutableFileType parse(String fileType)
	{
		ExecutableFileType[] executableFileTypes = values();

		for(ExecutableFileType executableFileType : executableFileTypes)
		{
			if(executableFileType.toString().equals(fileType))
			{
				return executableFileType;
			}
		}

		return null;
	}
}
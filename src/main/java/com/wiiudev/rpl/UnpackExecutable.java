package com.wiiudev.rpl;

public enum UnpackExecutable
{
	RPL_2_ELF("rpl2elf"),
	WII_U_RPX_TOOL("wiiurpxtool");

	private final String name;

	UnpackExecutable(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}

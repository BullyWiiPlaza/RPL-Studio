package com.wiiudev.rpl;

public enum FileExtensions
{
	ELF, A, RPL, RPX;

	@Override
	public String toString()
	{
		return "." + name().toLowerCase();
	}
}
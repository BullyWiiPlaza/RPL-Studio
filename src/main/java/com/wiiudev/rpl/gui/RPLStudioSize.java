package com.wiiudev.rpl.gui;

import javax.swing.*;
import java.awt.*;

public class RPLStudioSize
{
	private static final int DEFAULT_WIDTH = 400;
	private static final int DEFAULT_HEIGHT = 100;

	public static void setDefaultSize(JFrame frame)
	{
		setSize(frame, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	private static void setSize(JFrame frame, int width, int height)
	{
		Dimension dimension = new Dimension(width, height);
		frame.setMinimumSize(dimension);
		frame.setSize(dimension);
	}

	private static final int INITIALIZATION_HEIGHT_INCREMENT = 10;

	public static void setInitializingSize(JFrame frame)
	{
		int height = DEFAULT_HEIGHT + INITIALIZATION_HEIGHT_INCREMENT;
		setSize(frame, DEFAULT_WIDTH, height);
	}
}
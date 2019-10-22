package com.wiiudev.rpl.gui;

import lombok.val;

import javax.swing.*;
import java.awt.*;

class RPLStudioSize
{
	private static final int DEFAULT_WIDTH = 450;
	private static final int DEFAULT_HEIGHT = 140;

	static void setDefaultSize(JFrame frame)
	{
		setSize(frame, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	@SuppressWarnings("SameParameterValue")
	private static void setSize(JFrame frame, int width, int height)
	{
		val dimension = new Dimension(width, height);
		frame.setMinimumSize(dimension);
		frame.setSize(dimension);
	}

	private static final int INITIALIZATION_HEIGHT_INCREMENT = 10;

	static void setInitializingSize(JFrame frame)
	{
		val height = DEFAULT_HEIGHT + INITIALIZATION_HEIGHT_INCREMENT;
		setSize(frame, DEFAULT_WIDTH, height);
	}
}

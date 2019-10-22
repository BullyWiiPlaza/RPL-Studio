package com.wiiudev.rpl.gui;

import lombok.val;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

class IconImageUtilities
{
	@SuppressWarnings("SameParameterValue")
	static void setIconImage(Window window, String fileName)
	{
		try
		{
			val imageInputStream = window.getClass().getResourceAsStream("/" + fileName);
			val bufferedImage = ImageIO.read(imageInputStream);
			window.setIconImage(bufferedImage);
		} catch (IOException exception)
		{
			exception.printStackTrace();
		}
	}
}

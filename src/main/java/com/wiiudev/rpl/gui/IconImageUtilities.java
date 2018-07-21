package com.wiiudev.rpl.gui;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

class IconImageUtilities
{
	@SuppressWarnings("SameParameterValue")
	static void setIconImage(Window window, String fileName)
	{
		try
		{
			InputStream imageInputStream = window.getClass().getResourceAsStream("/" + fileName);
			BufferedImage bufferedImage = ImageIO.read(imageInputStream);
			window.setIconImage(bufferedImage);
		} catch (IOException exception)
		{
			exception.printStackTrace();
		}
	}
}

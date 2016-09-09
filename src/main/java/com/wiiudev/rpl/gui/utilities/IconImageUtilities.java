package com.wiiudev.rpl.gui.utilities;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class IconImageUtilities
{
	public static void setIconImage(Window window, String fileName)
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
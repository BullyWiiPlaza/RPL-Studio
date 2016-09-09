package com.wiiudev.rpl.gui.utilities;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;

public class FileChooserWrapper extends JFileChooser
{
	private JTextField pathField;
	private String extensionDescription;
	private String extension;

	public FileChooserWrapper(JTextField pathField)
	{
		this.pathField = pathField;
		configure();
	}

	public FileChooserWrapper(JTextField pathField, String description, String extension)
	{
		this.pathField = pathField;
		this.extensionDescription = description;
		this.extension = extension;
		setFileFilter();
		configure();
	}

	private void setFileFilter()
	{
		setFileFilter(new FileFilter()
		{
			public String getDescription()
			{
				return extensionDescription + " (*." + extension + ")";
			}

			public boolean accept(File file)
			{
				if (file.isDirectory())
				{
					return true;
				} else
				{
					String filename = file.getName().toLowerCase();
					return filename.endsWith("." + extension);
				}
			}
		});
	}

	public void selectFile(Component parent)
	{
		int selectedAnswer = showOpenDialog(parent);

		if (selectedAnswer == JOptionPane.OK_OPTION)
		{
			File selectedFile = getSelectedFile();
			String path = selectedFile.getAbsolutePath();
			String programDirectory = getProgramDirectory() + File.separator;

			if (path.contains(programDirectory))
			{
				path = path.replace(programDirectory, "");
			}

			pathField.setText(path);
		}
	}

	private String getProgramDirectory()
	{
		return System.getProperty("user.dir");
	}

	private void configure()
	{
		File currentFile = new File(pathField.getText());
		File parentDirectory = currentFile.getParentFile();

		if (currentFile.exists() && parentDirectory != null)
		{
			setCurrentDirectory(parentDirectory);
		} else
		{
			File currentDirectory = new File(getProgramDirectory());
			setCurrentDirectory(currentDirectory);
		}

		if(extension == null && extensionDescription == null)
		{
			setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		else
		{
			setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
	}
}
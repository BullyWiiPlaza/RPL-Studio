package com.wiiudev.rpl.gui;

import com.wiiudev.rpl.ExecutableFileExtension;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.Arrays;

class FileChooserWrapper extends JFileChooser
{
	private JTextField pathField;
	private String extensionDescription;
	private String[] extensions;

	FileChooserWrapper(JTextField pathField, String description, String[] extensions)
	{
		this.pathField = pathField;
		this.extensionDescription = description;
		this.extensions = extensions;
		setFileFilter();
		configure();
	}

	private void setFileFilter()
	{
		setFileFilter(new FileFilter()
		{
			public String getDescription()
			{
				return extensionDescription
						+ " "
						+ Arrays.toString(extensions).replace("[", "(")
						.replace("]", ")");
			}

			public boolean accept(File file)
			{
				if (file.isDirectory())
				{
					return true;
				} else
				{
					return ExecutableFileExtension.isExecutable(file.getAbsolutePath());
				}
			}
		});
	}

	void selectFile(Component parent)
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

		if (extensions == null && extensionDescription == null)
		{
			setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		} else
		{
			setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
	}
}

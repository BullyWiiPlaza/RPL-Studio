package com.wiiudev.rpl.gui;

import lombok.val;
import lombok.var;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.Arrays;

import static com.wiiudev.rpl.ExecutableFileExtension.isExecutable;
import static javax.swing.JOptionPane.OK_OPTION;

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
		val filter = new FileFilter()
		{
			public String getDescription()
			{
				return extensionDescription + " "
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
					return isExecutable(file.getAbsolutePath());
				}
			}
		};

		setFileFilter(filter);
	}

	void selectFile(Component parent)
	{
		val selectedAnswer = showOpenDialog(parent);
		if (selectedAnswer == OK_OPTION)
		{
			val selectedFile = getSelectedFile();
			var path = selectedFile.getAbsolutePath();
			val programDirectory = getProgramDirectory() + File.separator;

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
		val currentFile = new File(pathField.getText());
		val parentDirectory = currentFile.getParentFile();

		if (currentFile.exists() && parentDirectory != null)
		{
			setCurrentDirectory(parentDirectory);
		} else
		{
			val currentDirectory = new File(getProgramDirectory());
			setCurrentDirectory(currentDirectory);
		}

		if (extensions == null && extensionDescription == null)
		{
			setFileSelectionMode(DIRECTORIES_ONLY);
		} else
		{
			setFileSelectionMode(FILES_ONLY);
		}
	}
}

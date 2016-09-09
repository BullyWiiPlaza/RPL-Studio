package com.wiiudev.rpl.gui;

import com.wiiudev.rpl.ExecutableFileType;
import com.wiiudev.rpl.ExecutableTools;
import com.wiiudev.rpl.FileExtensions;
import com.wiiudev.rpl.gui.utilities.FileChooserWrapper;
import com.wiiudev.rpl.gui.utilities.FileNameUtilities;
import com.wiiudev.rpl.gui.utilities.IconImageUtilities;
import com.wiiudev.rpl.gui.utilities.SimpleProperties;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;

public class RPLStudioGUI extends JFrame
{
	private JPanel rootPanel;

	private JTextField rplFilePathField;
	private JButton rplBrowseButton;
	private JComboBox<ExecutableFileType> fileTypeComboBox;
	private JButton repackButton;
	private JButton unpackButton;
	private JButton cafeSDKRootBrowseButton;
	private JTextField cafeSDKRootField;

	private SimpleProperties simpleProperties;

	private boolean isUnpacking;
	private boolean isRepacking;

	public RPLStudioGUI()
	{
		add(rootPanel);

		setFrameProperties();
		simpleProperties = new SimpleProperties();

		populateFileTypes();
		restorePersistentSettings();

		addFilePathFieldDocumentListener();
		addBrowseButtonActionListener();
		addSDKRootDirectoryListener();
		addUnpackButtonListener();
		addFileTypeItemListener();
		addRepackButtonListener();

		setConversionButtonsTexts();
		setUnpackButtonAvailability();
		setRepackButtonAvailability();
		addShutdownBackupHook();

		cafeSDKRootBrowseButton.addActionListener(actionEvent ->
		{
			FileChooserWrapper fileChooserWrapper = new FileChooserWrapper(cafeSDKRootField);
			fileChooserWrapper.selectFile(this);
			setRepackButtonAvailability();
		});
	}

	private void setConversionButtonsTexts()
	{
		ExecutableFileType executableFileType = getSelectedFileType();
		unpackButton.setText(executableFileType + " -> ELF");
		repackButton.setText("ELF -> " + executableFileType);
	}

	private void addSDKRootDirectoryListener()
	{
		cafeSDKRootField.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent documentEvent)
			{
				setRepackButtonAvailability();
			}

			@Override
			public void removeUpdate(DocumentEvent documentEvent)
			{
				setRepackButtonAvailability();
			}

			@Override
			public void changedUpdate(DocumentEvent documentEvent)
			{
				setRepackButtonAvailability();
			}
		});
	}

	private void restorePersistentSettings()
	{
		String rplFilePath = simpleProperties.get("RPL_FILE_PATH");
		if (rplFilePath != null)
		{
			rplFilePathField.setText(rplFilePath);
		}

		String cafeRootDirectory = simpleProperties.get("CAFE_ROOT_DIRECTORY");

		if (cafeRootDirectory != null)
		{
			cafeSDKRootField.setText(cafeRootDirectory);
		}

		ExecutableFileType executableFileType = ExecutableFileType.parse(simpleProperties.get("FILE_TYPE"));

		if (executableFileType != null)
		{
			fileTypeComboBox.setSelectedItem(executableFileType);
		}
	}

	private void addShutdownBackupHook()
	{
		Runtime.getRuntime().addShutdownHook(new Thread(() ->
		{
			simpleProperties.put("RPL_FILE_PATH", rplFilePathField.getText());
			simpleProperties.put("CAFE_ROOT_DIRECTORY", cafeSDKRootField.getText());
			simpleProperties.put("FILE_TYPE", getSelectedFileType().toString());
			simpleProperties.writeToFile();
		}));
	}

	private void setRepackButtonAvailability()
	{
		boolean elfExists = doesElfExist();
		boolean sdkRootValid = new File(cafeSDKRootField.getText()).exists();
		cafeSDKRootField.setBackground(sdkRootValid ? Color.GREEN : Color.RED);
		boolean isRepackPossible = elfExists && sdkRootValid && !isRepacking;

		repackButton.setEnabled(isRepackPossible);
	}

	private boolean doesElfExist()
	{
		String elfFileName = getElfFileNameDerived();

		return new File(elfFileName).exists();
	}

	private String getElfFileNameDerived()
	{
		String executableFileName = rplFilePathField.getText();
		String baseFileName = FileNameUtilities.getBaseFileName(executableFileName);

		return baseFileName + FileExtensions.ELF;
	}

	public String getElfFilePath()
	{
		if (doesElfExist())
		{
			return getElfFileNameDerived();
		}

		throw new IllegalStateException("ELF file does not exist!");
	}

	private void addRepackButtonListener()
	{
		repackButton.addActionListener(actionEvent ->
				new SwingWorker<String, String>()
				{
					@Override
					protected String doInBackground() throws Exception
					{
						String repackButtonText = repackButton.getText();
						isRepacking = true;
						setUnpackButtonAvailability();
						repackButton.setText("Repacking...");
						setRepackButtonAvailability();

						try
						{
							ExecutableFileType executableFileType = getSelectedFileType();
							ExecutableTools.repack(executableFileType, getElfFilePath(), cafeSDKRootField.getText());
							String fileName = getFileName();
							JOptionPane.showMessageDialog(rootPane,
									fileName + "." + getSelectedFileType().toString().toLowerCase() + " repacked successfully!",
									"Success",
									JOptionPane.INFORMATION_MESSAGE,
									null);
						} catch (Exception exception)
						{
							exception.printStackTrace();
						} finally
						{
							repackButton.setText(repackButtonText);
							isRepacking = false;
							setRepackButtonAvailability();
						}

						return null;
					}
				}.execute());
	}

	private String getFileName()
	{
		String filePath = rplFilePathField.getText();
		String fileNameWithExtension = new File(filePath).getName();
		return FileNameUtilities.getBaseFileName(fileNameWithExtension);
	}

	private void addFileTypeItemListener()
	{
		fileTypeComboBox.addItemListener(itemEvent ->
		{
			setUnpackButtonAvailability();
			setConversionButtonsTexts();
		});
	}

	private void addUnpackButtonListener()
	{
		unpackButton.addActionListener(actionEvent ->
				new SwingWorker<String, String>()
				{
					@Override
					protected String doInBackground() throws Exception
					{
						String unpackButtonText = unpackButton.getText();
						isUnpacking = true;
						setUnpackButtonAvailability();
						unpackButton.setText("Unpacking...");

						try
						{
							String rplFilePath = rplFilePathField.getText();
							String elfFileName = ExecutableTools.convertRPLToELF(rplFilePath);
							unpackButton.setText(unpackButtonText);
							isUnpacking = false;
							setUnpackButtonAvailability();

							ExecutableTools.openWithHexEditor(elfFileName);
						} catch (Exception exception)
						{
							exception.printStackTrace();
						} finally
						{
							unpackButton.setText(unpackButtonText);
							isUnpacking = false;
							setRepackButtonAvailability();
							setUnpackButtonAvailability();
						}

						return null;
					}
				}.execute());
	}

	private void addBrowseButtonActionListener()
	{
		rplBrowseButton.addActionListener(actionEvent ->
		{
			ExecutableFileType executableFileType = getSelectedFileType();
			FileChooserWrapper fileChooserWrapper = new FileChooserWrapper(rplFilePathField, "Wii U Executables",
					executableFileType.toString().toLowerCase());
			fileChooserWrapper.selectFile(this);
			setRepackButtonAvailability();
		});
	}

	private void addFilePathFieldDocumentListener()
	{
		rplFilePathField.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent documentEvent)
			{
				setUnpackButtonAvailability();
			}

			@Override
			public void removeUpdate(DocumentEvent documentEvent)
			{
				setUnpackButtonAvailability();
			}

			@Override
			public void changedUpdate(DocumentEvent documentEvent)
			{
				setUnpackButtonAvailability();
			}
		});
	}

	private void setUnpackButtonAvailability()
	{
		boolean isUnpackPossible = isUnpackPossible();
		rplFilePathField.setBackground(isUnpackPossible ? Color.GREEN : Color.RED);
		unpackButton.setEnabled(isUnpackPossible);
	}

	private boolean isUnpackPossible()
	{
		String selectedFilePath = rplFilePathField.getText();
		File selectedFile = new File(selectedFilePath);
		boolean fileExists = selectedFile.exists();
		ExecutableFileType selectedFileType = getSelectedFileType();
		boolean isCorrectFileType = selectedFile.getAbsolutePath().toLowerCase().endsWith(selectedFileType.toString().toLowerCase());

		return fileExists && isCorrectFileType && !isUnpacking;
	}

	private ExecutableFileType getSelectedFileType()
	{
		int selectedIndex = fileTypeComboBox.getSelectedIndex();

		return fileTypeComboBox.getItemAt(selectedIndex);
	}

	private void setFrameProperties()
	{
		setTitle("RPL Studio v1.0 by Bully@WiiPlaza");
		setSize(400, 200);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		IconImageUtilities.setIconImage(this, "Icon.png");
	}

	private void populateFileTypes()
	{
		DefaultComboBoxModel<ExecutableFileType> defaultComboBoxModel = new DefaultComboBoxModel<>(ExecutableFileType.values());
		fileTypeComboBox.setModel(defaultComboBoxModel);
	}
}
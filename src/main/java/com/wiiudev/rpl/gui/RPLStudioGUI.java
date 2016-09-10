package com.wiiudev.rpl.gui;

import com.wiiudev.rpl.ExecutableFileExtension;
import com.wiiudev.rpl.downloading.ApplicationLauncher;
import com.wiiudev.rpl.downloading.ApplicationUtilities;
import com.wiiudev.rpl.downloading.DownloadingUtilities;
import com.wiiudev.rpl.downloading.ZipUtilities;
import com.wiiudev.rpl.RPXTool;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RPLStudioGUI extends JFrame
{
	private JPanel rootPanel;

	private JTextField rplFilePathField;
	private JButton rplBrowseButton;
	private JButton repackButton;
	private JButton unpackButton;
	private JButton openButton;

	private SimpleProperties simpleProperties;

	private boolean isUnpacking;
	private boolean isRepacking;

	private RPXTool rpxTool;

	public RPLStudioGUI() throws IOException
	{
		add(rootPanel);

		setFrameProperties();
		simpleProperties = new SimpleProperties();

		restorePersistentSettings();

		addFilePathFieldDocumentListener();
		addBrowseButtonActionListener();
		addUnpackButtonListener();
		addRepackButtonListener();
		addEditButtonListener();

		setButtonsAvailability();

		addShutdownBackupHook();

		rpxTool = new RPXTool();
		rpxTool.initialize();
	}

	private void addEditButtonListener()
	{
		openButton.addActionListener(actionEvent ->
		{
			ApplicationLauncher applicationLauncher = new ApplicationLauncher("C:\\Program Files (x86)\\HxD\\HxD.exe",
					"https://mh-nexus.de/downloads/HxDSetupEN.zip", "HxD", true);
			startApplication(actionEvent, applicationLauncher);
		});
	}

	private void startApplication(ActionEvent actionEvent, ApplicationLauncher applicationLauncher)
	{
		JButton editButton = (JButton) actionEvent.getSource();
		Path installedExecutablePath = applicationLauncher.getInstalledExecutablePath();
		String setupFileName = applicationLauncher.getSetupFileName();

		try
		{
			if (isRunning(setupFileName))
			{
				return;
			}

			String buttonText = editButton.getText();
			boolean installed = Files.exists(installedExecutablePath);

			if (installed)
			{
				editButton.setText("Starting...");
				ProcessBuilder processBuilder = new ProcessBuilder();
				processBuilder.command(installedExecutablePath.toString(), rplFilePathField.getText());
				processBuilder.start();
				editButton.setText(buttonText);
			} else
			{
				new SwingWorker<String, String>()
				{
					@Override
					protected String doInBackground() throws Exception
					{
						try
						{
							editButton.setEnabled(false);
							editButton.setText("Downloading...");
							DownloadingUtilities.trustAllCertificates();
							DownloadingUtilities.download(applicationLauncher.getDownloadURL());

							File executeFile;

							if (applicationLauncher.shouldUnZip())
							{
								editButton.setText("Unzipping...");
								executeFile = ZipUtilities.unZip(Paths.get(setupFileName));
							} else
							{
								executeFile = new File(setupFileName);
							}

							editButton.setText("Executing...");
							Desktop.getDesktop().open(executeFile);
						} catch (Exception exception)
						{
							exception.printStackTrace();
						}

						return null;
					}

					@Override
					protected void done()
					{
						editButton.setEnabled(true);
						editButton.setText(buttonText);
					}
				}.execute();
			}
		} catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	private boolean isRunning(String applicationName) throws IOException
	{
		String forcedExtension = ".exe";

		if (!applicationName.endsWith(forcedExtension))
		{
			applicationName += forcedExtension;
		}

		if (ApplicationUtilities.isProcessRunning(applicationName))
		{
			JOptionPane.showMessageDialog(rootPane,
					applicationName + " is running already",
					"Error",
					JOptionPane.ERROR_MESSAGE);

			return true;
		}

		return false;
	}

	private void restorePersistentSettings()
	{
		String rplFilePath = simpleProperties.get("RPL_FILE_PATH");
		if (rplFilePath != null)
		{
			rplFilePathField.setText(rplFilePath);
		}
	}

	private void addShutdownBackupHook()
	{
		Runtime.getRuntime().addShutdownHook(new Thread(() ->
		{
			simpleProperties.put("RPL_FILE_PATH", rplFilePathField.getText());
			simpleProperties.writeToFile();
		}));
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
						setButtonsAvailability();
						repackButton.setText("Repacking...");
						setButtonsAvailability();

						try
						{
							String filePath = rplFilePathField.getText();
							rpxTool.repack(filePath);
							String fileName = getExecutableFileName();
							JOptionPane.showMessageDialog(rootPane,
									fileName + " repacked successfully!",
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
							setButtonsAvailability();
						}

						return null;
					}
				}.execute());
	}

	private String getExecutableFileName()
	{
		String filePath = rplFilePathField.getText();
		return new File(filePath).getName();
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
						setButtonsAvailability();
						unpackButton.setText("Unpacking...");

						try
						{
							String rplFilePath = rplFilePathField.getText();
							rpxTool.unpack(rplFilePath);
							unpackButton.setText(unpackButtonText);
							isUnpacking = false;
							String fileName = getExecutableFileName();
							JOptionPane.showMessageDialog(rootPane,
									fileName + " unpacked successfully!",
									"Success",
									JOptionPane.INFORMATION_MESSAGE,
									null);
						} catch (Exception exception)
						{
							exception.printStackTrace();
						} finally
						{
							unpackButton.setText(unpackButtonText);
							isUnpacking = false;
							setButtonsAvailability();
						}

						return null;
					}
				}.execute());
	}

	private void addBrowseButtonActionListener()
	{
		rplBrowseButton.addActionListener(actionEvent ->
		{
			FileChooserWrapper fileChooserWrapper = new FileChooserWrapper(rplFilePathField,
					"Wii U Executables", ExecutableFileExtension.getExtensions());
			fileChooserWrapper.selectFile(this);
			setButtonsAvailability();
		});
	}

	private void addFilePathFieldDocumentListener()
	{
		rplFilePathField.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent documentEvent)
			{
				setButtonsAvailability();
			}

			@Override
			public void removeUpdate(DocumentEvent documentEvent)
			{
				setButtonsAvailability();
			}

			@Override
			public void changedUpdate(DocumentEvent documentEvent)
			{
				setButtonsAvailability();
			}
		});
	}

	private void setButtonsAvailability()
	{
		boolean isUnpackPossible = isUnpackPossible();
		rplFilePathField.setBackground(doesExecutableFileExist() ? Color.GREEN : Color.RED);
		unpackButton.setEnabled(isUnpackPossible);
		repackButton.setEnabled(isUnpackPossible);
		openButton.setEnabled(isUnpackPossible);
	}

	private boolean isUnpackPossible()
	{
		boolean fileExists = doesExecutableFileExist();
		return fileExists && !isUnpacking && !isRepacking;
	}

	private boolean doesExecutableFileExist()
	{
		String selectedFilePath = rplFilePathField.getText();
		File selectedFile = new File(selectedFilePath);
		boolean exists = selectedFile.exists();
		boolean correctExtension = ExecutableFileExtension.isExecutable(selectedFilePath);

		return exists && correctExtension;
	}

	private void setFrameProperties()
	{
		setTitle("RPL Studio by Bully@WiiPlaza");
		setSize(400, 100);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		IconImageUtilities.setIconImage(this, "Icon.png");
	}
}
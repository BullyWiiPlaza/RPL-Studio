package com.wiiudev.rpl.gui;

import com.wiiudev.rpl.ExecutableFileExtension;
import com.wiiudev.rpl.RPXTool;
import com.wiiudev.rpl.downloading.ApplicationLauncher;
import com.wiiudev.rpl.downloading.ApplicationUtilities;
import com.wiiudev.rpl.downloading.DownloadingUtilities;
import com.wiiudev.rpl.downloading.ZipUtilities;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RPLStudioGUI extends JFrame
{
	private JPanel rootPanel;

	private JTextField executableFilePathField;
	private JButton executableBrowseButton;
	private JButton repackButton;
	private JButton unpackButton;
	private JButton openButton;
	private JLabel initializingLabel;

	private SimpleProperties simpleProperties;

	private boolean isUnpacking;
	private boolean isRepacking;

	private RPXTool rpxTool;

	public RPLStudioGUI() throws IOException
	{
		add(rootPanel);

		setFrameProperties();
		handlePersistentSettings();
		addBrowseButtonActionListener();
		addUnpackButtonListener();
		addRepackButtonListener();
		addOpenButtonListener();

		runButtonsAvailabilityThread();
		initializeRPXTool();
	}

	private void handlePersistentSettings()
	{
		simpleProperties = new SimpleProperties();
		restorePersistentSettings();
		addShutdownBackupHook();
	}

	private void initializeRPXTool() throws IOException
	{
		initializingLabel.setText("Initializing " + RPXTool.APPLICATION_NAME + "...");

		Thread thread = new Thread(() ->
		{
			try
			{
				rpxTool = RPXTool.getInstance();
				rpxTool.initialize();
				initializingLabel.setText("");
				RPLStudioSize.setDefaultSize(this);
			} catch (IOException exception)
			{
				exception.printStackTrace();
				initializingLabel.setText(RPXTool.APPLICATION_NAME + " initialization failed!");
			}
		});

		thread.setName("RPLXTool Initializer");
		thread.start();
	}

	private void runButtonsAvailabilityThread()
	{
		Thread buttonsAvailabilitySetter = new Thread(() ->
		{
			while (true)
			{
				setButtonsAvailability();

				try
				{
					Thread.sleep(100);
				} catch (InterruptedException exception)
				{
					exception.printStackTrace();
				}
			}
		});

		buttonsAvailabilitySetter.start();
	}

	private void addOpenButtonListener()
	{
		openButton.addActionListener(actionEvent -> openDecompressed());
	}

	private void openDecompressed()
	{
		ApplicationLauncher applicationLauncher = new ApplicationLauncher("C:\\Program Files (x86)\\HxD\\HxD.exe",
				"https://mh-nexus.de/downloads/HxDSetupEN.zip", "HxD", true);
		startApplication(applicationLauncher);
	}

	private void startApplication(ApplicationLauncher applicationLauncher)
	{
		JButton editButton = openButton;
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
				processBuilder.command(installedExecutablePath.toString(),
						getDecompressedFileName());
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

	private String getDecompressedFileName()
	{
		String executableFileName = executableFilePathField.getText();
		return RPXTool.getDecompressedFileName(executableFileName);
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
			executableFilePathField.setText(rplFilePath);
		}
	}

	private void addShutdownBackupHook()
	{
		Runtime.getRuntime().addShutdownHook(new Thread(() ->
		{
			simpleProperties.put("RPL_FILE_PATH", executableFilePathField.getText());
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
						repackButton.setText("Compressing...");

						try
						{
							String decompressedFileName = getDecompressedFileName();
							Path decompressedFilePath = Paths.get(decompressedFileName);
							byte[] compressedFileBytes = Files.readAllBytes(decompressedFilePath);
							rpxTool.repack(decompressedFileName);
							Files.write(decompressedFilePath, compressedFileBytes);
							String fileName = getExecutableFileName();
							JOptionPane.showMessageDialog(rootPane,
									"\"" + getDecompressedFileName() + "\"" +
											" compressed to \""
											+ fileName + "\" successfully!",
									"Decompressed",
									JOptionPane.INFORMATION_MESSAGE,
									null);
						} catch (Exception exception)
						{
							exception.printStackTrace();
						} finally
						{
							repackButton.setText(repackButtonText);
							isRepacking = false;
						}

						return null;
					}
				}.execute());
	}

	private String getExecutableFileName()
	{
		String filePath = executableFilePathField.getText();
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
						unpackButton.setText("Decompressing...");

						try
						{
							String rplFilePath = executableFilePathField.getText();
							Path inputFilePath = Paths.get(rplFilePath);
							byte[] packedExecutableBytes = Files.readAllBytes(inputFilePath);
							rpxTool.unpack(rplFilePath);
							Files.write(inputFilePath, packedExecutableBytes);
							unpackButton.setText(unpackButtonText);
							isUnpacking = false;
							openDecompressed();
						} catch (Exception exception)
						{
							exception.printStackTrace();
						} finally
						{
							unpackButton.setText(unpackButtonText);
							isUnpacking = false;
						}

						return null;
					}
				}.execute());
	}

	private void addBrowseButtonActionListener()
	{
		executableBrowseButton.addActionListener(actionEvent ->
		{
			FileChooserWrapper fileChooserWrapper = new FileChooserWrapper(executableFilePathField,
					"Wii U Executables", ExecutableFileExtension.getExtensions());
			fileChooserWrapper.selectFile(this);
		});
	}

	private void setButtonsAvailability()
	{
		boolean isUnpackPossible = canUnpack();
		executableFilePathField.setBackground(doesExecutableFileExist() ? Color.GREEN : Color.RED);
		boolean isRPXToolInitialized = rpxTool != null && rpxTool.isInitialized();
		unpackButton.setEnabled(isUnpackPossible && isRPXToolInitialized);
		boolean canRepack = canRepack();
		repackButton.setEnabled(canRepack && isRPXToolInitialized);
		openButton.setEnabled(canRepack);
	}

	private boolean canRepack()
	{
		String decompressedFileName = getDecompressedFileName();
		return Files.exists(Paths.get(decompressedFileName));
	}

	private boolean canUnpack()
	{
		boolean fileExists = doesExecutableFileExist();
		return fileExists && !isUnpacking && !isRepacking;
	}

	private boolean doesExecutableFileExist()
	{
		String selectedFilePath = executableFilePathField.getText();
		File selectedFile = new File(selectedFilePath);
		boolean exists = selectedFile.exists();
		boolean correctExtension = ExecutableFileExtension.isExecutable(selectedFilePath);

		return exists && correctExtension;
	}

	private void setFrameProperties()
	{
		setTitle("RPL Studio by Bully@WiiPlaza");
		RPLStudioSize.setInitializingSize(this);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		IconImageUtilities.setIconImage(this, "Icon.png");
	}
}
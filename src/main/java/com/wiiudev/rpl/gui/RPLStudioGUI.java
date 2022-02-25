package com.wiiudev.rpl.gui;

import com.wiiudev.rpl.ExecutableFileExtension;
import com.wiiudev.rpl.RPXTool;
import com.wiiudev.rpl.UnpackExecutable;
import com.wiiudev.rpl.downloading.ApplicationLauncher;
import lombok.val;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static com.wiiudev.rpl.ExecutableFileExtension.isExecutable;
import static com.wiiudev.rpl.StackTraceUtilities.handleException;
import static com.wiiudev.rpl.downloading.ApplicationUtilities.isProcessRunning;
import static com.wiiudev.rpl.downloading.DownloadingUtilities.download;
import static com.wiiudev.rpl.downloading.DownloadingUtilities.trustAllCertificates;
import static com.wiiudev.rpl.downloading.ZipUtilities.unZip;
import static com.wiiudev.rpl.gui.RPLStudioSize.setDefaultSize;
import static com.wiiudev.rpl.gui.RPLStudioSize.setInitializingSize;
import static java.awt.Desktop.getDesktop;
import static java.lang.System.getenv;
import static java.nio.file.Files.*;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

public class RPLStudioGUI extends JFrame
{
	private static final String RPL_FILE_PATH_KEY = "RPL_FILE_PATH";

	private static final String APPLICATION_README = "https://github.com/BullyWiiPlaza/RPL-Studio/blob/master/README.md";
	private static final String HXD_DOWNLOAD_URL = "https://mh-nexus.de/downloads/HxDSetup.zip";
	private static final String PROGRAM_FILES_DIRECTORY = getenv("ProgramFiles");
	private static final String HXD_EXECUTABLE_FILE_PATH = PROGRAM_FILES_DIRECTORY + "\\HxD\\HxD.exe";

	private JPanel rootPanel;

	private JTextField executableFilePathField;
	private JButton executableBrowseButton;
	private JButton repackButton;
	private JButton unpackButton;
	private JButton openButton;
	private JLabel initializingLabel;
	private JComboBox<UnpackExecutable> decompressUtilitySelection;
	private JButton aboutButton;

	private SimpleProperties simpleProperties;

	private boolean isUnpacking;
	private boolean isRepacking;

	private RPXTool rpxTool;

	public RPLStudioGUI()
	{
		add(rootPanel);

		setFrameProperties();
		handlePersistentSettings();
		addBrowseButtonActionListener();
		initializeDecompressUtilitySelection();
		addUnpackButtonListener();
		addRepackButtonListener();
		addOpenButtonListener();
		addAboutButtonListener();

		runButtonsAvailabilityThread();
		initializeRPXTool();
	}

	private void initializeDecompressUtilitySelection()
	{
		decompressUtilitySelection.setModel(new DefaultComboBoxModel<>(UnpackExecutable.values()));
	}

	private void addAboutButtonListener()
	{
		aboutButton.addActionListener(actionEvent ->
		{
			try
			{
				val desktop = getDesktop();
				desktop.browse(new URI(APPLICATION_README));
			} catch (IOException | URISyntaxException exception)
			{
				exception.printStackTrace();
			}
		});
	}

	private void handlePersistentSettings()
	{
		simpleProperties = new SimpleProperties();
		restorePersistentSettings();
		addShutdownBackupHook();
	}

	private void initializeRPXTool()
	{
		initializingLabel.setText("Initializing " + RPXTool.WII_U_RPX_TOOL + "...");

		Thread thread = new Thread(() ->
		{
			try
			{
				rpxTool = RPXTool.getInstance();
				rpxTool.initialize();
				initializingLabel.setText("");
				setDefaultSize(this);
			} catch (Exception exception)
			{
				exception.printStackTrace();
				val exceptionMessage = exception.getClass().getSimpleName() + ": " + exception.getMessage();
				val labelMessage = RPXTool.WII_U_RPX_TOOL + " initialization failed: " + exceptionMessage;
				initializingLabel.setText(labelMessage);
			}
		});

		thread.setName("RPLXTool Initializer");
		thread.start();
	}

	private void runButtonsAvailabilityThread()
	{
		val buttonsAvailabilitySetter = new Thread(() ->
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
		try
		{
			val applicationLauncher = new ApplicationLauncher(HXD_EXECUTABLE_FILE_PATH, HXD_DOWNLOAD_URL,
					"HxD", true);
			startApplication(applicationLauncher);
		} catch (Exception exception)
		{
			handleException(rootPane, exception);
		}
	}

	private void startApplication(ApplicationLauncher applicationLauncher)
	{
		val editButton = openButton;
		val installedExecutablePath = applicationLauncher.getInstalledExecutablePath();
		val setupFileName = applicationLauncher.getSetupFileName();

		try
		{
			if (isRunning(setupFileName))
			{
				return;
			}

			val buttonText = editButton.getText();
			boolean installed = exists(installedExecutablePath);

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
					protected String doInBackground()
					{
						try
						{
							editButton.setEnabled(false);
							editButton.setText("Downloading...");
							trustAllCertificates();
							download(applicationLauncher.getDownloadURL());

							File executeFile;
							if (applicationLauncher.isUnZip())
							{
								editButton.setText("Unzipping...");
								executeFile = unZip(Paths.get(setupFileName));
							} else
							{
								executeFile = new File(setupFileName);
							}

							editButton.setText("Executing...");

							val desktop = getDesktop();
							desktop.open(executeFile);
						} catch (Exception exception)
						{
							handleException(rootPane, exception);
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
		val executableFileName = executableFilePathField.getText();
		return RPXTool.getDecompressedFileName(executableFileName);
	}

	private boolean isRunning(String applicationName) throws IOException
	{
		val forcedExtension = ".exe";

		if (!applicationName.endsWith(forcedExtension))
		{
			applicationName += forcedExtension;
		}

		if (isProcessRunning(applicationName))
		{
			showMessageDialog(rootPane,
					applicationName + " is running already",
					"Error",
					JOptionPane.ERROR_MESSAGE);

			return true;
		}

		return false;
	}

	private void restorePersistentSettings()
	{
		val rplFilePath = simpleProperties.get(RPL_FILE_PATH_KEY);
		if (rplFilePath != null)
		{
			executableFilePathField.setText(rplFilePath);
		}
	}

	private void addShutdownBackupHook()
	{
		val runtime = Runtime.getRuntime();
		runtime.addShutdownHook(new Thread(() ->
		{
			simpleProperties.put(RPL_FILE_PATH_KEY, executableFilePathField.getText());
			simpleProperties.writeToFile();
		}));
	}

	private void addRepackButtonListener()
	{
		repackButton.addActionListener(actionEvent ->
				new SwingWorker<String, String>()
				{
					@Override
					protected String doInBackground()
					{
						String repackButtonText = repackButton.getText();
						isRepacking = true;
						repackButton.setText("Compressing...");

						try
						{
							val decompressedFileName = getDecompressedFileName();
							val decompressedFilePath = Paths.get(decompressedFileName);
							val compressedFileBytes = readAllBytes(decompressedFilePath);
							rpxTool.repack(decompressedFileName);
							write(decompressedFilePath, compressedFileBytes);
							val fileName = getExecutableFileName();
							showMessageDialog(rootPane,
									"\"" + getDecompressedFileName() + "\"" +
											" compressed to \"" + fileName + "\" successfully!",
									"Decompressed", INFORMATION_MESSAGE, null);
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
		val filePath = executableFilePathField.getText();
		return new File(filePath).getName();
	}

	private void addUnpackButtonListener()
	{
		unpackButton.addActionListener(actionEvent ->
				new SwingWorker<String, String>()
				{
					@Override
					protected String doInBackground()
					{
						val unpackButtonText = unpackButton.getText();
						isUnpacking = true;
						unpackButton.setText("Decompressing...");

						try
						{
							val rplFilePath = executableFilePathField.getText();
							val inputFilePath = Paths.get(rplFilePath);
							val packedExecutableBytes = readAllBytes(inputFilePath);
							val unpackExecutable = getSelectedItem(decompressUtilitySelection);
							rpxTool.unpack(rplFilePath, unpackExecutable);
							write(inputFilePath, packedExecutableBytes);
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

	private UnpackExecutable getSelectedItem(JComboBox<UnpackExecutable> decompressUtilitySelection)
	{
		val selectedIndex = decompressUtilitySelection.getSelectedIndex();
		return decompressUtilitySelection.getItemAt(selectedIndex);
	}

	private void addBrowseButtonActionListener()
	{
		executableBrowseButton.addActionListener(actionEvent ->
		{
			val fileChooserWrapper = new FileChooserWrapper(executableFilePathField,
					"Wii U Executables", ExecutableFileExtension.getExtensions());
			fileChooserWrapper.selectFile(this);
		});
	}

	private void setButtonsAvailability()
	{
		val isUnpackPossible = canUnpack();
		executableFilePathField.setBackground(doesExecutableFileExist() ? Color.GREEN : Color.RED);
		val isRPXToolInitialized = rpxTool != null && rpxTool.isInitialized();
		unpackButton.setEnabled(isUnpackPossible && isRPXToolInitialized);
		val canRepack = canRepack();
		repackButton.setEnabled(canRepack && isRPXToolInitialized);
		openButton.setEnabled(canRepack);
	}

	private boolean canRepack()
	{
		val decompressedFileName = getDecompressedFileName();
		return exists(Paths.get(decompressedFileName));
	}

	private boolean canUnpack()
	{
		val fileExists = doesExecutableFileExist();
		return fileExists && !isUnpacking && !isRepacking;
	}

	private boolean doesExecutableFileExist()
	{
		val selectedFilePath = executableFilePathField.getText();
		val selectedFile = new File(selectedFilePath);
		val exists = selectedFile.exists();
		val correctExtension = isExecutable(selectedFilePath);

		return exists && correctExtension;
	}

	private void setFrameProperties()
	{
		setTitle("RPL Studio by Bully@WiiPlaza");
		setInitializingSize(this);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		IconImageUtilities.setIconImage(this, "Icon.png");
	}
}

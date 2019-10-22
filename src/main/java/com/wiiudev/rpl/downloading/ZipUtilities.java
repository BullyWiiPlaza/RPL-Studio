package com.wiiudev.rpl.downloading;

import lombok.val;
import lombok.var;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipInputStream;

import static java.lang.System.getProperty;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.delete;

public class ZipUtilities
{

	private static final String USER_DIRECTORY = getProperty("user.dir");

	public static File unZip(Path zipFile) throws IOException
	{
		val buffer = new byte[1024];
		val outputPath = Paths.get(USER_DIRECTORY);
		createDirectories(outputPath);

		val zipInputStream = new ZipInputStream(new FileInputStream(zipFile.toFile()));

		var zipEntry = zipInputStream.getNextEntry();

		File extracted = null;

		while (zipEntry != null)
		{
			val fileName = zipEntry.getName();
			val newFile = outputPath.resolve(fileName);

			val fileOutputStream = new FileOutputStream(newFile.toFile());

			int length;
			while ((length = zipInputStream.read(buffer)) > 0)
			{
				fileOutputStream.write(buffer, 0, length);
			}

			fileOutputStream.close();
			extracted = new File(zipEntry.getName());
			zipEntry = zipInputStream.getNextEntry();
		}

		zipInputStream.closeEntry();
		zipInputStream.close();

		delete(zipFile);

		return extracted;
	}
}
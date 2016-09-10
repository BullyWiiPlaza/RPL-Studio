package com.wiiudev.rpl.downloading;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtilities
{
	public static File unZip(Path zipFile) throws IOException
	{
		byte[] buffer = new byte[1024];

		String outputFolder = System.getProperty("user.dir");
		Path outputPath = Paths.get(outputFolder);
		Files.createDirectories(outputPath);

		ZipInputStream zipInputStream =
				new ZipInputStream(new FileInputStream(zipFile.toFile()));

		ZipEntry zipEntry = zipInputStream.getNextEntry();

		File extracted = null;

		while (zipEntry != null)
		{
			String fileName = zipEntry.getName();
			Path newFile = outputPath.resolve(fileName);

			FileOutputStream fileOutputStream = new FileOutputStream(newFile.toFile());

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

		Files.delete(zipFile);

		return extracted;
	}
}
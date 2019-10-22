package com.wiiudev.rpl.downloading;

import lombok.val;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import static java.nio.file.Files.copy;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class DownloadingUtilities
{
	public static Path download(String downloadURL) throws IOException
	{
		val fileName = getFileName(downloadURL);
		val website = new URL(downloadURL);
		val targetPath = Paths.get(fileName);

		try (val inputStream = website.openStream())
		{
			copy(inputStream, targetPath, REPLACE_EXISTING);
		}

		return targetPath;
	}

	public static String getFileName(String url)
	{
		int lastPathSlashIndex = url.lastIndexOf('/');
		return url.substring(lastPathSlashIndex + 1, url.length());
	}

	/**
	 * Allows all SSL certificates for downloading files.
	 *
	 * @see <a href="http://stackoverflow.com/a/2893932/3764804" StackOverflow></a>
	 */
	public static void trustAllCertificates() throws NoSuchAlgorithmException, KeyManagementException
	{
		TrustManager[] trustManagers = new TrustManager[]{new X509TrustManager()
		{
			public X509Certificate[] getAcceptedIssuers()
			{
				return new X509Certificate[0];
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType)
			{
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType)
			{
			}
		}};

		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, trustManagers, new SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
	}
}
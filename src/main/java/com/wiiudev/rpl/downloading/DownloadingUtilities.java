package com.wiiudev.rpl.downloading;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class DownloadingUtilities
{
	public static Path download(String downloadURL) throws IOException
	{
		String fileName = getFileName(downloadURL);
		URL website = new URL(downloadURL);
		Path targetPath = Paths.get(fileName);

		try (InputStream inputStream = website.openStream())
		{
			Files.copy(inputStream,
					targetPath,
					StandardCopyOption.REPLACE_EXISTING);
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
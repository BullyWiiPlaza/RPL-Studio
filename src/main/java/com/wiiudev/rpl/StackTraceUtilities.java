package com.wiiudev.rpl;

import lombok.var;
import lombok.val;

import javax.swing.*;

import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;
import static javax.swing.JOptionPane.*;
import static javax.swing.SwingUtilities.*;

public class StackTraceUtilities
{
	private static final Logger LOGGER = getLogger(StackTraceUtilities.class.getName());

	private static String toString(Exception exception)
	{
		val stringBuilder = new StringBuilder(exception.toString() + "\n");
		for (val stackTraceElement : exception.getStackTrace())
		{
			stringBuilder.append("\n\tat ");
			stringBuilder.append(stackTraceElement);
		}

		return stringBuilder.toString();
	}

	private static final int MAXIMUM_CHARACTERS_COUNT = 1500;

	private static String truncateStackTrace(Exception exception)
	{
		var stackTrace = toString(exception);

		if (stackTrace.length() > MAXIMUM_CHARACTERS_COUNT)
		{
			val lastIndex = stackTrace.indexOf("\n", MAXIMUM_CHARACTERS_COUNT);
			stackTrace = stackTrace.substring(0, lastIndex) + "\n[...]";
		}

		return stackTrace;
	}

	public static void handleException(JRootPane rootPane, Exception exception)
	{
		val stackTrace = truncateStackTrace(exception);
		LOGGER.severe(stackTrace);

		invokeLater(() -> showMessageDialog(rootPane, stackTrace, "Error", ERROR_MESSAGE));
	}
}
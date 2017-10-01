import com.wiiudev.rpl.OperatingSystemUtilities;
import com.wiiudev.rpl.RPXTool;
import com.wiiudev.rpl.gui.RPLStudioGUI;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

// TODO Use RPL2Elf by Hykem for unpacking?
public class RPLStudioGUIClient
{
	public static void main(String[] args) throws Exception
	{
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		SwingUtilities.invokeLater(() ->
		{
			if (OperatingSystemUtilities.isRunningWindows())
			{
				try
				{
					RPLStudioGUI rplStudioGUI = new RPLStudioGUI();
					rplStudioGUI.setVisible(true);
				} catch (IOException exception)
				{
					exception.printStackTrace();
				}
			} else
			{
				JOptionPane.showMessageDialog(null,
						"Sorry, this currently only works on Windows due to \"" + RPXTool.APPLICATION_NAME + "\" being a Windows executable.\n",
						"Error",
						JOptionPane.ERROR_MESSAGE,
						null);
			}
		});
	}
}
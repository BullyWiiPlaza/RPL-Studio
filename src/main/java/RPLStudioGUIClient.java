import com.wiiudev.rpl.gui.RPLStudioGUI;

import static com.wiiudev.rpl.OperatingSystemUtilities.isRunningWindows;
import static com.wiiudev.rpl.RPXTool.APPLICATION_NAME;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.UIManager.getSystemLookAndFeelClassName;
import static javax.swing.UIManager.setLookAndFeel;

public class RPLStudioGUIClient
{
	public static void main(String[] arguments) throws Exception
	{
		setLookAndFeel(getSystemLookAndFeelClassName());

		invokeLater(() ->
		{
			if (isRunningWindows())
			{
				RPLStudioGUI rplStudioGUI = new RPLStudioGUI();
				rplStudioGUI.setVisible(true);
			} else
			{
				showMessageDialog(null,
						"Sorry, this currently only works on Windows due to \""
								+ APPLICATION_NAME + "\" being a Windows executable.\n",
						"Unsupported", ERROR_MESSAGE, null);
			}
		});
	}
}

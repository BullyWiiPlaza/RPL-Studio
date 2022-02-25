import com.wiiudev.rpl.gui.RPLStudioGUI;
import lombok.val;

import static com.wiiudev.rpl.RPXTool.RPL_2_ELF;
import static com.wiiudev.rpl.RPXTool.WII_U_RPX_TOOL;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.UIManager.getSystemLookAndFeelClassName;
import static javax.swing.UIManager.setLookAndFeel;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;

// TODO: Rework compress/decompress cycle (maybe don't overwrite files?)
public class RPLStudioGUIClient
{
	public static void main(String[] arguments) throws Exception
	{
		setLookAndFeel(getSystemLookAndFeelClassName());

		invokeLater(() ->
		{
			if (IS_OS_WINDOWS)
			{
				val rplStudioGUI = new RPLStudioGUI();
				rplStudioGUI.setVisible(true);
			} else
			{
				showMessageDialog(null,
						"Sorry, this currently only works on Windows due to \""
								+ WII_U_RPX_TOOL + "\" and " + RPL_2_ELF + " being Windows executables.\n",
						"Unsupported", ERROR_MESSAGE, null);
			}
		});
	}
}

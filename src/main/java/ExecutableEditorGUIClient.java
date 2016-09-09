import com.wiiudev.rpl.gui.RPLStudioGUI;

import javax.swing.*;

public class ExecutableEditorGUIClient
{
	public static void main(String[] args) throws Exception
	{
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		RPLStudioGUI RPLStudioGUI = new RPLStudioGUI();
		RPLStudioGUI.setVisible(true);
	}
}
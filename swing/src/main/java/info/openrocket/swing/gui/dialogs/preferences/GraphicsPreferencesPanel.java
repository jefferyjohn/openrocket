package info.openrocket.swing.gui.dialogs.preferences;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import info.openrocket.core.startup.ORPreferences;
import net.miginfocom.swing.MigLayout;

import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.arch.SystemInfo.Platform;
import info.openrocket.core.startup.Application;

import info.openrocket.swing.gui.adaptors.BooleanModel;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.components.StyledLabel.Style;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.gui.widgets.SelectColorButton;

import com.itextpdf.text.Font;

@SuppressWarnings("serial")
public class GraphicsPreferencesPanel extends PreferencesPanel {

	public GraphicsPreferencesPanel(JDialog parent) {
		super(parent, new MigLayout("fillx"));
		
		JPanel editorPrefPanel = new JPanel(new MigLayout("fill, ins n n n")) {
			{ //Editor Options		
				TitledBorder border = BorderFactory.createTitledBorder(trans.get("pref.dlg.lbl.DecalEditor"));
				GUIUtil.changeFontStyle(border, Font.BOLD);
				setBorder(border);
				
				ButtonGroup execGroup = new ButtonGroup();
				
				JRadioButton showPrompt = new JRadioButton(trans.get("EditDecalDialog.lbl.prompt"));
				showPrompt.setSelected(!preferences.isDecalEditorPreferenceSet());
				showPrompt.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						if (((JRadioButton) e.getItem()).isSelected()) {
							preferences.clearDecalEditorPreference();
						}
					}
				});
				add(showPrompt, "wrap");
				execGroup.add(showPrompt);
				
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.EDIT)) {
					
					JRadioButton systemRadio = new JRadioButton(trans.get("EditDecalDialog.lbl.system"));
					systemRadio.setSelected(preferences.isDecalEditorPreferenceSystem());
					systemRadio.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							if (((JRadioButton) e.getItem()).isSelected()) {
								preferences.setDecalEditorPreference(true, null);
							}
						}
					});
					add(systemRadio, "wrap");
					execGroup.add(systemRadio);
					
				}
				
				boolean commandLineIsSelected = preferences.isDecalEditorPreferenceSet() && !preferences.isDecalEditorPreferenceSystem();
				final JRadioButton commandRadio = new JRadioButton(trans.get("EditDecalDialog.lbl.cmdline"));
				commandRadio.setSelected(commandLineIsSelected);
				add(commandRadio, "wrap");
				execGroup.add(commandRadio);
				
				final JTextField commandText = new JTextField();
				commandText.setEnabled(commandLineIsSelected);
				commandText.setText(commandLineIsSelected ? preferences.getDecalEditorCommandLine() : "");
				commandText.getDocument().addDocumentListener(new DocumentListener() {
					
					@Override
					public void insertUpdate(DocumentEvent e) {
						preferences.setDecalEditorPreference(false, commandText.getText());
					}
					
					@Override
					public void removeUpdate(DocumentEvent e) {
						preferences.setDecalEditorPreference(false, commandText.getText());
					}
					
					@Override
					public void changedUpdate(DocumentEvent e) {
						preferences.setDecalEditorPreference(false, commandText.getText());
					}
					
				});
				add(commandText, "growx, wrap");
				
				final JButton chooser = new SelectColorButton(trans.get("EditDecalDialog.btn.chooser"));
				chooser.setEnabled(commandLineIsSelected);
				chooser.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						JFileChooser fc = new JFileChooser();
						int action = fc.showOpenDialog(SwingUtilities.windowForComponent(GraphicsPreferencesPanel.this.parentDialog));
						if (action == JFileChooser.APPROVE_OPTION) {
							String commandLine = fc.getSelectedFile().getAbsolutePath();
							commandText.setText(commandLine);
							preferences.setDecalEditorPreference(false, commandLine);
							((SwingPreferences) Application.getPreferences()).setDefaultDirectory(fc.getCurrentDirectory());
						}
						
					}
					
				});
				add(chooser, "wrap");
				
				
				commandRadio.addChangeListener(new ChangeListener() {
					
					@Override
					public void stateChanged(ChangeEvent e) {
						boolean enabled = commandRadio.isSelected();
						commandText.setEnabled(enabled);
						chooser.setEnabled(enabled);
					}
					
				});
			}
		};
		
		/* Don't show the editor preferences panel when confined in a snap on Linux.
		 * The snap confinement doesn't allow to run any edit commands, and instead
		 * we will rely on using the xdg-open command which allows the user to pick
		 * their preferred application.
		 */
		if ((SystemInfo.getPlatform() != Platform.UNIX) || !SystemInfo.isConfined()) {
			this.add(editorPrefPanel, "growx, span");
		}
		
		this.add(new JPanel(new MigLayout("fill, ins n n n")) {
			{/////GL Options
				TitledBorder border = BorderFactory.createTitledBorder(trans.get("pref.dlg.opengl.lbl.title"));
				GUIUtil.changeFontStyle(border, Font.BOLD);
				setBorder(border);
				
				//// The effects will take place the next time you open a window.
				add(new StyledLabel(
						trans.get("pref.dlg.lbl.effect1"), -2, Style.ITALIC),
						"spanx, wrap");
				
				BooleanModel enableGLModel = new BooleanModel(preferences.getBoolean(ORPreferences.OPENGL_ENABLED, true));
				final JCheckBox enableGL = new JCheckBox(enableGLModel);
				enableGL.setText(trans.get("pref.dlg.opengl.but.enableGL"));
				enableGL.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						preferences.putBoolean(ORPreferences.OPENGL_ENABLED, enableGL.isSelected());
					}
				});
				add(enableGL, "wrap");
				
				final JCheckBox enableAA = new JCheckBox(trans.get("pref.dlg.opengl.but.enableAA"));
				enableAA.setSelected(preferences.getBoolean(ORPreferences.OPENGL_ENABLE_AA, true));
				enableAA.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						preferences.putBoolean(ORPreferences.OPENGL_ENABLE_AA, enableAA.isSelected());
					}
				});
				enableGLModel.addEnableComponent(enableAA);
				add(enableAA, "wrap");
				
				final JCheckBox useFBO = new JCheckBox(trans.get("pref.dlg.opengl.lbl.useFBO"));
				useFBO.setSelected(preferences.getBoolean(ORPreferences.OPENGL_USE_FBO, false));
				useFBO.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						preferences.putBoolean(ORPreferences.OPENGL_USE_FBO, useFBO.isSelected());
					}
				});
				enableGLModel.addEnableComponent(useFBO);
				add(useFBO, "wrap");
			}
		}, "growx, span");
	}

}

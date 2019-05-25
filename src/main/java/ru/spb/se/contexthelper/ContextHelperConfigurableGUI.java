package ru.spb.se.contexthelper;

import com.intellij.util.PlatformUtils;
import ru.spb.se.contexthelper.component.PersistentStateSettingsComponent.SettingKeys;

import javax.swing.*;
import java.util.EnumMap;
import java.util.Map;

public class ContextHelperConfigurableGUI {
    private JCheckBox compileErrorCheckBox;
    private JCheckBox runtimeErrorCheckBox;
    private JPanel rootPanel;

    ContextHelperConfigurableGUI(){

    }

    private JPanel getInfoGUI() {
        JPanel noSettingsPanel = new JPanel();
        JEditorPane noSettingsPane = new JEditorPane();
        noSettingsPane.setContentType("text/html");
        noSettingsPane.setEditable(false);
        noSettingsPane.setOpaque(false);
        noSettingsPane.setText("<html>" +
                "Your platform does not currently support settings." +
                "</html>");
        noSettingsPanel.add(noSettingsPane);
        return noSettingsPanel;
    }

    public JPanel getRootPanel(Map<SettingKeys, Boolean> settings) {
        if(!PlatformUtils.isIntelliJ()) {
            return getInfoGUI();
        }

        this.runtimeErrorCheckBox.setSelected(settings.get(SettingKeys.RUNTIME_ERROR));
        this.compileErrorCheckBox.setSelected(settings.get(SettingKeys.COMPILER_ERROR));

        return rootPanel;
    }

    public Map<SettingKeys, Boolean> getSettings(){
        Map<SettingKeys, Boolean> settings = new EnumMap<SettingKeys, Boolean>(SettingKeys.class);
        settings.put(SettingKeys.RUNTIME_ERROR, this.runtimeErrorCheckBox.isSelected());
        settings.put(SettingKeys.COMPILER_ERROR, this.compileErrorCheckBox.isSelected());

        return settings;
    }
}

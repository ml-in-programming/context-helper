package ru.spb.se.contexthelper

import com.intellij.openapi.options.Configurable
import ru.spb.se.contexthelper.component.PersistentStateSettingsComponent
import com.intellij.openapi.components.ServiceManager
import javax.swing.JComponent

class ContextHelperConfigurable : Configurable{
    override fun getDisplayName(): String {
        return DISPLAY_NAME
    }

    override fun getHelpTopic(): String? {
        return ID
    }

    override fun createComponent(): JComponent? {
        gui = ContextHelperConfigurableGUI()
        persistentStateSettingsComponent = ServiceManager.getService(PersistentStateSettingsComponent::class.java)
        return gui?.getRootPanel(persistentStateSettingsComponent?.getSettings())
    }

    override fun disposeUIResources() {
        gui = null
    }

    override fun isModified(): Boolean {
        val persistentSettings = persistentStateSettingsComponent!!.getSettings()
        val currentSettings = gui!!.settings
        return !currentSettings.equals(persistentSettings)
    }

    override fun apply() {
        val guiState = gui!!.settings;
        persistentStateSettingsComponent!!.updateSettings(guiState);
    }

    override fun reset() {

    }

    companion object{
        private const val DISPLAY_NAME = "Context Helper"
        private const val ID = "preferences.ContextHelperConfigurable"
        private var persistentStateSettingsComponent: PersistentStateSettingsComponent? = null
        private var gui: ContextHelperConfigurableGUI? = null
    }
}
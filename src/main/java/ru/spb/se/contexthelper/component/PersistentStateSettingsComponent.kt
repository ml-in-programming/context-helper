package ru.spb.se.contexthelper.component
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.util.PlatformUtils
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import java.util.EnumMap

@State(
        name = "SettingsState",
        storages = [Storage("ContextHelper-settings.xml")]
)

class PersistentStateSettingsComponent : PersistentStateComponent<PersistentStateSettingsComponent.Companion.State>{
    companion object{data class State(val settings: EnumMap<SettingKeys, Boolean>)}

    private var myState: State? = null

    init {
        if(!PlatformUtils.isIntelliJ()) {
            noStateLoaded()
        }
    }

    override fun noStateLoaded() {
        myState = State(EnumMap(SettingKeys::class.java))
        myState!!.settings[SettingKeys.COMPILER_ERROR] = true
        myState!!.settings[SettingKeys.RUNTIME_ERROR] = true
    }
    fun isRuntimeErrorEnabled(): Boolean {
        return myState!!.settings[SettingKeys.RUNTIME_ERROR] ?: false
    }

    fun isCompileErrorEnabled(): Boolean {
        return myState!!.settings[SettingKeys.COMPILER_ERROR] ?: false
    }

    fun getSettings(): Map<SettingKeys, Boolean>? {
        return myState!!.settings
    }

    fun updateSettings(setting: SettingKeys, enabled: Boolean) {
        myState!!.settings[setting] = enabled
    }

    fun updateSettings(updatedSettingsMap: Map<SettingKeys, Boolean>) {
        myState!!.settings.putAll(updatedSettingsMap)
    }

    override fun getState(): State? {
        return myState
    }

    override fun loadState(state: State) {
        myState = state
    }

    enum class SettingKeys{
        COMPILER_ERROR,
        RUNTIME_ERROR
    }
}
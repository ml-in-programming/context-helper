package ru.spb.se.contexthelper.component

import com.intellij.execution.filters.InputFilter
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import ru.spb.se.contexthelper.errors.ErrorMessage



class RuntimeListenerComponent(private val project: Project) : ProjectComponent{
    private val messageBuilder: MutableMap<InputFilter, MutableMap<String, StringBuilder>> = HashMap()

    fun appendMessage(console: InputFilter, type: String, line: String){
        if (!messageBuilder.containsKey(console)) {
            messageBuilder.put(console, HashMap())
            messageBuilder.get(console)!!.put(type, StringBuilder())
        } else if (!messageBuilder.get(console)!!.containsKey(type)) {
            messageBuilder.get(console)!!.put(type, StringBuilder())
        }
        messageBuilder.get(console)!!.get(type)!!.append(line)
    }

    fun getMessages(console: InputFilter): ErrorMessage? {
        return when (messageBuilder.containsKey(console)) {
            true -> {
                val consoleMessages: MutableMap<String, StringBuilder> = messageBuilder.get(console)!!
                val errorMessage: ErrorMessage = ErrorMessage()

                if(consoleMessages.containsKey("ERROR")) {
                    errorMessage.put(ErrorMessage.MessageType.ERROR, consoleMessages.get("ERROR").toString())
                }
                if(consoleMessages.containsKey("WARNING")) {
                    errorMessage.put(ErrorMessage.MessageType.WARNING, consoleMessages.get("WARNING").toString())
                }
                if(consoleMessages.containsKey("INFORMATION")) {
                    errorMessage.put(ErrorMessage.MessageType.INFORMATION, consoleMessages.get("INFORMATION").toString())
                }

                messageBuilder.remove(console)
                errorMessage
            }
            else -> null
        }
    }

    override fun getComponentName(): String = "$PLUGIN_NAME.$COMPONENT_NAME"

    override fun projectOpened() {

    }

    override fun projectClosed() {

    }

    override fun disposeComponent() {

    }

    companion object {
        const val PLUGIN_NAME = "ContextHelper"

        private const val COMPONENT_NAME = "RuntimeListenerComponent"

        fun getFor(project: Project): RuntimeListenerComponent =
                project.getComponent(RuntimeListenerComponent::class.java)
    }
}
package ru.spb.se.contexthelper.component

import com.intellij.openapi.compiler.CompilerTopics
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBusConnection

class CompilerListenerComponent(private val project: Project) : ProjectComponent {
    private val connection: MessageBusConnection = project.messageBus.connect()

    override fun initComponent() {
        connection.subscribe(CompilerTopics.COMPILATION_STATUS, CustomCompilationStatusListener(project))
    }

    override fun getComponentName(): String = "$PLUGIN_NAME.$COMPONENT_NAME"

    override fun disposeComponent() {
        connection.disconnect()
    }

    override fun projectOpened() {

    }

    override fun projectClosed() {

    }

    companion object {
        const val PLUGIN_NAME = "ContextHelper"

        private const val COMPONENT_NAME = "CompilerListenerComponent"

        fun getFor(project: Project): CompilerListenerComponent =
                project.getComponent(CompilerListenerComponent::class.java)
    }
}
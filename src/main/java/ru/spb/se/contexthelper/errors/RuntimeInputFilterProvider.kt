package ru.spb.se.contexthelper.errors

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.filters.ConsoleInputFilterProvider
import com.intellij.execution.filters.InputFilter
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import groovy.ui.Console
import ru.spb.se.contexthelper.component.ContextHelperProjectComponent
import ru.spb.se.contexthelper.component.RuntimeListenerComponent
import java.util.regex.Pattern


class RuntimeInputFilterProvider : ConsoleInputFilterProvider{
    override fun getDefaultFilters(project: Project): Array<InputFilter>{
        return arrayOf(ConsoleErrorInputFilter(project))
    }

    private inner class ConsoleErrorInputFilter(private val project: Project) : InputFilter {
        private val runtimeListenerComponent: RuntimeListenerComponent = RuntimeListenerComponent.getFor(project)

        override fun applyFilter(text: String, contentType: ConsoleViewContentType): MutableList<Pair<String, ConsoleViewContentType>>? {
            when (contentType) {
                ConsoleViewContentType.ERROR_OUTPUT ->
                    runtimeListenerComponent.appendMessage(this, "ERROR", text)
                ConsoleViewContentType.SYSTEM_OUTPUT -> {
                    if (endOfExecutionPattern.matcher(text).matches()) {
                        val runtimeErrorMessage: ErrorMessage? = runtimeListenerComponent.getMessages(this)

                        runtimeErrorMessage?.let {
                            if (runtimeErrorMessage.get(ErrorMessage.MessageType.ERROR).isNotEmpty()) {
                                val parsedMessage = ErrorMessageParser.parseError(runtimeErrorMessage, project)
                                val helperComponent = ContextHelperProjectComponent.getFor(project)
                                helperComponent.processTextQuery("$parsedMessage runtime error")
                            }
                        }
                    }
                    else runtimeListenerComponent.appendMessage(this, "INFORMATION", text)
                }
                else -> {
                }
            }
            return null
        }
    }

    companion object {
        private val endOfExecutionPattern = Pattern.compile("\nProcess finished with exit code \\d+\n")
    }
}
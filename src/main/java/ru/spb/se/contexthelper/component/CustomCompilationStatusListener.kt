package ru.spb.se.contexthelper.component

import com.intellij.openapi.compiler.CompilationStatusListener
import com.intellij.openapi.compiler.CompileContext
import com.intellij.openapi.compiler.CompilerMessageCategory
import com.intellij.openapi.project.Project
import ru.spb.se.contexthelper.errors.ErrorMessage
import ru.spb.se.contexthelper.errors.ErrorMessageParser

class CustomCompilationStatusListener(private val project: Project) : CompilationStatusListener{
    override fun compilationFinished(aborted: Boolean, errors: Int, warnings: Int, compileContext: CompileContext) {
        if(compileContext.getMessages(CompilerMessageCategory.ERROR).isNotEmpty()){
            val errorMessages = ErrorMessage()
            val helperComponent = ContextHelperProjectComponent.getFor(project)

            errorMessages.put(
                    ErrorMessage.MessageType.ERROR,
                    List<String>(compileContext.getMessageCount(CompilerMessageCategory.ERROR))
                    {compileContext.getMessages(CompilerMessageCategory.ERROR)[it].message}
            )
            errorMessages.put(
                    ErrorMessage.MessageType.WARNING,
                    List<String>(compileContext.getMessageCount(CompilerMessageCategory.WARNING))
                    {compileContext.getMessages(CompilerMessageCategory.WARNING)[it].message}
            )
            errorMessages.put(
                    ErrorMessage.MessageType.INFORMATION,
                    List<String>(compileContext.getMessageCount(CompilerMessageCategory.INFORMATION))
                    {compileContext.getMessages(CompilerMessageCategory.INFORMATION)[it].message}
            )

            val parsedMessage = ErrorMessageParser.parseError(errorMessages, project)
            helperComponent.processTextQuery("$parsedMessage compiler error")
        }
    }
}
package ru.spb.se.contexthelper

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import ru.spb.se.contexthelper.component.ContextHelperProjectComponent
import ru.spb.se.contexthelper.util.ActionEventUtil
import ru.spb.se.contexthelper.util.MessagesUtil

/** An action that is triggering Context Helper assistance in the editor's context. */
class DeclarationsContextHelpAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = ActionEventUtil.getProjectFor(event) ?: return
        val editor = ActionEventUtil.getEditorFor(event)
        if (editor == null) {
            MessagesUtil.showInfoDialog("Source code editor is not selected", project)
            return
        }
        val helperComponent = ContextHelperProjectComponent.getFor(project)
        helperComponent.enterNewSession()
        val caretOffset = editor.caretModel.offset
        val documentText = editor.document.text
        helperComponent.sendContextsMessage(caretOffset, documentText)
        val psiFile = ActionEventUtil.getPsiFileFor(event)
        if (psiFile == null) {
            MessagesUtil.showInfoDialog("No enclosing file found", project)
            return
        }
        val psiElement = psiFile.findElementAt(editor.caretModel.offset - 1)
        if (psiElement == null) {
            MessagesUtil.showInfoDialog("No PSI for the element found", project)
            return
        }
        helperComponent.assistAround(psiElement)
    }
}
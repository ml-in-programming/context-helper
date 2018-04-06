package ru.spb.se.contexthelper

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import ru.spb.se.contexthelper.component.ContextHelperProjectComponent
import ru.spb.se.contexthelper.util.getEditorFor
import ru.spb.se.contexthelper.util.getProjectFor
import ru.spb.se.contexthelper.util.getPsiFileFor
import ru.spb.se.contexthelper.util.showInfoDialog

/** An action that is triggering Context Helper assistance in the editor's context. */
class DeclarationsContextHelpAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = getProjectFor(event) ?: return
        val editor = getEditorFor(event)
        if (editor == null) {
            showInfoDialog("Source code editor is not selected", project)
            return
        }
        val helperComponent = ContextHelperProjectComponent.getFor(project)
        helperComponent.enterNewSession()
        val caretOffset = editor.caretModel.offset
        val documentText = editor.document.text
        helperComponent.sendContextsMessage(caretOffset, documentText)
        val psiFile = getPsiFileFor(event)
        if (psiFile == null) {
            showInfoDialog("No enclosing file found", project)
            return
        }
        val psiElement = psiFile.findElementAt(editor.caretModel.offset - 1)
        if (psiElement == null) {
            showInfoDialog("No PSI for the element found", project)
            return
        }
        helperComponent.assistAround(psiElement)
    }
}

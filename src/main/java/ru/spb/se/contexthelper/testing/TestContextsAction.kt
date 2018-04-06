package ru.spb.se.contexthelper.testing

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.WriteAction
import com.intellij.psi.PsiDocumentManager
import ru.spb.se.contexthelper.util.getEditorFor
import ru.spb.se.contexthelper.util.getProjectFor
import ru.spb.se.contexthelper.util.showInfoDialog
import java.io.File

class TestContextsAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = getProjectFor(event) ?: return
        val editor = getEditorFor(event)
        if (editor == null) {
            showInfoDialog("Editor is not selected", project)
            return
        }
        val document = editor.document
        val file = File("~/R&D/context-helper/testdata/contexts/0")
        val lines = file.readLines()
        val offset = lines[2].toInt()
        val meaningfulLines = lines.drop(3)
        WriteAction.run<Exception> {
            document.setText(meaningfulLines.joinToString("\n"))
            editor.caretModel.moveToOffset(offset)
        }
        PsiDocumentManager.getInstance(project).commitDocument(document)
        val dataContext = DataManager.getInstance().dataContextFromFocus
        val contextHelpAction = ActionManager.getInstance().getAction("DeclarationsContextHelpAction")
        contextHelpAction.actionPerformed(
            AnActionEvent.createFromAnAction(contextHelpAction, null, "", dataContext.result))
    }
}

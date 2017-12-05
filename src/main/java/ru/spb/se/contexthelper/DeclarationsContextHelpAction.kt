package ru.spb.se.contexthelper

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.components.JBList
import ru.spb.se.contexthelper.component.ContextHelperProjectComponent
import ru.spb.se.contexthelper.context.ContextProcessor
import ru.spb.se.contexthelper.context.NotEnoughContextException
import ru.spb.se.contexthelper.util.ActionEventUtil
import ru.spb.se.contexthelper.util.MessagesUtil
import javax.swing.SwingUtilities

/** An action that is generating a query based on the declarations available at the cursor. */
class DeclarationsContextHelpAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = ActionEventUtil.getProjectFor(event) ?: return
        val editor = ActionEventUtil.getEditorFor(event)
        if (editor == null) {
            MessagesUtil.showInfoDialog("Source code editor is not selected", project)
            return
        }
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
        val contextProcessor = ContextProcessor(psiElement)
        val query = try {
            contextProcessor.generateQuery()
        } catch (ignored: NotEnoughContextException) {
            MessagesUtil.showInfoDialog("Unable to describe the context.", project)
            return
        }
        val queryList = JBList<String>(query)
        val popupWindow =
            JBPopupFactory.getInstance().createListPopupBuilder(queryList)
                .setTitle("Select query for StackOverflow")
                .setMovable(false)
                .setResizable(false)
                .setRequestFocus(true)
                .setItemChoosenCallback {
                    val selectedQuery = queryList.selectedValue
                    if (selectedQuery != null) {
                        val helperComponent = ContextHelperProjectComponent.getFor(project)
                        helperComponent.processQuery(selectedQuery)
                    }

                }.createPopup()
        showPopupUnderneathCaret(popupWindow, editor)
    }

    private fun showPopupUnderneathCaret(popupWindow: JBPopup, editor: Editor) {
        val visualPosition = editor.offsetToVisualPosition(editor.caretModel.offset)
        val point = editor.visualPositionToXY(visualPosition)
        SwingUtilities.convertPointToScreen(point, editor.component)
        popupWindow.showInBestPositionFor(editor)
    }
}
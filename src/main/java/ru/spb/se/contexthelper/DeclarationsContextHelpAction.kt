package ru.spb.se.contexthelper

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import ru.spb.se.contexthelper.component.ContextHelperProjectComponent
import ru.spb.se.contexthelper.context.NotEnoughContextException
import ru.spb.se.contexthelper.context.declr.DeclarationsContextExtractor
import ru.spb.se.contexthelper.context.declr.DeclarationsContextQueryBuilder
import ru.spb.se.contexthelper.util.ActionEventUtil
import ru.spb.se.contexthelper.util.MessagesUtil

/** An action that is generating a query based on the declarations available at the cursor. */
class DeclarationsContextHelpAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = ActionEventUtil.getProjectFor(event) ?: return
        val editor = ActionEventUtil.getEditorFor(event)
        if (editor == null) {
            MessagesUtil.showInfoDialog("Editor is not selected", project)
            return
        }
        val psiFile = ActionEventUtil.getPsiFileFor(event)
        if (psiFile == null) {
            MessagesUtil.showInfoDialog("No enclosing file found", project)
            return
        }
        val psiElement = psiFile.findElementAt(editor.caretModel.offset)
        if (psiElement == null) {
            MessagesUtil.showInfoDialog("No PSI for the element found", project)
            return
        }
        val contextExtractor = DeclarationsContextExtractor(psiElement)
        val context = contextExtractor.context
        val queryBuilder = DeclarationsContextQueryBuilder(context)
        val query = try {
            queryBuilder.buildQuery()
        } catch (ignored: NotEnoughContextException) {
            MessagesUtil.showInfoDialog("Unable to describe the context.", project)
            return
        }
        val helperComponent = ContextHelperProjectComponent.getFor(project)
        helperComponent.processQuery(query)
    }
}
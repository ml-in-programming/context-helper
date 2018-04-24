package ru.spb.se.contexthelper.testing

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import ru.spb.se.contexthelper.util.getEditorFor
import ru.spb.se.contexthelper.util.getProjectFor
import ru.spb.se.contexthelper.util.showInfoDialog

class TestContextsAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = getProjectFor(event) ?: return
        val editor = getEditorFor(event)
        if (editor == null) {
            showInfoDialog("Editor is not selected for quality measurements", project)
            return
        }
        val testContextsIterator = TestContextsIterator(event.dataContext)
        testContextsIterator.run()
    }
}

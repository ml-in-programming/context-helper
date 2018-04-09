package ru.spb.se.contexthelper.testing

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.WindowWrapper
import com.intellij.openapi.ui.WindowWrapperBuilder
import com.intellij.psi.PsiDocumentManager
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import ru.spb.se.contexthelper.component.ContextHelperProjectComponent
import ru.spb.se.contexthelper.component.QuestionResultsListener
import ru.spb.se.contexthelper.lookup.StackExchangeQuestionResults
import ru.spb.se.contexthelper.util.showInfoDialog
import java.awt.Dimension
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors.toList
import javax.swing.SwingUtilities
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableModel

class TestContextsIterator(private val project: Project, private val editor: Editor) : QuestionResultsListener {
    private val contextToResults: HashMap<Int, StackExchangeQuestionResults> = hashMapOf()
    private val ids: List<Int>
    private var lastIdIndex: Int = 0

    init {
        val contextsPath = Paths.get("$TESTDATA_PATH/contexts")
        ids =
            Files.list(contextsPath)
                .map { Integer.parseInt(it.fileName.toString()) }
                .sorted()
                .collect(toList())
    }

    fun run() {
        if (ids.isEmpty()) {
            showInfoDialog("No contexts were found", project)
            return
        }
        val everyCorrectPresent = ids.all {
            Files.exists(Paths.get("$TESTDATA_PATH/relevant/${ids[lastIdIndex]}"))
        }
        if (!everyCorrectPresent) {
            showInfoDialog("Not every relevant answer for the context is present", project)
            return
        }
        val helperComponent = ContextHelperProjectComponent.getFor(project)
        helperComponent.addResultsListener(this)
        processNext()
    }

    override fun receiveResults(questionResults: StackExchangeQuestionResults): Boolean {
        contextToResults[ids[lastIdIndex]] = questionResults
        lastIdIndex += 1
        SwingUtilities.invokeLater {
            processNext()
        }
        return lastIdIndex != ids.size
    }

    private fun processNext() {
        if (lastIdIndex == ids.size) {
            showQualityMeasurements()
            return
        }
        val document = editor.document
        val file = File("$TESTDATA_PATH/contexts/${ids[lastIdIndex]}")
        val lines = file.readLines()
        val offset = lines[2].toInt()
        val meaningfulLines = lines.drop(3)
        WriteAction.run<Exception> {
            document.setText(meaningfulLines.joinToString("\n"))
        }
        editor.caretModel.moveToOffset(offset)
        PsiDocumentManager.getInstance(project).commitDocument(document)
        val dataContext = DataManager.getInstance().dataContextFromFocus
        val contextHelpAction = ActionManager.getInstance().getAction("DeclarationsContextHelpAction")
        contextHelpAction.actionPerformed(
            AnActionEvent.createFromAnAction(contextHelpAction, null, "", dataContext.result))
        return
    }

    private fun showQualityMeasurements() {
        val tableModel = buildTableModel()
        val table = JBTable(tableModel)
        table.minimumSize = Dimension(720, 480)
        val wrapperDialog =
            WindowWrapperBuilder(WindowWrapper.Mode.MODAL, JBScrollPane(table))
                .setProject(project)
                .setTitle("Quality measurements")
                .build()
        wrapperDialog.show()
    }

    private fun buildTableModel(): TableModel {
        val contextToRelevant = ids.map { contextId ->
            val path = Paths.get("$TESTDATA_PATH/relevant/$contextId")
            val relevant = Files.readAllLines(path)[0].toInt()
            Pair(contextId, relevant)
        }.toMap()
        val contextToRelevantIndex = ids.map { contextId ->
            val relevant = contextToRelevant[contextId]!!
            val results = contextToResults[contextId]!!
            val questionIds = results.questions.map { it.questionId }
            val relevantIndex = questionIds.indexOf(relevant.toLong())
            Pair(contextId, relevantIndex)
        }.toMap()
        val mrr = contextToRelevantIndex.map { contextRelevantIndex ->
            val relevantIndex = contextRelevantIndex.value
            if (relevantIndex == -1) 0.0 else 1.0 / (relevantIndex + 1)
        }.sum() / ids.size
        return object : AbstractTableModel() {
            override fun getRowCount(): Int = ids.size + 1
            override fun getColumnCount(): Int = 4
            override fun getColumnName(column: Int): String = when (column) {
                0 -> "ContextId"
                1 -> "RelevantId"
                2 -> "QuestionIds"
                3 -> "ReciprocalRank"
                else -> "?"
            }
            override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
                if (rowIndex == ids.size) {
                    return if (columnIndex == 3) mrr else ""
                }
                val contextId = ids[rowIndex]
                return when (columnIndex) {
                    0 -> contextId
                    1 -> contextToRelevant[contextId]!!
                    2 -> {
                        val results = contextToResults[contextId]!!
                        results.questions
                            .joinToString(",", "[", "]") { it.questionId.toString() }
                    }
                    3 -> {
                        val relevantIndex = contextToRelevantIndex[contextId]!!
                        if (relevantIndex == -1) "0" else "1/${relevantIndex + 1}"
                    }
                    else -> "?"
                }
            }
        }
    }

    companion object {
        private const val TESTDATA_PATH = "/Users/niksaz/RnD/context-helper/testdata"
    }
}
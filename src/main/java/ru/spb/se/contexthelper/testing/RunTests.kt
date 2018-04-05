package ru.spb.se.contexthelper.testing

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import ru.spb.se.contexthelper.component.ContextHelperProjectComponent
import ru.spb.se.contexthelper.component.QuestionResultsListener
import ru.spb.se.contexthelper.lookup.StackExchangeQuestionResults
import ru.spb.se.contexthelper.util.getEditorFor
import ru.spb.se.contexthelper.util.getProjectFor
import ru.spb.se.contexthelper.util.showInfoDialog

class RunTests : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = getProjectFor(event) ?: return
        val editor = getEditorFor(event)
        if (editor == null) {
            showInfoDialog("Editor is not selected", project)
            return
        }
        val document = editor.document
        WriteCommandAction.runWriteCommandAction(project) {
            document.setText("package com.mikhail.pravilov.mit.ticTacToe;\n" +
                "\n" +
                "import com.mikhail.pravilov.mit.ticTacToe.model.TicTacToeStatistic;\n" +
                "import com.mikhail.pravilov.mit.ticTacToe.view.GameTypeStageSupplier;\n" +
                "import javafx.application.Application;\n" +
                "import javafx.scene.control.Alert;\n" +
                "import javafx.stage.Stage;\n" +
                "import org.json.JSONException;\n" +
                "\n" +
                "import java.io.IOException;\n" +
                "\n" +
                "public class TicTacToeApplication extends Application {\n" +
                "    @Override\n" +
                "    public void start(Stage primaryStage) throws Exception {\n" +
                "        new GameTypeStageSupplier().getStage().show();\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void stop() throws Exception {\n" +
                "        try {\n" +
                "            TicTacToeStatistic.getInstance().save();\n" +
                "        }\n" +
                "        catch (JSONException | IOException e) {\n" +
                "            Alert savingStatisticError = new Alert(Alert.AlertType.INFORMATION);\n" +
                "            savingStatisticError.setTitle(\"Error\");\n" +
                "            savingStatisticError.setHeaderText(\"Error during saving statistic\");\n" +
                "            savingStatisticError.setContentText(\"Reason: \" + e.getLocalizedMessage());\n" +
                "            savingStatisticError.showAndWait();\n" +
                "        }\n" +
                "\n" +
                "        super.stop();\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * Main method of program. Runs tictactoe application.\n" +
                "     * @param args command line arguments.\n" +
                "     */\n" +
                "    public static void main(String[] args) {\n" +
                "        Application.launch(args);\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "\n")
        }
        editor.caretModel.moveToOffset(837)
        val helperComponent = ContextHelperProjectComponent.getFor(project)
        helperComponent.addResultsListener(object : QuestionResultsListener {
            override fun receiveResults(questionResults: StackExchangeQuestionResults) {
                println("NEW RESULTS!!!!!")
                println(questionResults.questions.forEach { print(it.questionId) })
            }
        })
    }
}

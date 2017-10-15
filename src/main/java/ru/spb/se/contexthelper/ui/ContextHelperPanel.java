package ru.spb.se.contexthelper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBScrollPane;
import ru.spb.se.contexthelper.ContextHelperConstants;
import ru.spb.se.contexthelper.component.ContextHelperProjectComponent;
import ru.spb.se.contexthelper.lookup.StackExchangeQuestionResults;
import ru.spb.se.contexthelper.model.StackExchangeQuestionsTreeModel;

import javax.swing.*;
import java.awt.*;

/** ContextHelper's side panel. */
public class ContextHelperPanel extends JPanel implements Runnable {

  private final ContextHelperProjectComponent contextHelperProjectComponent;
  private final JTextField queryJTextField;
  private final ContextHelperTree tree;
  private final JBScrollPane treeScrollPane;
  private StackExchangeQuestionsTreeModel treeModel;

  public ContextHelperPanel(ContextHelperProjectComponent contextHelperProjectComponent) {
    this.contextHelperProjectComponent = contextHelperProjectComponent;
    this.treeModel =
        new StackExchangeQuestionsTreeModel(
            contextHelperProjectComponent.getStackExchangeClient(), null);
    this.queryJTextField = new JTextField();
    this.tree = new ContextHelperTree(treeModel);
    this.treeScrollPane = new JBScrollPane(tree);
    buildGui();
  }

  /** Updates the underlying data model and JTree element. */
  public void updatePanelWithQueryResults(StackExchangeQuestionResults queryResults) {
    queryJTextField.setText(queryResults.getQueryContent());
    treeModel =
        new StackExchangeQuestionsTreeModel(
            contextHelperProjectComponent.getStackExchangeClient(), queryResults.getQuestions());
    tree.setModel(treeModel);
    treeScrollPane.getVerticalScrollBar().setValue(0);
    showPanel();
  }

  /** Configures the panel's UI. */
  private void buildGui() {
    setLayout(new BorderLayout());
    add(queryJTextField, BorderLayout.PAGE_START);
    add(treeScrollPane, BorderLayout.CENTER);
  }

  private void showPanel() {
    Project project = contextHelperProjectComponent.getProject();
    ToolWindow toolWindow =
        ToolWindowManager.getInstance(project).getToolWindow(ContextHelperConstants.ID_TOOL_WINDOW);
    toolWindow.activate(this);
  }

  @Override
  public void run() {
  }
}

package ru.spb.se.contexthelper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBScrollPane;
import ru.spb.se.contexthelper.ContextHelperConstants;
import ru.spb.se.contexthelper.component.ContextHelperProjectComponent;
import ru.spb.se.contexthelper.lookup.StackExchangeQuestionResults;
import ru.spb.se.contexthelper.model.StackExchangeThreadsTreeModel;

import javax.swing.*;
import java.awt.*;

/** ContextHelper's side panel. */
public class ContextHelperPanel extends JPanel implements Runnable {

  private static final int SPLIT_DIVIDER_POSITION = 300;

  private final ContextHelperProjectComponent contextHelperProjectComponent;

  private final JTextField queryJTextField;

  private final StackExchangeThreadsTree tree;

  private final JBScrollPane treeScrollPane;

  private final JTextPane bodyTextPane;

  private StackExchangeThreadsTreeModel treeModel;

  public ContextHelperPanel(ContextHelperProjectComponent contextHelperProjectComponent) {
    this.contextHelperProjectComponent = contextHelperProjectComponent;
    this.treeModel =
        new StackExchangeThreadsTreeModel(
            contextHelperProjectComponent.getStackExchangeClient(), null);
    this.queryJTextField = new JTextField();
    this.tree = new StackExchangeThreadsTree(this, treeModel);
    this.treeScrollPane = new JBScrollPane(tree);
    this.bodyTextPane = new JTextPane();

    configureGui();
  }

  /** Configures the panel's UI. */
  private void configureGui() {
    bodyTextPane.setContentType("text/html");
    bodyTextPane.setEditable(false);

    setLayout(new BorderLayout());
    add(queryJTextField, BorderLayout.PAGE_START);
    JSplitPane splitPane = new JSplitPane(
        JSplitPane.VERTICAL_SPLIT, treeScrollPane, new JBScrollPane(bodyTextPane));
    splitPane.setDividerLocation(SPLIT_DIVIDER_POSITION);
    add(splitPane, BorderLayout.CENTER);
  }

  /** Updates the underlying data model and JTree element. */
  public void updatePanelWithQueryResults(StackExchangeQuestionResults queryResults) {
    queryJTextField.setText(queryResults.getQueryContent());
    treeModel =
        new StackExchangeThreadsTreeModel(
            contextHelperProjectComponent.getStackExchangeClient(), queryResults.getQuestions());
    tree.setModel(treeModel);
    treeScrollPane.getVerticalScrollBar().setValue(0);
    showPanel();
  }

  void updateBodyTextPaneWithText(String text) {
    bodyTextPane.setText(text);
    bodyTextPane.setCaretPosition(0);
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

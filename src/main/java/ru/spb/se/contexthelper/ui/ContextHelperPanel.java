package ru.spb.se.contexthelper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.JBProgressBar;
import com.intellij.ui.components.JBScrollPane;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.prompt.PromptSupport;
import ru.spb.se.contexthelper.ContextHelperConstants;
import ru.spb.se.contexthelper.component.ContextHelperProjectComponent;
import ru.spb.se.contexthelper.lookup.StackExchangeQuestionResults;
import ru.spb.se.contexthelper.model.StackExchangeThreadsTreeModel;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;

/** ContextHelper's side panel. */
public class ContextHelperPanel extends JPanel implements Runnable {

  private static final int SPLIT_DIVIDER_POSITION = 200;

  private final ContextHelperProjectComponent contextHelperProjectComponent;

  private final JBProgressBar progressBar;

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
    this.progressBar = new JBProgressBar();
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
    bodyTextPane.addHyperlinkListener(e -> {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        if (Desktop.isDesktopSupported()) {
          try {
            Desktop.getDesktop().browse(e.getURL().toURI());
          } catch (Exception ignored) {
          }
        }
      }
    });
    PromptSupport.setPrompt("Enter your query", queryJTextField);

    JPanel topPanel = new JPanel();
    topPanel.setLayout(new VerticalLayout());
    topPanel.add(progressBar);
    topPanel.add(queryJTextField);

    setLayout(new BorderLayout());
    add(topPanel, BorderLayout.PAGE_START);
    queryJTextField.addActionListener(e ->
        contextHelperProjectComponent.processQuery(queryJTextField.getText()));
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

  public void setQueryingStatus(boolean isQuerying) {
    showPanel();
    if (isQuerying) {
      progressBar.setIndeterminate(true);
      queryJTextField.setText("");
      treeModel =
          new StackExchangeThreadsTreeModel(
              contextHelperProjectComponent.getStackExchangeClient(), null);
      tree.setModel(treeModel);
    } else {
      progressBar.setIndeterminate(false);
    }
  }

  @Override
  public void run() {
  }
}

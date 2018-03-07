package ru.spb.se.contexthelper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBScrollPane;
import java.awt.BorderLayout;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.prompt.PromptSupport;
import ru.spb.se.contexthelper.ContextHelperConstants;
import ru.spb.se.contexthelper.component.ContextHelperProjectComponent;
import ru.spb.se.contexthelper.lookup.StackExchangeQuestionResults;

/** ContextHelper's side panel. */
public class ContextHelperPanel extends JPanel implements Runnable {

  private static final int SPLIT_DIVIDER_POSITION = 200;

  private final ContextHelperProjectComponent contextHelperProjectComponent;

  private final JProgressBar progressBar;
  private final JTextField queryJTextField;

  private final JBScrollPane treeScrollPane;

  private WebView webView;

  private final StackExchangeThreadsTree tree;
  private StackExchangeThreadsTreeModel treeModel;

  public ContextHelperPanel(ContextHelperProjectComponent contextHelperProjectComponent) {
    this.contextHelperProjectComponent = contextHelperProjectComponent;
    this.treeModel =
        new StackExchangeThreadsTreeModel(
            contextHelperProjectComponent.getStackExchangeClient(), null);
    this.progressBar = new JProgressBar();
    this.queryJTextField = new JTextField();
    this.tree = new StackExchangeThreadsTree(this, treeModel);
    this.treeScrollPane = new JBScrollPane(tree);

    configureGui();
  }

  /** Configures the panel's UI. */
  private void configureGui() {
    PromptSupport.setPrompt("Enter your query", queryJTextField);

    JPanel topPanel = new JPanel();
    topPanel.setLayout(new VerticalLayout());
    topPanel.add(progressBar);
    topPanel.add(queryJTextField);

    setLayout(new BorderLayout());
    add(topPanel, BorderLayout.PAGE_START);

    JFXPanel jfxPanel = new JFXPanel();
    Platform.runLater(() -> {
      webView = new WebView();
      jfxPanel.setScene(new Scene(webView));
    });

    queryJTextField.addActionListener(e ->
        contextHelperProjectComponent.processQuery(queryJTextField.getText()));
    JSplitPane splitPane = new JSplitPane(
        JSplitPane.VERTICAL_SPLIT, treeScrollPane, jfxPanel);
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

  void updateWebViewWithHtml(String context) {
    Platform.runLater(() -> {
      WebEngine engine = webView.getEngine();
      engine.loadContent(context, "text/html");
    });
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
      updateWebViewWithHtml("");
    } else {
      progressBar.setIndeterminate(false);
    }
  }

  @Override
  public void run() {
  }
}

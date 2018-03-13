package ru.spb.se.contexthelper.ui;

import com.google.code.stackexchange.schema.Answer;
import com.google.code.stackexchange.schema.Question;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBScrollPane;
import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.util.stream.Collectors;
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
public class ContextHelperPanel extends JPanel implements Runnable, StackExchangeTreeListener {

  private static final int SPLIT_DIVIDER_POSITION = 200;

  private final ContextHelperProjectComponent contextHelperProjectComponent;

  private final JProgressBar progressBar;
  private final JTextField queryJTextField;

  private final JBScrollPane treeScrollPane;

  private WebView webView;

  private final JBCheckBox checkBox;
  private Object selectedItem;

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
    this.checkBox = new JBCheckBox("Do you find this item helpful to the problem?");

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

    checkBox.setVisible(false);
    checkBox.addItemListener(itemEvent -> {
      if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
        checkBox.setEnabled(false);

        Long id = null;
        if (selectedItem instanceof Question) {
          Question question = (Question) selectedItem;
          id = question.getQuestionId();
        } else if (selectedItem instanceof Answer) {
          Answer answer = (Answer) selectedItem;
          id = answer.getAnswerId();
        }
        if (id != null) {
          contextHelperProjectComponent.sendHelpfulMessage(Long.toString(id));
        }
      }
    });

    JPanel bottomPanel = new JPanel();
    bottomPanel.setLayout(new BorderLayout());
    bottomPanel.add(checkBox, BorderLayout.PAGE_START);
    bottomPanel.add(jfxPanel, BorderLayout.CENTER);
    queryJTextField.addActionListener(actionEvent -> {
      contextHelperProjectComponent.enterNewSession();
      contextHelperProjectComponent.processQuery(queryJTextField.getText());
    });
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treeScrollPane, bottomPanel);
    splitPane.setDividerLocation(SPLIT_DIVIDER_POSITION);
    add(splitPane, BorderLayout.CENTER);
  }

  /** Updates the underlying data model and JTree element. */
  public void updatePanelWithQueryResults(StackExchangeQuestionResults queryResults) {
    contextHelperProjectComponent.sendQuestionsMessage(
      queryResults.getQueryContent(),
      queryResults.getQuestions().stream()
        .map(Question::getQuestionId)
        .collect(Collectors.toList()));
    queryJTextField.setText(queryResults.getQueryContent());
    treeModel =
        new StackExchangeThreadsTreeModel(
            contextHelperProjectComponent.getStackExchangeClient(), queryResults.getQuestions());
    tree.setModel(treeModel);
    treeScrollPane.getVerticalScrollBar().setValue(0);
    showPanel();
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
      checkBox.setVisible(false);
      treeModel =
          new StackExchangeThreadsTreeModel(
              contextHelperProjectComponent.getStackExchangeClient(), null);
      tree.setModel(treeModel);
      renderHtmlText("");
    } else {
      progressBar.setIndeterminate(false);
    }
  }

  @Override
  public void run() {
  }

  @Override
  public void renderHtmlText(String htmlText) {
    Platform.runLater(() -> {
      WebEngine engine = webView.getEngine();
      engine.loadContent(htmlText, "text/html");
    });
  }

  @Override
  public void questionClicked(Question question) {
    selectedItem = question;
    contextHelperProjectComponent.sendClicksMessage(Long.toString(question.getQuestionId()));
    enableCheckBox();
  }

  @Override
  public void answerClicked(Answer answer) {
    selectedItem = answer;
    contextHelperProjectComponent.sendClicksMessage(Long.toString(answer.getAnswerId()));
    enableCheckBox();
  }

  private void enableCheckBox() {
    checkBox.setVisible(true);
    checkBox.setSelected(false);
    checkBox.setEnabled(true);
  }
}
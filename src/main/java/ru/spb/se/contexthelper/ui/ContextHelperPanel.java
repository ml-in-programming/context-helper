package ru.spb.se.contexthelper.ui;

import com.google.code.stackexchange.schema.Answer;
import com.google.code.stackexchange.schema.Question;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBScrollPane;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.prompt.PromptSupport;
import ru.spb.se.contexthelper.ContextHelperConstants;
import ru.spb.se.contexthelper.component.ContextHelperProjectComponent;
import ru.spb.se.contexthelper.context.processor.ProcessorMethodEnum;
import ru.spb.se.contexthelper.lookup.StackExchangeQuestionResults;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.net.URL;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** ContextHelper's side panel. */
public class ContextHelperPanel extends JPanel implements Runnable, StackExchangeTreeListener {
  private static final int SPLIT_DIVIDER_POSITION = 205;

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
    this.treeModel = new StackExchangeThreadsTreeModel(null);
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
    topPanel.add(buildComboBox());
    topPanel.add(queryJTextField);

    setLayout(new BorderLayout());
    add(topPanel, BorderLayout.PAGE_START);

    JFXPanel jfxPanel = new JFXPanel();
    Platform.runLater(() -> {
      webView = new WebView();
      webView.getEngine().setUserStyleSheetLocation(
          getClass().getResource("/style.css").toString());
      webView.addEventFilter(KeyEvent.KEY_RELEASED, (KeyEvent e) -> {
        if (e.getCode() == KeyCode.ADD || e.getCode() == KeyCode.EQUALS
          || e.getCode() == KeyCode.PLUS) {
          webView.setZoom(webView.getZoom() * 1.1);
        }
        else if (e.getCode() == KeyCode.SUBTRACT || e.getCode() == KeyCode.MINUS ){
          webView.setZoom(webView.getZoom() / 1.1);
        }
      });
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
      contextHelperProjectComponent.processTextQuery(queryJTextField.getText());
    });
    @SuppressWarnings("SuspiciousNameCombination")
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treeScrollPane, bottomPanel);
    splitPane.setDividerLocation(SPLIT_DIVIDER_POSITION);
    add(splitPane, BorderLayout.CENTER);
  }

  private ComboBox buildComboBox() {
    ProcessorMethodEnum[] processorMethods = ProcessorMethodEnum.values();
    ComboBox<ProcessorMethodEnum> comboBox = new ComboBox<>(processorMethods);
    Font plainFont = getFont();
    Font boldFont = new Font(plainFont.getName(), Font.BOLD, (int) (plainFont.getSize() * 0.8));
    comboBox.setSelectedIndex(0);
    comboBox.setFont(boldFont);
    comboBox.setRenderer(new ListCellRendererWrapper<ProcessorMethodEnum>() {
      @Override
      public void customize(JList list, ProcessorMethodEnum value, int index, boolean selected, boolean hasFocus) {
        setText(value.name());
        setFont(plainFont);
      }
    });
    comboBox.addActionListener(e -> {
      ProcessorMethodEnum method = (ProcessorMethodEnum) comboBox.getSelectedItem();
      contextHelperProjectComponent.changeProcessorMethodTo(Objects.requireNonNull(method));
    });
    return comboBox;
  }

  /** Updates the underlying data model and JTree element. */
  public void updatePanelWithQueryResults(StackExchangeQuestionResults queryResults) {
    contextHelperProjectComponent.sendQuestionsMessage(
      queryResults.getQueryContent(),
      queryResults.getQuestions().stream()
          .map(Question::getQuestionId)
          .collect(Collectors.toList()));
    queryJTextField.setText(queryResults.getQueryContent());
    treeModel = new StackExchangeThreadsTreeModel(queryResults.getQuestions());
    tree.setModel(treeModel);
    treeScrollPane.getVerticalScrollBar().setValue(0);
    showPanel();
  }

  private void showPanel() {
    Project project = contextHelperProjectComponent.getProject();
    ToolWindow toolWindow =
        ToolWindowManager.getInstance(project).getToolWindow(ContextHelperConstants.ID_TOOL_WINDOW);
    toolWindow.show(this);
  }

  public void setQueryingStatus(boolean isQuerying) {
    showPanel();
    if (isQuerying) {
      progressBar.setIndeterminate(true);
      queryJTextField.setText("");
      checkBox.setVisible(false);
      treeModel = new StackExchangeThreadsTreeModel(null);
      tree.setModel(treeModel);
      renderHtml("");
    } else {
      progressBar.setIndeterminate(false);
    }
  }

  @Override
  public void run() {
  }

  @Override
  public void renderHtml(String bodyHtml) {
    String highlightedHtml = highlightHtml(bodyHtml);
    WebEngine engine = webView.getEngine();
    URL prettifyUrl = this.getClass().getResource("/prettify.js");
    Platform.runLater(() -> engine.loadContent(
        "<html>\n"
        + "<head>\n"
        + "<script type=\"text/javascript\" src=\"" + prettifyUrl.toString() + "\"></script>\n"
        + "</head>\n"
        + "<body>\n"
        + highlightedHtml
            .replace("<code>", "<pre class=\"prettyprint\">")
            .replace("</code>", "</pre>")
        + "</body>\n"
        + "</html>",
        "text/html"));
  }

  private String highlightHtml(String bodyHtml) {
    String[] words = queryJTextField.getText().split("\\s+");
    String highlightedHtml = bodyHtml;
    for (String word : words) {
      highlightedHtml = highlightedHtml.replaceAll(
          Pattern.quote(word), "<span class='highlight'>" + word + "</span>");
    }
    return highlightedHtml;
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
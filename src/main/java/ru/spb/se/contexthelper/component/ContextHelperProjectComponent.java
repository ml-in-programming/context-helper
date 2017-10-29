package ru.spb.se.contexthelper.component;

import com.google.code.stackexchange.schema.Question;
import com.google.common.net.UrlEscapers;
import com.intellij.openapi.components.NamedComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import ru.spb.se.contexthelper.lookup.StackExchangeClient;
import ru.spb.se.contexthelper.lookup.StackExchangeQuestionResults;
import ru.spb.se.contexthelper.ui.ContextHelperPanel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static ru.spb.se.contexthelper.ContextHelperConstants.ID_TOOL_WINDOW;
import static ru.spb.se.contexthelper.ContextHelperConstants.PLUGIN_NAME;

/** Component which is called to initialize ContextHelper plugin for each {@link Project}. */
public class ContextHelperProjectComponent implements ProjectComponent {

  /** Last part of the name for {@link NamedComponent}. */
  private static final String COMPONENT_NAME = "ContextHelperProjectComponent";

  private static final String ICON_PATH_TOOL_WINDOW = "/icons/se-icon.png";

  private final Project project;

  private final StackExchangeClient stackExchangeClient;

  private ContextHelperPanel viewerPanel;

  public ContextHelperProjectComponent(Project project) {
    this.project = project;
    this.stackExchangeClient = new StackExchangeClient();
  }

  private ContextHelperPanel getViewerPanel() {
    return viewerPanel;
  }

  public StackExchangeClient getStackExchangeClient() {
    return stackExchangeClient;
  }

  public Project getProject() {
    return project;
  }

  @Override
  public void projectOpened() {
    initToolWindow();
  }

  private void initToolWindow() {
    viewerPanel = new ContextHelperPanel(this);
    ToolWindow toolWindow = getOrRegisterToolWindow();
    toolWindow.setIcon(IconLoader.getIcon(ICON_PATH_TOOL_WINDOW));
  }

  private ToolWindow getOrRegisterToolWindow() {
    ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
    if (isToolWindowRegistered()) {
      return ToolWindowManager.getInstance(project).getToolWindow(ID_TOOL_WINDOW);
    } else {
      ToolWindow toolWindow =
          toolWindowManager.registerToolWindow(ID_TOOL_WINDOW, true, ToolWindowAnchor.RIGHT);
      Content content = ContentFactory.SERVICE.getInstance().createContent(viewerPanel, "", false);
      toolWindow.getContentManager().addContent(content);
      return toolWindow;
    }
  }

  private boolean isToolWindowRegistered() {
    return ToolWindowManager.getInstance(project).getToolWindow(ID_TOOL_WINDOW) != null;
  }

  @Override
  public void projectClosed() {
    disposeToolWindow();
  }

  private void disposeToolWindow() {
    viewerPanel = null;
    if (isToolWindowRegistered()) {
      ToolWindowManager.getInstance(project).unregisterToolWindow(ID_TOOL_WINDOW);
    }
  }

  @Override
  public void initComponent() {
  }

  @Override
  public void disposeComponent() {
  }

  @NotNull
  @Override
  public String getComponentName() {
    return PLUGIN_NAME + "." + COMPONENT_NAME;
  }

  public static ContextHelperProjectComponent getInstance(Project project) {
    return project.getComponent(ContextHelperProjectComponent.class);
  }

  public void processQuery(String query) {
    try {
      List<Long> questionIds = experimentalQueryProcessing(query);
      List<Question> questions = getStackExchangeClient().requestQuestionsWith(questionIds);
      StackExchangeQuestionResults queryResults =
          new StackExchangeQuestionResults(query, questions);
      ContextHelperPanel contextHelperPanel = getViewerPanel();
      contextHelperPanel.updatePanelWithQueryResults(queryResults);
    } catch (Exception e) {
      showErrorMessage("Unable to process the query.", project);
    }
    // Currently using Google Custom Search's topical search engine. But it has 100 queries per day
    // limit. May return to StackExchange search in the future.
    // StackExchangeQuestionResults queryResults = stackExchangeClient.requestRelevantQuestions
    // (query);
  }

  private List<Long> experimentalQueryProcessing(String query) throws Exception {
    String key = "AIzaSyBXQg39PaVjqONPEL4eubyA7S-pEuqVKOc";
    String searchEngineId = "004273159360178116673:j1srnoyrr-i";
    String encodedUrl = UrlEscapers.urlFragmentEscaper().escape(
        "https://www.googleapis.com/customsearch/v1"
            + "?key=" + key
            + "&cx=" + searchEngineId
            + "&q=" + query + " java"
            + "&alt=json");
    URL url = new URL(encodedUrl);

    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    List<Long> questionIds = new ArrayList<>();
    try (AutoCloseable ignored = urlConnection::disconnect) {
      urlConnection.setRequestMethod("GET");
      urlConnection.setRequestProperty("Accept", "application/json");
      BufferedReader bufferedReader =
          new BufferedReader(
              new InputStreamReader(urlConnection.getInputStream()));
      String nextLine;
      while ((nextLine = bufferedReader.readLine()) != null) {
        if (nextLine.contains("\"link\": \"")){
          String link = nextLine.substring(
              nextLine.indexOf("\"link\": \"") + ("\"link\": \"").length(),
              nextLine.indexOf("\","));
          // Format: https://stackoverflow.com/questions/id/...
          String[] urlParts = link.split("/");
          String idText = urlParts[4];
          questionIds.add(Long.parseLong(idText));
        }
      }
    }
    return questionIds;
  }

  private static void showErrorMessage(String message, Project project) {
    Messages.showMessageDialog(
        project,
        message,
        "Error",
        Messages.getInformationIcon());
  }
}

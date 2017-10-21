package ru.spb.se.contexthelper;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import ru.spb.se.contexthelper.component.ContextHelperProjectComponent;
import ru.spb.se.contexthelper.context.ContextExtractionException;
import ru.spb.se.contexthelper.context.ContextExtractor;
import ru.spb.se.contexthelper.context.ContextQueryBuilder;
import ru.spb.se.contexthelper.context.EventContext;
import ru.spb.se.contexthelper.lookup.StackExchangeClient;
import ru.spb.se.contexthelper.lookup.StackExchangeQuestionResults;
import ru.spb.se.contexthelper.ui.ContextHelperPanel;
import ru.spb.se.contexthelper.util.ActionEventUtil;

/** An action for getting help based on the context around editor's caret. */
public class ContextHelpAction extends AnAction {

  @Override
  public void actionPerformed(AnActionEvent event) {
    Project project = ActionEventUtil.getProjectFor(event);
    ContextExtractor contextExtractor = new ContextExtractor(event);
    EventContext eventContext;
    try {
      eventContext = contextExtractor.extractContext();
    } catch (ContextExtractionException e) {
      showInfoMessage(e.getMessage(), project);
      return;
    }
    ContextQueryBuilder contextQueryBuilder = new ContextQueryBuilder(eventContext);
    String query = contextQueryBuilder.buildQuery();
    runQuery(query, project);
  }

  /** Runs a query for the given context. */
  private static void runQuery(String query, Project project) {
    ContextHelperProjectComponent helperProjectComponent =
        ContextHelperProjectComponent.getInstance(project);

    StackExchangeClient stackExchangeClient = helperProjectComponent.getStackExchangeClient();
    StackExchangeQuestionResults queryResults = stackExchangeClient.requestRelevantQuestions(query);

    ContextHelperPanel contextHelperPanel = helperProjectComponent.getViewerPanel();
    contextHelperPanel.updatePanelWithQueryResults(queryResults);
  }

  private static void showInfoMessage(String message, Project project) {
    Messages.showMessageDialog(
        project,
        message,
        "Information",
        Messages.getInformationIcon());
  }
}

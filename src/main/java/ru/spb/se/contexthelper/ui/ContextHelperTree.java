package ru.spb.se.contexthelper.ui;

import com.google.code.stackexchange.schema.Answer;
import com.google.code.stackexchange.schema.Question;
import ru.spb.se.contexthelper.model.StackExchangeQuestionsTreeModel;

import javax.swing.*;

/** {@link JTree} view component for ContextHelper's side panel. */
class ContextHelperTree extends JTree {

  ContextHelperTree(
      ContextHelperPanel contextHelperPanel, StackExchangeQuestionsTreeModel treeModel) {
    super(treeModel);
    setCellRenderer(new ContextHelperTreeCellRender());
    setRootVisible(false);
    addTreeSelectionListener(e -> {
      Object lastSelectedComponent = getLastSelectedPathComponent();
      if (lastSelectedComponent instanceof Question) {
        Question question = (Question) lastSelectedComponent;
        contextHelperPanel.updateBodyTextPaneWithText(question.getBody());
      } else if (lastSelectedComponent instanceof Answer) {
        Answer answer = (Answer) lastSelectedComponent;
        contextHelperPanel.updateBodyTextPaneWithText(answer.getBody());
      }
    });
  }
}

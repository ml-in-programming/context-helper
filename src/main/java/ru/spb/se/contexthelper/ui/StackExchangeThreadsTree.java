package ru.spb.se.contexthelper.ui;

import com.google.code.stackexchange.schema.Answer;
import com.google.code.stackexchange.schema.Question;
import ru.spb.se.contexthelper.model.StackExchangeThreadsTreeModel;

import javax.swing.*;

/** {@link JTree} view component for displaying StackExchange threads. */
class StackExchangeThreadsTree extends JTree {

  StackExchangeThreadsTree(
      ContextHelperPanel contextHelperPanel, StackExchangeThreadsTreeModel treeModel) {
    super(treeModel);
    setCellRenderer(new StackExchangeThreadsTreeCellRender());
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

package ru.spb.se.contexthelper.ui;

import com.google.code.stackexchange.schema.Answer;
import com.google.code.stackexchange.schema.Question;
import javax.swing.JTree;

/** {@link JTree} view component for displaying StackExchange threads. */
class StackExchangeThreadsTree extends JTree {

  StackExchangeThreadsTree(
      StackExchangeTreeListener treeListener, StackExchangeThreadsTreeModel treeModel) {
    super(treeModel);
    setCellRenderer(new StackExchangeThreadsTreeCellRender());
    setRootVisible(false);
    addTreeSelectionListener(e -> {
      Object lastSelectedComponent = getLastSelectedPathComponent();
      if (lastSelectedComponent instanceof Question) {
        Question question = (Question) lastSelectedComponent;
        //TODO: Add question.link as a first line of the text.
        treeListener.questionClicked(question);
        treeListener.renderPrettifiedHtml(question.getBody());
      } else if (lastSelectedComponent instanceof Answer) {
        Answer answer = (Answer) lastSelectedComponent;
        treeListener.answerClicked(answer);
        treeListener.renderPrettifiedHtml(answer.getBody());
      }
    });
  }
}
package ru.spb.se.contexthelper.ui;

import com.google.code.stackexchange.schema.Answer;
import com.google.code.stackexchange.schema.Question;

import javax.swing.JTree;

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
        //TODO: Add question.link as a first line of the text.
        contextHelperPanel.updateWebViewWithHtml(
          "<script src=\"https://cdn.rawgit.com/google/code-prettify/master/loader/run_prettify.js\"></script>"
            + question.getBody()
            .replace("<code>", "<pre class=\"prettyprint\"><code>")
            .replace("</code>", "</code></pre>"));
      } else if (lastSelectedComponent instanceof Answer) {
        Answer answer = (Answer) lastSelectedComponent;
        contextHelperPanel.updateWebViewWithHtml(
          "<script src=\"https://cdn.rawgit.com/google/code-prettify/master/loader/run_prettify.js\"></script>"
            + answer.getBody()
            .replace("<code>", "<pre class=\"prettyprint\"><code>")
            .replace("</code>", "</code></pre>"));
      }
    });
  }
}

package ru.spb.se.contexthelper.ui;

import ru.spb.se.contexthelper.model.StackExchangeQuestionsTreeModel;

import javax.swing.*;

/** {@link JTree} view component for ContextHelper's side panel. */
class ContextHelperTree extends JTree {

  ContextHelperTree(StackExchangeQuestionsTreeModel treeModel) {
    super(treeModel);
    setCellRenderer(new ContextHelperTreeCellRender());
    setRootVisible(false);
  }
}

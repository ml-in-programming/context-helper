package ru.spb.se.contexthelper.ui;

import ru.spb.se.contexthelper.model.ContextHelperTreeModel;

import javax.swing.*;

/** {@link JTree} view component for ContextHelper's side panel. */
class ContextHelperTree extends JTree {

  ContextHelperTree(ContextHelperTreeModel treeModel) {
    super(treeModel);
  }
}

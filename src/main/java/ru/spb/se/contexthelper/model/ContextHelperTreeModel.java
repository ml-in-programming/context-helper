package ru.spb.se.contexthelper.model;

import ru.spb.se.contexthelper.component.ContextHelperProjectComponent;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/** {@link TreeModel} for ContextHelper's side panel. */
public class ContextHelperTreeModel implements TreeModel {

  private final ContextHelperProjectComponent projectComponent;

  public ContextHelperTreeModel(ContextHelperProjectComponent projectComponent) {
    this.projectComponent = projectComponent;
  }

  @Override
  public Object getRoot() {
    return null;
  }

  @Override
  public Object getChild(Object parent, int index) {
    return null;
  }

  @Override
  public int getChildCount(Object parent) {
    return 0;
  }

  @Override
  public boolean isLeaf(Object node) {
    return false;
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    return 0;
  }

  @Override
  public void addTreeModelListener(TreeModelListener l) {
  }

  @Override
  public void removeTreeModelListener(TreeModelListener l) {

  }
}

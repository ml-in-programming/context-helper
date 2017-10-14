package ru.spb.se.contexthelper.model;

import com.google.code.stackexchange.schema.Question;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.List;

/** {@link TreeModel} for ContextHelper's side panel. */
public class ContextHelperTreeModel implements TreeModel {

  private final List<Question> questions;

  public ContextHelperTreeModel(List<Question> questions) {
    this.questions = questions;
  }

  @Override
  public Object getRoot() {
    return questions;
  }

  @Override
  public Object getChild(Object parent, int index) {
    if (parent instanceof List) {
      return questions.get(index);
    } else {
      return null;
    }
  }

  @Override
  public int getChildCount(Object parent) {
    if (parent instanceof List) {
      return questions.size();
    } else {
      return 0;
    }
  }

  @Override
  public boolean isLeaf(Object node) {
    return !(node instanceof List);
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

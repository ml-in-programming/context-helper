package ru.spb.se.contexthelper.ui;

import com.google.code.stackexchange.schema.Answer;
import com.google.code.stackexchange.schema.Comment;
import com.google.code.stackexchange.schema.Question;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.List;

/** {@link TreeModel} which provides data for StackExchange threads. */
public class StackExchangeThreadsTreeModel implements TreeModel {

  private final List<Question> questions;

  StackExchangeThreadsTreeModel(List<Question> questions) {
    this.questions = questions;
  }

  @Override
  public Object getRoot() {
    return questions;
  }

  @Override
  public Object getChild(Object parent, int index) {
    if (parent instanceof List) {
      List list = (List) parent;
      return list.get(index);
    } else if (parent instanceof Question) {
      Question question = (Question) parent;
      List<Comment> comments = question.getComments();
      // TODO(niksaz): Earlier the answers were sorted by their scores.
      return index < comments.size()
          ? comments.get(index)
          : question.getAnswers().get(index - comments.size());
    } else if (parent instanceof Answer) {
      Answer answer = (Answer) parent;
      return answer.getComments().get(index);
    } else {
      return null;
    }
  }

  @Override
  public int getChildCount(Object parent) {
    if (parent instanceof List) {
      List list = (List) parent;
      return list.size();
    } else if (parent instanceof Question) {
      Question question = (Question) parent;
      // TODO(niksaz): Look into it!
      // Since the answer count could be larger than the number of fetched answers, we will display,
      // the child count should not be larger than the displayed number.
      return question.getComments().size() + question.getAnswers().size();
    } else if (parent instanceof Answer) {
      Answer answer = (Answer) parent;
      return answer.getComments().size();
    } else {
      return 0;
    }
  }

  @Override
  public boolean isLeaf(Object node) {
    return getChildCount(node) == 0;
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    // TODO(niksaz): Investigate where the method is used and implement it.
    return 0;
  }

  @Override
  public void addTreeModelListener(TreeModelListener l) {
  }

  @Override
  public void removeTreeModelListener(TreeModelListener l) {
  }
}

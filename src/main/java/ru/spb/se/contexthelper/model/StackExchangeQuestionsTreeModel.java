package ru.spb.se.contexthelper.model;

import com.google.code.stackexchange.schema.Answer;
import com.google.code.stackexchange.schema.Question;
import ru.spb.se.contexthelper.lookup.StackExchangeClient;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** {@link TreeModel} bases on List<Question>. */
public class StackExchangeQuestionsTreeModel implements TreeModel {

  private final StackExchangeClient stackExchangeClient;

  private final List<Question> questions;

  private final Map<Question, List<Answer>> questionToAnswers = new HashMap<>();

  public StackExchangeQuestionsTreeModel(
      StackExchangeClient stackExchangeClient, List<Question> questions) {
    this.stackExchangeClient = stackExchangeClient;
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
      List<Answer> answers = getOrRequestFor(question);
      return answers.get(index);
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
      List<Answer> answers = getOrRequestFor(question);
      return answers.size();
    } else {
      return 0;
    }
  }

  @Override
  public boolean isLeaf(Object node) {
    return !(node instanceof List) && !(node instanceof Question);
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

  private List<Answer> getOrRequestFor(Question question) {
    List<Answer> cachedAnswers = questionToAnswers.get(question);
    if (cachedAnswers == null) {
      cachedAnswers = stackExchangeClient.requestAnswersFor(question.getQuestionId());
      questionToAnswers.put(question, cachedAnswers);
    }
    return cachedAnswers;
  }
}

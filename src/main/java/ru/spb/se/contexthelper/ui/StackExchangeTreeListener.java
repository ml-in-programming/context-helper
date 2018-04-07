package ru.spb.se.contexthelper.ui;

import com.google.code.stackexchange.schema.Answer;
import com.google.code.stackexchange.schema.Question;

/** An entity that can listen to events which occur in {@link StackExchangeThreadsTree} */
public interface StackExchangeTreeListener {
  void renderHtml(String bodyHtml);

  void questionClicked(Question question);

  void answerClicked(Answer answer);
}
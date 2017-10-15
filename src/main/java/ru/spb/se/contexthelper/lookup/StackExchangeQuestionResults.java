package ru.spb.se.contexthelper.lookup;

import com.google.code.stackexchange.schema.Question;

import java.util.List;

/** Represents results for StackExchange*/
public class StackExchangeQuestionResults {

  private final String queryContent;

  private final List<Question> questions;

  StackExchangeQuestionResults(String queryContent, List<Question> questions) {
    this.queryContent = queryContent;
    this.questions = questions;
  }

  public String getQueryContent() {
    return queryContent;
  }

  public List<Question> getQuestions() {
    return questions;
  }
}

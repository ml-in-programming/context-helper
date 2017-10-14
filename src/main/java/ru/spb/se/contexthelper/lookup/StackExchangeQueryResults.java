package ru.spb.se.contexthelper.lookup;

import com.google.code.stackexchange.schema.Question;

import java.util.List;

/** Represents results for StackExchange*/
public class StackExchangeQueryResults {

  private final String queryContent;

  // TODO(niksaz): Use PagesList<Question>, not only first page.
  private final List<Question> questions;

  StackExchangeQueryResults(String queryContent, List<Question> questions) {
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

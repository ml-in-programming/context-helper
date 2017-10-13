package ru.spb.se.contexthelper;

import com.google.code.stackexchange.client.query.StackExchangeApiQueryFactory;
import com.google.code.stackexchange.common.PagedList;
import com.google.code.stackexchange.schema.Paging;
import com.google.code.stackexchange.schema.Question;
import com.google.code.stackexchange.schema.StackExchangeSite;
import com.google.code.stackexchange.schema.User;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StackOverflowClient {

  public StackOverflowClient() {
  }

  public JComponent jComponentForQuery(String query) {
    String responseText = askQuery(query);
    JTextPane textPane = new JTextPane();
    textPane.setText(responseText);
    textPane.setCaretPosition(0);
    return new JBScrollPane(textPane);
  }

  private String askQuery(String unrefinedQuery) {
    String[] queryWords = unrefinedQuery.split("(?=\\p{Upper})");
    String query = Arrays.stream(queryWords).reduce("", (s1, s2) -> s1 + " " + s2);
    StackExchangeApiQueryFactory queryFactory = StackExchangeApiQueryFactory
        .newInstance("F)x9bhGombhjqpnXt)5Mwg((",
            StackExchangeSite.STACK_OVERFLOW);
    Paging paging = new Paging(1, 100);
    List<String> tagged = new ArrayList<>();
    tagged.add("java");
    PagedList<Question> questions = queryFactory
        .newAdvanceSearchApiQuery().withPaging(paging).withQuery(query).withTags(tagged)
        .withSort(User.QuestionSortOrder.MOST_RELEVANT).list();
    StringBuilder summaryBuilder = new StringBuilder("Results for query: " + query);
    summaryBuilder.append('\n');
    summaryBuilder.append('\n');
    for (Question question : questions) {
      summaryBuilder.append(question.getScore()).append(' ').append(question.getTitle());
      summaryBuilder.append('\n');
      summaryBuilder.append('\n');
    }
    return summaryBuilder.toString();
  }
}

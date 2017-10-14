package ru.spb.se.contexthelper.lookup;

import com.google.code.stackexchange.client.query.StackExchangeApiQueryFactory;
import com.google.code.stackexchange.common.PagedList;
import com.google.code.stackexchange.schema.Paging;
import com.google.code.stackexchange.schema.Question;
import com.google.code.stackexchange.schema.StackExchangeSite;
import com.google.code.stackexchange.schema.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Client for StackExchange API. */
public class StackOverflowClient {

  private static final String APPLICATION_KEY = "F)x9bhGombhjqpnXt)5Mwg((";

  public StackOverflowClient() {
  }

  public String processQuery(String unrefinedQuery) {
    String[] queryWords = unrefinedQuery.split("(?=\\p{Upper})");
    String query = Arrays.stream(queryWords).reduce("", (s1, s2) -> s1 + " " + s2);
    StackExchangeApiQueryFactory queryFactory =
        StackExchangeApiQueryFactory.newInstance(APPLICATION_KEY, StackExchangeSite.STACK_OVERFLOW);
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

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
public class StackExchangeClient {

  private static final String APPLICATION_KEY = "F)x9bhGombhjqpnXt)5Mwg((";
  private static final StackExchangeSite STACK_EXCHANGE_SITE = StackExchangeSite.STACK_OVERFLOW;

  private static final int PAGE_SIZE = 100;

  private final StackExchangeApiQueryFactory queryFactory;

  public StackExchangeClient() {
    queryFactory = StackExchangeApiQueryFactory.newInstance(APPLICATION_KEY, STACK_EXCHANGE_SITE);
  }

  /** Returns Java tagged question for a query generated based on the parameter. */
  public StackExchangeQueryResults processJavaQuery(String unrefinedQuery) {
    String[] queryWords = unrefinedQuery.split("(?=\\p{Upper})");
    String query = Arrays.stream(queryWords).reduce("", (s1, s2) -> s1 + " " + s2);
    Paging paging = new Paging(1, PAGE_SIZE);
    List<String> tagged = new ArrayList<>();
    tagged.add("java");
    PagedList<Question> questions =
        queryFactory.newAdvanceSearchApiQuery()
            .withQuery(query)
            .withPaging(paging)
            .withTags(tagged)
            .withSort(User.QuestionSortOrder.MOST_RELEVANT)
            .list();
    return new StackExchangeQueryResults(query, questions);
  }
}

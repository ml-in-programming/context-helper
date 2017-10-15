package ru.spb.se.contexthelper.lookup;

import com.google.code.stackexchange.client.query.StackExchangeApiQueryFactory;
import com.google.code.stackexchange.common.PagedList;
import com.google.code.stackexchange.schema.*;

import java.util.ArrayList;
import java.util.List;

/** Client for StackExchange API. */
public class StackExchangeClient {

  private static final String APPLICATION_KEY = "F)x9bhGombhjqpnXt)5Mwg((";
  private static final StackExchangeSite STACK_EXCHANGE_SITE = StackExchangeSite.STACK_OVERFLOW;

  // TODO(niksaz): Use a filter for only fields, that are used in the plugin.
  private static final String QUESTIONS_FILTER = "default";
  private static final int QUESTIONS_PAGE_SIZE = 100;

  /**
   * To get a set of fields different from the default, we need to provide custom filter.
   * Apart from default fields, this filter ask for the answers.title.
   */
  // TODO(niksaz): Further refine the filter for Answers.
  private static final String ANSWERS_FILER = "!6UYYQsxs_0G)y";
  private static final int ANSWERS_PAGE_SIZE = 10;

  /** Returns Java tagged questions for the given query. */
  public StackExchangeQuestionResults requestRelevantQuestions(String query) {
    StackExchangeApiQueryFactory queryFactory =
        StackExchangeApiQueryFactory.newInstance(APPLICATION_KEY, STACK_EXCHANGE_SITE);
    Paging paging = new Paging(1, QUESTIONS_PAGE_SIZE);
    List<String> tagged = new ArrayList<>();
    tagged.add("java");
    PagedList<Question> questions =
        queryFactory.newAdvanceSearchApiQuery()
            .withQuery(query)
            .withPaging(paging)
            .withTags(tagged)
            .withSort(User.QuestionSortOrder.MOST_RELEVANT)
            .withFilter(QUESTIONS_FILTER)
            .list();
    // TODO(niksaz): Provide access to all questions, not only the first page.
    return new StackExchangeQuestionResults(query, questions);
  }

  /** Returns answers for the question with the given id. */
  public List<Answer> requestAnswersFor(long questionId) {
    StackExchangeApiQueryFactory queryFactory =
        StackExchangeApiQueryFactory.newInstance(APPLICATION_KEY, STACK_EXCHANGE_SITE);
    Paging paging = new Paging(1, ANSWERS_PAGE_SIZE);
    // TODO(niksaz): Provide access to all answers, not only the first page.
    return queryFactory.newAnswerApiQuery()
        .withQuestionIds(questionId)
        .withPaging(paging)
        .withFilter(ANSWERS_FILER)
        .listByQuestions();
  }
}

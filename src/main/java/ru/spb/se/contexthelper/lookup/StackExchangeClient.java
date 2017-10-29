package ru.spb.se.contexthelper.lookup;

import com.google.code.stackexchange.client.query.StackExchangeApiQueryFactory;
import com.google.code.stackexchange.common.PagedList;
import com.google.code.stackexchange.schema.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Client for StackExchange API. */
public class StackExchangeClient {

  private static final String APPLICATION_KEY = "F)x9bhGombhjqpnXt)5Mwg((";
  private static final StackExchangeSite STACK_EXCHANGE_SITE = StackExchangeSite.STACK_OVERFLOW;

  /** Included fields: question.answer_count, question.body, question.title. */
  // TODO(niksaz): Consider including shallow answers in questions, rather than asking for its
  // content later.
  private static final String QUESTIONS_FILTER = "!KGsZNLG*l6eqP";
  private static final int QUESTIONS_PAGE_SIZE = 100;

  /**
   * Included fields: answer.body, answer.creation_date, answer.owner, answer.score,
   * shallow_user.display_name.
   */
  private static final String ANSWERS_FILER = "!6QljBaH0jbgaIk6a";
  public static final int ANSWERS_PAGE_SIZE = 10;

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

  public List<Question> requestQuestionsWith(List<Long> questionIds) {
    StackExchangeApiQueryFactory queryFactory =
        StackExchangeApiQueryFactory.newInstance(APPLICATION_KEY, STACK_EXCHANGE_SITE);
    Paging paging = new Paging(1, QUESTIONS_PAGE_SIZE);
    List<Question> questions =
        queryFactory.newQuestionApiQuery()
            .withQuestionIds(questionIds)
            .withFilter(QUESTIONS_FILTER)
            .withPaging(paging)
            .list();
    // StackExchangeApi does not guarantee the same order of questions returned and initial ids.
    questions.sort(Comparator.comparingInt(quest -> questionIds.indexOf(quest.getQuestionId())));
    return questions;
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

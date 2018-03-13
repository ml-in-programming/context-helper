package ru.spb.se.contexthelper.lookup;

import com.google.code.stackexchange.client.query.StackExchangeApiQueryFactory;
import com.google.code.stackexchange.common.PagedList;
import com.google.code.stackexchange.schema.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Client for StackExchange API. */
public class StackExchangeClient {

  /**
   * Included fields: question.answer_count, question.body, question.title, question.question_id,
   * question.link.
   */
  // TODO(niksaz): Consider including shallow answers, rather than asking for them later.
  private static final String QUESTIONS_FILTER = "!)1mUVrPI9sDnTx-H1.m";
  private static final int QUESTIONS_PAGE_SIZE = 100;

  /**
   * Included fields: answer.answer_id, answer.body, answer.creation_date, answer.owner,
   * answer.score, shallow_user.display_name.
   */
  private static final String ANSWERS_FILER = "!Fcb3plMp6RG-q6e9_9oob)z-X9";
  public static final int ANSWERS_PAGE_SIZE = 10;

  @NotNull
  private final String apiKey;
  @NotNull
  private final StackExchangeSite stackExchangeSite;

  public StackExchangeClient(@NotNull String apiKey, @NotNull StackExchangeSite stackExchangeSite) {
    this.apiKey = apiKey;
    this.stackExchangeSite = stackExchangeSite;
  }

  /** Returns Java tagged questions for the given query. */
  public StackExchangeQuestionResults requestRelevantQuestions(String query) {
    StackExchangeApiQueryFactory queryFactory = getQueryFactory();
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
    StackExchangeApiQueryFactory queryFactory = getQueryFactory();
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
    StackExchangeApiQueryFactory queryFactory = getQueryFactory();
    Paging paging = new Paging(1, ANSWERS_PAGE_SIZE);
    // TODO(niksaz): Provide access to all answers, not only the first page.
    return queryFactory.newAnswerApiQuery()
        .withQuestionIds(questionId)
        .withPaging(paging)
        .withFilter(ANSWERS_FILER)
        .listByQuestions();
  }

  private StackExchangeApiQueryFactory getQueryFactory() {
    return StackExchangeApiQueryFactory.newInstance(apiKey, stackExchangeSite);
  }
}
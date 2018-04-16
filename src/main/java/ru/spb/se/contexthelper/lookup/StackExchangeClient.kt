package ru.spb.se.contexthelper.lookup

import com.google.code.stackexchange.client.query.StackExchangeApiQueryFactory
import com.google.code.stackexchange.schema.Paging
import com.google.code.stackexchange.schema.Question
import com.google.code.stackexchange.schema.StackExchangeSite

/** Contains results that are extracted by [[StackExchangeClient]]. */
data class StackExchangeQuestionResults(
    val queryContent: String,
    val questions: List<Question>)

/** Client for extracting threads from StackExchange API. */
class StackExchangeClient(
    private val apiKey: String,
    private val stackExchangeSite: StackExchangeSite
) {
    fun getQuestionsWithIds(query: String, questionIds: List<Long>): StackExchangeQuestionResults {
        val paging = Paging(1, PAGE_SIZE)
        val questions: List<Question> =
            getQueryFactory().newQuestionApiQuery()
                .withQuestionIds(questionIds)
                .withFilter(QUESTIONS_FILTER)
                .withPaging(paging)
                .list()
        // StackExchangeApi does not guarantee returning the questions in the same order.
        val sortedQuestions =
            questions.sortedBy { question -> questionIds.indexOf(question.questionId) }
        return StackExchangeQuestionResults(query, sortedQuestions)
    }

    private fun getQueryFactory(): StackExchangeApiQueryFactory {
        return StackExchangeApiQueryFactory.newInstance(apiKey, stackExchangeSite)
    }

    companion object {
        /**
         * Included fields: answer.(answer_id, body, comments, is_accepted, score), .wrapper.items,
         * comment.(body, score), question.(answers, body, comments, question_id, score, title).
         */
        // TODO(niksaz): Client need private_scope to access answer.is_accepted.
        private const val QUESTIONS_FILTER = "!E-PL9L5uXc2mRG4-jWQtpb(HdXTeXFtXj.5JHu"
        private const val PAGE_SIZE = 100
    }
}

package ru.spb.se.contexthelper.lookup

import org.junit.Test
import ru.spb.se.contexthelper.context.Keyword
import ru.spb.se.contexthelper.context.Query

class QuestionRecommenderTest {
    @Test
    fun commonLookup() {
        val recommender = QuestionRecommender()
        recommender.loadSuggestions(QUERIES_PATH)
        val query = Query(
            listOf(
                Keyword("string", 2),
                Keyword("split", 1)),
            "Test query")
        val questions = recommender.getRelevantQuestions(query, 5)
        assert(questions.contains("Split letter string in java"))
        assert(questions.contains("Split string from url pattern"))
    }

    @Test
    fun camelCaseClassLookup() {
        val recommender = QuestionRecommender()
        recommender.loadSuggestions(QUERIES_PATH)
        val query = Query(
            listOf(
                Keyword("http", 2),
                Keyword("get", 2),
                Keyword("add", 1)),
            "Test query")
        val questions = recommender.getRelevantQuestions(query, 5)
        assert(questions.contains("Add header to httpget request"))
        assert(questions.contains("Add date header to httpget"))
    }

    companion object {
        private const val QUERIES_PATH = "tasks/suggested.txt"
    }
}
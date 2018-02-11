package ru.spb.se.contexthelper.lookup

import ru.spb.se.contexthelper.context.Query

/** Class which represents loaded suggested questions. */
data class QuestionSuggestion(val wordSequence: List<String>)

/** Class which ranks the extracted StackOverflow queries based on given keywords. */
class QuestionRecommender {
    private var questionSuggestions: List<QuestionSuggestion> = listOf()

    fun loadSuggestions(resourceName: String) {
        val classLoader = javaClass.classLoader
        val inputStream = classLoader.getResourceAsStream(resourceName)!!
        val bufferedReader = inputStream.bufferedReader()
        questionSuggestions = bufferedReader.useLines { lines ->
            lines.map { QuestionSuggestion(it.split(" ")) }.toList()
        }
    }

    fun getRelevantQuestions(query: Query, count: Int): List<String> {
        val indexToScores = questionSuggestions.mapIndexed { index, suggestion ->
            val score = query.keywords.map { keyword ->
                val containsKeyword =
                    suggestion.wordSequence.any { it.hasPrefixOrSuffixEqualTo(keyword.word)}
                if (containsKeyword) keyword.weight else 0
            }.sum()
            index to score
        }
        return indexToScores
            .sortedByDescending { it.second }
            .take(count)
            .map { questionSuggestions[it.first].wordSequence.joinToString(" ").capitalize() }
            .toList()
    }

    private fun String.hasPrefixOrSuffixEqualTo(query: String): Boolean =
        commonPrefixWith(query) == query || commonSuffixWith(query) == query
}
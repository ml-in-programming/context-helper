package ru.spb.se.contexthelper.lookup

import ru.spb.se.contexthelper.context.Query

/** Class which ranks the extracted StackOverflow queries based on given keywords. */
class QueryRecommender {
    private var querySuggestions: List<String> = listOf()

    fun loadSuggestions(resourceName: String) {
        val classLoader = javaClass.classLoader
        val inputStream = classLoader.getResourceAsStream(resourceName)!!
        val bufferedReader = inputStream.bufferedReader()
        querySuggestions = bufferedReader.useLines { it.toList() }
    }

    fun getRelevantQuestions(query: Query, count: Int): List<String> {
        val questions = mutableListOf<String>(/* query.defaultQuestion */)
        val scoredSuggestions = querySuggestions.map { suggestion ->
            val score = query.keywords.map { keyword ->
                val containsKeyword =
                    suggestion
                        .split(" ")
                        .any { it.hasPrefixOrSuffixEqualTo(keyword.word)}
                if (containsKeyword) keyword.weight else 0
            }.sum()
            suggestion to score
        }
        scoredSuggestions
            .sortedByDescending { it.second }
            .take(count)
            .forEach { questions.add(it.first.capitalize()) }
        return questions.toList()
    }

    private fun String.hasPrefixOrSuffixEqualTo(query: String): Boolean =
        commonPrefixWith(query) == query || commonSuffixWith(query) == query
}
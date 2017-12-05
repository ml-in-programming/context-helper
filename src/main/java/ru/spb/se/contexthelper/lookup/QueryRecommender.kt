package ru.spb.se.contexthelper.lookup

import ru.spb.se.contexthelper.context.Query

/** Class which ranks the extracted StackOverflow queries based on given keywords. */
class QueryRecommender {
    private var querySuggestions: List<String> = listOf()

    fun loadQueries(resourceName: String) {
        val classLoader = javaClass.classLoader
        val inputStream = classLoader.getResourceAsStream(resourceName)!!
        val bufferedReader = inputStream.bufferedReader()
        querySuggestions = bufferedReader.useLines { it.toList() }
    }

    fun relevantQuestions(query: Query, count: Int): List<String> {
        val mutableList = mutableListOf(query.defaultQuestion)
        val scoredSuggestions = querySuggestions.map { suggestion ->
            val score = query.keywords.map {
                if (suggestion.contains(it.word)) it.weight else 0
            }.sum()
            suggestion to score
        }
        scoredSuggestions
            .sortedByDescending { it.second }
            .take(count - 1)
            .forEach { mutableList.add(it.first.capitalize()) }
        return mutableList.toList()
    }
}
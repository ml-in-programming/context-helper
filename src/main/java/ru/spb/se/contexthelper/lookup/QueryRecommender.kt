package ru.spb.se.contexthelper.lookup

/** Class which ranks the extracted StackOverflow queries based on given keywords. */
class QueryRecommender {
    private var querySuggestions: List<String> = listOf()

    fun loadQueries(resourceName: String) {
        val classLoader = javaClass.classLoader
        val inputStream = classLoader.getResourceAsStream(resourceName)!!
        val bufferedReader = inputStream.bufferedReader()
        querySuggestions = bufferedReader.useLines { it.toList() }
    }

    fun findSimilar(query: String, count: Int): List<String> {
        val mutableList = mutableListOf(query)
        querySuggestions.take(count - 1).forEach { mutableList.add(it.capitalize()) }
        return mutableList.toList()
    }
}
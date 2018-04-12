package ru.spb.se.contexthelper.lookup

interface QuestionLookupClient {
    /** Returns the list of up to 10 StackOverflow questions ids, relevant to the query. */
    fun lookupQuestionIds(query: String): List<Long>
}
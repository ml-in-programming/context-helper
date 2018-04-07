package ru.spb.se.contexthelper.component

import ru.spb.se.contexthelper.lookup.StackExchangeQuestionResults

/** An entity that wants to listen for suggested [[StackExchangeQuestionResults]]. */
interface QuestionResultsListener {
    /** Returns false if it is not interested in listening to updates anymore. */
    fun receiveResults(questionResults: StackExchangeQuestionResults): Boolean
}
package ru.spb.se.contexthelper.component

import ru.spb.se.contexthelper.lookup.StackExchangeQuestionResults

/** An entity that wants to listen for suggested [[StackExchangeQuestionResults]]. */
interface QuestionResultsListener {
    fun receiveResults(questionResults: StackExchangeQuestionResults)
}

package ru.spb.se.contexthelper.lookup

import com.google.code.stackexchange.schema.Question

/** Contains results that are extracted by [[StackExchangeClient]]. */
data class StackExchangeQuestionResults(val queryContent: String, val questions: List<Question>)
package ru.spb.se.contexthelper.logs

/** Log info about the process and the result of assistance popup. */
data class PopupLog(val keywords: List<KeywordLog>, val questions: List<String>)
package ru.spb.se.contexthelper.stats

import ru.spb.se.contexthelper.context.Keyword

/** Log info about the process and the result of assistance popup. */
data class PopupLog(val keywords: List<Keyword>, val questions: List<String>)
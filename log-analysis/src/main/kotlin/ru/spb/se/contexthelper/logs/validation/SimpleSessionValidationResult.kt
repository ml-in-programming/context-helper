package ru.spb.se.contexthelper.logs.validation

class SimpleSessionValidationResult {
    val errorLines = mutableListOf<String>()
    val validLines = mutableListOf<String>()

    fun addErrorSession(errorSession: List<LogLine>) {
        errorLines.addAll(errorSession.map { it.textLine })
    }

    fun addValidSession(validSession: List<LogLine>) {
        validLines.addAll(validSession.map { it.textLine })
    }
}
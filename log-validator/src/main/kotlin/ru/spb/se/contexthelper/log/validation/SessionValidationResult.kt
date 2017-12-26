package ru.spb.se.contexthelper.log.validation

class SessionValidationResult {
    val errorLines = mutableListOf<String>()
    val validLines = mutableListOf<String>()

    fun addErrorLine(errorLine: String) {
        errorLines.add(errorLine)
    }

    fun addErrorSession(errorSession: List<LogLine>) {
        errorLines.addAll(errorSession.map { it.textLine })
    }

    fun addValidSession(validSession: List<LogLine>) {
        validLines.addAll(validSession.map { it.textLine })
    }
}
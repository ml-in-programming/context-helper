package ru.spb.se.contexthelper.logs.validation

class SimpleSessionValidationResult {
    private val error = mutableListOf<String>()
    private val valid = mutableListOf<String>()

    val errorLines: List<String>
        get() = error

    val validLines: List<String>
        get() = valid

    fun addErrorSession(errorSession: List<LogLine>) {
        error.addAll(errorSession.map { it.textLine })
    }

    fun addValidSession(validSession: List<LogLine>) {
        valid.addAll(validSession.map { it.textLine })
    }
}
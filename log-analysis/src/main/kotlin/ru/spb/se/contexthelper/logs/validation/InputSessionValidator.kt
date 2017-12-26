package ru.spb.se.contexthelper.logs.validation

import ru.spb.se.contexthelper.logs.Log

class InputSessionValidator(private val sessionValidationResult: SimpleSessionValidationResult) {
    fun validate(input: Iterable<String>) {
        var currentSessionUid: String? = null
        val session = mutableListOf<LogLine>()

        for (line in input) {
            if (line.trim().isEmpty()) continue

            val log = Log.fromString(line)
            val logLine = LogLine(log, line)

            if (logLine.log.sessionId == currentSessionUid) {
                session.add(logLine)
            }
            else {
                processSession(session)
                session.clear()
                currentSessionUid = logLine.log.sessionId
                session.add(logLine)
            }
        }
        processSession(session)
    }

    private fun processSession(session: List<LogLine>) {
        if (session.isEmpty()) return
//        if (session.any { !it.isValid }) {
//            storeSession(session, isValidSession = false)
//            return
//        }
//
//        var isValidSession = false
//        var errorMessage = ""
//        val initial = session.first()
//        if (initial.event is CompletionStartedEvent) {
//            val state = CompletionValidationState(initial.event)
//            session.drop(1).forEach { state.accept(it.event!!) }
//            isValidSession = state.isSessionValid()
//            if (!isValidSession) {
//                errorMessage = state.errorMessage()
//            }
//        }
//        else {
//            errorMessage = "Session starts with other event: ${initial.event?.actionType}"
//        }

        storeSession(session, true)
    }

    private fun storeSession(session: List<LogLine>, isValidSession: Boolean) {
        if (isValidSession) {
            sessionValidationResult.addValidSession(session)
        }
        else {
            sessionValidationResult.addErrorSession(session)
        }
    }
}
package ru.spb.se.contexthelper.log.validation

import ru.spb.se.contexthelper.log.ActionType
import ru.spb.se.contexthelper.log.Log

class InputSessionValidator(private val sessionValidationResult: SessionValidationResult) {
    fun validate(input: Iterable<String>) {
        var currentSessionUid: String? = null
        val session = mutableListOf<LogLine>()

        for (line in input) {
            if (line.trim().isEmpty()) continue

            val log = Log.fromTabSeparatedString(line)
            if (log == null) {
                sessionValidationResult.addErrorLine(line)
                continue
            }

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
        val suggestedSomething =
            session.any { it.log.actionType == ActionType.QUERY_POPUP.typeName }
        val selectedSomething =
            session.any { it.log.actionType == ActionType.QUERY_HIT.typeName }
        val isValidSession = suggestedSomething || !selectedSomething
        storeSession(session, isValidSession)
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
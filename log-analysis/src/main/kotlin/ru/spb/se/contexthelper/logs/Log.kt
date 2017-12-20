package ru.spb.se.contexthelper.logs

import com.google.gson.Gson
import ru.spb.se.contexthelper.logs.data.LogData

private object Utils {
    val gson = Gson()
}

/** Context helper's log format. */
data class Log(
    private val stamp: Long,
    private val recorderId: String = "context-helper",
    private val recorderVersion: String = "1.0",
    private val userId: String,
    private val sessionId: String,
    private val actionType: String,
    private val logData: LogData
) {
    override fun toString(): String {
        val jsonLogData = Utils.gson.toJson(logData)
        return "$stamp\t$recorderId\t$recorderVersion\t" +
            "$userId\t$sessionId\t$actionType\t$jsonLogData"
    }
}
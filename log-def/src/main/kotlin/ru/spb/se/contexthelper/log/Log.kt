package ru.spb.se.contexthelper.log

import com.google.gson.Gson
import ru.spb.se.contexthelper.log.data.LogData
import ru.spb.se.contexthelper.log.data.PopupLogData
import ru.spb.se.contexthelper.log.data.SelectionLogData

private object Utils {
    val gson = Gson()
}

/** Context helper's log format. */
data class Log(
    val stamp: Long,
    val recorderId: String = "context-helper",
    val recorderVersion: String = "1.0",
    val userId: String,
    val sessionId: String,
    val actionType: String,
    val logData: LogData
) {
    override fun toString(): String {
        val jsonLogData = Utils.gson.toJson(logData)
        return "$stamp\t$recorderId\t$recorderVersion\t" +
            "$userId\t$sessionId\t$actionType\t$jsonLogData"
    }

    companion object {
        fun fromString(logText: String): Log? {
            try {
                val parts = logText.split("\t")
                val stamp = parts[0].toLong()
                val recorderId = parts[1]
                val recorderVersion = parts[2]
                val userId = parts[3]
                val sessionId = parts[4]
                val actionType = parts[5]
                val logData = when(actionType) {
                    "QUERY_POPUP" -> Utils.gson.fromJson(parts[6], PopupLogData::class.java)
                    "QUERY_HIT" -> Utils.gson.fromJson(parts[6], SelectionLogData::class.java)
                    else -> return null
                }
                return Log(
                    stamp,
                    recorderId,
                    recorderVersion,
                    userId,
                    sessionId,
                    actionType,
                    logData)
            } catch (e: Exception) {
                return null
            }
        }
    }
}
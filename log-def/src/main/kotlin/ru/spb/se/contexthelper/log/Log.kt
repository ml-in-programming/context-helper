package ru.spb.se.contexthelper.log

import com.google.gson.Gson
import ru.spb.se.contexthelper.log.data.LogData

private object Utils {
    val gson = Gson()
}

/** Context helper's log format. */
@Suppress("DataClassPrivateConstructor")
data class Log private constructor (
    val stamp: Long,
    val recorderId: String,
    val recorderVersion: String,
    val userId: String,
    val sessionId: String,
    val actionType: String,
    val logData: LogData
) {
    constructor(
        stamp: Long,
        recorderId: String = "context-helper",
        recorderVersion: String = "1.0",
        userId: String,
        sessionId: String,
        actionType: ActionType,
        logData: LogData
    ) : this(stamp, recorderId, recorderVersion, userId, sessionId, actionType.typeName, logData) {
        assert(logData.javaClass == actionType.clazz) {
            "Argument log data's class does not match action type's class: " +
                "${logData.javaClass} ${actionType.clazz}"
        }
    }

    fun toTabSeparatedString(): String {
        val jsonLogData = Utils.gson.toJson(logData)
        return "$stamp\t$recorderId\t$recorderVersion\t" +
            "$userId\t$sessionId\t$actionType\t$jsonLogData"
    }

    companion object {
        fun fromTabSeparatedString(logText: String): Log? {
            try {
                val parts = logText.split("\t")
                val stamp = parts[0].toLong()
                val recorderId = parts[1]
                val recorderVersion = parts[2]
                val userId = parts[3]
                val sessionId = parts[4]
                val actionTypeName = parts[5]
                val actionType =
                    ActionType.values()
                        .firstOrNull { it.typeName == actionTypeName } ?: return null
                val logData = Utils.gson.fromJson(parts[6], actionType.clazz)
                return Log(
                    stamp,
                    recorderId,
                    recorderVersion,
                    userId,
                    sessionId,
                    actionTypeName,
                    logData)
            } catch (e: Exception) {
                return null
            }
        }
    }
}
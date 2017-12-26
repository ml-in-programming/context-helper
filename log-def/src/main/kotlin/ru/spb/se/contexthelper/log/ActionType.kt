package ru.spb.se.contexthelper.log

import ru.spb.se.contexthelper.log.data.LogData
import ru.spb.se.contexthelper.log.data.PopupLogData
import ru.spb.se.contexthelper.log.data.SelectionLogData

enum class ActionType(val typeName: String, val clazz: Class<out LogData>) {
    QUERY_POPUP("QUERY_POPUP", PopupLogData::class.java),
    QUERY_HIT("QUERY_HIT", SelectionLogData::class.java)
}
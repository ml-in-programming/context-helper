package ru.spb.se.contexthelper.errors

import com.intellij.openapi.project.Project

object ErrorMessageParser{
    private fun parseLines(lines: Array<String>): String =
            when{
                lines[0].compareTo("\n") == 0 -> ""
                else -> lines[0].split("\n")[0].replace(":", "")
            }

    fun parseError(errorMessage: ErrorMessage, project: Project): String{
        val error = errorMessage.get(ErrorMessage.MessageType.ERROR)
        return parseLines(error)
    }
}
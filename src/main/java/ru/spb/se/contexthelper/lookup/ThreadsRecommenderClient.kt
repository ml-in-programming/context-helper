package ru.spb.se.contexthelper.lookup

import java.io.PrintWriter
import java.net.Socket
import java.util.*

/** Client for the backend server of recommending StackOverflow threads. */
class ThreadsRecommenderClient {
    fun askForRecommendedThreads(query: String): List<Long> {
        val leftBraceIndex = query.indexOf('(')
        val rightBraceIndex = query.indexOf(')')
        if (leftBraceIndex == -1 || rightBraceIndex == -1) {
            return emptyList()
        }
        val types = query.substring(leftBraceIndex + 1, rightBraceIndex)
        val typesList = types
            .split("|")
            .filter { it != "\"\"" }
            .map { it.split(".").last() }
            .toList()
        val threadIds = ArrayList<Long>()
        try {
            Socket("localhost", 40000).use { socket ->
                val printWriter = PrintWriter(socket.getOutputStream(), true)
                printWriter.println(typesList.size)
                typesList.forEach { printWriter.println(it) }
                val scanner = Scanner(socket.getInputStream())
                var count = scanner.nextLine().toInt()
                while (count > 0) {
                    threadIds.add(scanner.nextLine().toLong())
                    count -= 1
                }
                scanner.close()
                printWriter.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return threadIds
    }
}

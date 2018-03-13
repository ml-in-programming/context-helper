package ru.spb.se.contexthelper.reporting

import java.io.PrintWriter
import java.net.Socket
import kotlin.concurrent.thread

/** Class for sending the usage statistics to the server. */
class LocalUsageCollector {
    fun sendContextsMessage(
        installationID: String, sessionID: String, caretOffset: Int, documentText: String) {
        thread {
            try {
                Socket(hostName, contextsPortNumber).use { socket ->
                    PrintWriter(socket.getOutputStream(), true).use { printWriter ->
                        printWriter.println(installationID)
                        printWriter.println(sessionID)
                        printWriter.println(caretOffset)
                        printWriter.println(documentText)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val hostName = "93.92.205.31"

        private const val contextsPortNumber = 25001
        private const val questionsPortNumber = 25002
        private const val clicksPortNumber = 25003
        private const val helpfulPortNumber = 25004
    }
}
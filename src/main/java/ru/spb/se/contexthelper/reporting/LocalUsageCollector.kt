package ru.spb.se.contexthelper.reporting

import com.intellij.openapi.diagnostic.Logger
import java.io.PrintWriter
import java.net.Socket
import kotlin.concurrent.thread

/** Class for sending the usage statistics to the server. */
class LocalUsageCollector(private val hostName: String) {
    fun sendContextsMessage(
            installationID: String, sessionID: String, caretOffset: Int, documentText: String) {
        sendAsynchronously(contextsPortNumber) { printWriter ->
            printWriter.println(installationID)
            printWriter.println(sessionID)
            printWriter.println(caretOffset)
            printWriter.println(documentText)
        }
    }

    fun sendQuestionsMessage(sessionID: String, request: String, questionIds: List<Long>) {
        sendAsynchronously(questionsPortNumber) { printWriter ->
            printWriter.println(sessionID)
            printWriter.println(request)
            questionIds.forEach { printWriter.println(it) }
        }
    }

    fun sendClicksMessage(sessionID: String, itemID: String) {
        sendAsynchronously(clicksPortNumber) { printWriter ->
            printWriter.println(sessionID)
            printWriter.println(itemID)
        }
    }

    fun sendHelpfulMessage(sessionID: String, itemID: String) {
        sendAsynchronously(helpfulPortNumber) { printWriter ->
            printWriter.println(sessionID)
            printWriter.println(itemID)
        }
    }

    private fun sendAsynchronously(portNumber: Int, printRoutine: (PrintWriter) -> Unit) {
        thread {
            try {
                Socket(hostName, portNumber).use { socket ->
                    PrintWriter(socket.getOutputStream(), true).use { printWriter ->
                        printRoutine(printWriter)
                    }
                }
            } catch (e: Exception) {
                LOG.info(e)
            }
        }
    }

    companion object {
        private val LOG = Logger.getInstance(LocalUsageCollector::class.java)

        private const val contextsPortNumber = 25001
        private const val questionsPortNumber = 25002
        private const val clicksPortNumber = 25003
        private const val helpfulPortNumber = 25004
    }
}
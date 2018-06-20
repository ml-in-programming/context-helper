package ru.spb.se.contexthelper.lookup

import com.intellij.openapi.diagnostic.Logger
import ru.spb.se.contexthelper.context.Query
import java.io.PrintWriter
import java.net.Socket
import java.util.*

/** Client for the backend server of recommending StackOverflow threads. */
class ThreadsRecommenderClient {
    fun askForRecommendedThreads(query: Query): List<Long> {
        val keywords = query.keywords
        val threadIds = ArrayList<Long>()
        try {
            Socket("localhost", 40000).use { socket ->
                val printWriter = PrintWriter(socket.getOutputStream(), true)
                printWriter.println(keywords.size)
                keywords.forEach { printWriter.println(it.word) }
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
            LOG.info(e)
        }
        return threadIds
    }

    companion object {
        private val LOG = Logger.getInstance(ThreadsRecommenderClient::class.java)
    }
}

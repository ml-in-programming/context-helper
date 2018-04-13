package ru.spb.se.contexthelper.reporting

import kotlin.concurrent.thread

class StatsCollector {
    private val reports: MutableList<String> = mutableListOf()
    private var size: Int = 0
    private var lastSendingThread: Thread? = null

    fun appendReport(report: String) {
        if (size + report.length + System.lineSeparator().length > BYTES_LIMIT) {
            flush()
        }
        size += report.length + System.lineSeparator().length
        reports.add(report)
    }

    fun flush() {
        val reportsToSent = reports.toList()
        reports.clear()
        size = 0
        lastSendingThread = thread {
            StatsSender.send(reportsToSent.joinToString(System.lineSeparator()))
        }
    }

    fun ensureSent() {
        lastSendingThread?.join()
    }

    companion object {
        private val BYTES_LIMIT = 250 * 1024
    }
}
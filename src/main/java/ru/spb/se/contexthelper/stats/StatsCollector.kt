package ru.spb.se.contexthelper.stats

import kotlin.concurrent.thread

class StatsCollector {
    private val reports: MutableList<String> = mutableListOf()
    private var size: Int = 0

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
        thread {
            StatsSender.send(reportsToSent.joinToString(System.lineSeparator()))
            // TODO(niksaz): Remove debug output.
            println(reportsToSent.joinToString(
                System.lineSeparator(),
                "/*" + System.lineSeparator(),
                System.lineSeparator() + "*/"))
        }
    }

    companion object {
        private val BYTES_LIMIT = 250 * 1024
    }
}
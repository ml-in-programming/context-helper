package ru.spb.se.contexthelper.logs

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import ru.spb.se.contexthelper.logs.data.SelectionLogData

class LogTest {
    @Test
    fun testSerialization() {
        val log = Log(
            1513801181316L,
            "context-helper",
            "1.0",
            "b27fe89c239d",
            "513801179873",
            "QUERY_HIT",
            SelectionLogData(0)
        )
        val logText = log.toString()
        assertThat(logText).isEqualTo(
            "1513801181316\tcontext-helper\t1.0" +
                "\tb27fe89c239d\t513801179873\tQUERY_HIT\t{\"numberSelected\":0}")
    }

    @Test
    fun testDeserialization() {
        val logText = "1513801181316\tcontext-helper\t1.0" +
            "\tb27fe89c239d\t513801179873\tQUERY_HIT\t{\"numberSelected\":0}"
        val log = Log.fromString(logText)
        assertThat(log).isEqualTo(Log(
            1513801181316L,
            "context-helper",
            "1.0",
            "b27fe89c239d",
            "513801179873",
            "QUERY_HIT",
            SelectionLogData(0)))
    }
}
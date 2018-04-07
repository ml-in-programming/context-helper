package ru.spb.se.contexthelper.ui

import org.junit.Test

import com.google.common.truth.Truth.assertThat
import ru.spb.se.contexthelper.ui.StackExchangeThreadsTreeCellRender.Companion.convertScoreToString

class StackExchangeThreadsTreeCellRenderTest {
    @Test
    fun convertPositiveScoreToString() {
        val formatted = convertScoreToString(19)
        assertThat(formatted).isEqualTo("+19")
    }

    @Test
    fun convertZeroScoreToString() {
        val formatted = convertScoreToString(0)
        assertThat(formatted).isEqualTo("+0")
    }

    @Test
    fun convertNegativeScoreToString() {
        val formatted = convertScoreToString(-736)
        assertThat(formatted).isEqualTo("-736")
    }
}

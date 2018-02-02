package ru.spb.se.contexthelper.log.validator

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import ru.spb.se.contexthelper.log.validation.InputSessionValidator
import ru.spb.se.contexthelper.log.validation.SessionValidationResult
import java.io.File

class InputSessionValidatorTest {
    @Test
    fun checkValidSessions1() {
        val lines = File("src/test/resources/validSessions1.txt").readLines()
        val result = validateLines(lines)

        assertThat(result.validLines).hasSize(6)
        assertThat(result.validLines).containsExactlyElementsIn(lines)
        assertThat(result.errorLines).hasSize(0)
    }

    @Test
    fun checkValidSessions2() {
        val lines = File("src/test/resources/validSessions2.txt").readLines()
        val result = validateLines(lines)

        assertThat(result.validLines).hasSize(5)
        assertThat(result.validLines).containsExactlyElementsIn(lines)
        assertThat(result.errorLines).hasSize(0)
    }

    @Test
    fun checkLexicallyInvalidSessions() {
        val lines = File("src/test/resources/lexicallyInvalidSessions.txt").readLines()
        val result = validateLines(lines)

        assertThat(result.validLines).hasSize(0)
        assertThat(result.errorLines).hasSize(7)
        assertThat(result.errorLines).containsExactlyElementsIn(lines)
    }

    @Test
    fun checkSemanticallyInvalidSessions() {
        val lines = File("src/test/resources/semanticallyInvalidSessions.txt").readLines()
        val result = validateLines(lines)

        assertThat(result.validLines).hasSize(0)
        assertThat(result.errorLines).hasSize(3)
        assertThat(result.errorLines).containsExactlyElementsIn(lines)
    }

    private fun validateLines(lines: List<String>): SessionValidationResult {
        val result = SessionValidationResult()
        val validator = InputSessionValidator(result)
        validator.validate(lines)
        return result
    }
}
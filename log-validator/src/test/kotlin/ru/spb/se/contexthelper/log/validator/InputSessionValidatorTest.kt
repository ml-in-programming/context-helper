package ru.spb.se.contexthelper.log.validator

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import ru.spb.se.contexthelper.log.validation.InputSessionValidator
import ru.spb.se.contexthelper.log.validation.SessionValidationResult
import java.io.File

class InputSessionValidatorTest {
    @Test
    fun checkValidSessions1() {
        val result = SessionValidationResult()
        val validator = InputSessionValidator(result)
        val lines = File("src/test/resources/validSessions1.txt").readLines()
        validator.validate(lines)

        assertThat(result.validLines).hasSize(6)
        assertThat(result.validLines).containsExactlyElementsIn(lines).inOrder()
        assertThat(result.errorLines).hasSize(0)
    }

    @Test
    fun checkValidSessions2() {
        val result = SessionValidationResult()
        val validator = InputSessionValidator(result)
        val lines = File("src/test/resources/validSessions2.txt").readLines()
        validator.validate(lines)

        assertThat(result.validLines).hasSize(5)
        assertThat(result.validLines).containsExactlyElementsIn(lines).inOrder()
        assertThat(result.errorLines).hasSize(0)
    }

    @Test
    fun checkLexicallyInvalidSessions() {
        val result = SessionValidationResult()
        val validator = InputSessionValidator(result)
        val lines = File("src/test/resources/lexicallyInvalidSessions.txt").readLines()
        validator.validate(lines)

        assertThat(result.validLines).hasSize(0)
        assertThat(result.errorLines).hasSize(6)
        assertThat(result.errorLines).containsExactlyElementsIn(lines).inOrder()
    }
}
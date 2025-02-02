package io.github.rrevo.karna.json

import org.testng.annotations.Test

class JazzerTest {
    @Test(expectedExceptions = [KarnaException::class])
    fun characterInNumericLiteral() {
        val json = "0r"
        Parser.default().parse(StringBuilder(json))
    }

    @Test(expectedExceptions = [KarnaException::class])
    fun numericKeyAndObject() {
        val json = "{1{"
        Parser.default().parse(StringBuilder(json))
    }

    @Test(expectedExceptions = [KarnaException::class])
    fun numericKeyAndArray() {
        val json = "{3["
        Parser.default().parse(StringBuilder(json))
    }

    @Test(expectedExceptions = [KarnaException::class])
    fun numericKeyAndString() {
        val json = "{0\"\""
        Parser.default().parse(StringBuilder(json))
    }

    @Test(expectedExceptions = [KarnaException::class])
    fun incompleteUnicodeEscape() {
        val json = "\"\\u"
        Parser.default().parse(StringBuilder(json))
    }

    @Test(expectedExceptions = [KarnaException::class])
    fun nonNumericUnicodeEscape() {
        val json = "\"\\u\\\\{["
        Parser.default().parse(StringBuilder(json))
    }
}

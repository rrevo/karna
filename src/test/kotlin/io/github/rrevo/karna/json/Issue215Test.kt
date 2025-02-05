package io.github.rrevo.karna.json

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

@Test
class Issue215Test {
    fun issue215() {
        val input = """{"hi" : "hello"}"""
        val map = Karna().parse<Map<String, String>>(input)
        assertThat(map).isEqualTo(mapOf("hi" to "hello"))
    }
}
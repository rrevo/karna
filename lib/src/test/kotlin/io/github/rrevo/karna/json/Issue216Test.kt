package io.github.rrevo.karna.json

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

@Test
class Issue216Test {
    fun issue216() {
        val m = mapOf("x" to "y", "n" to null)
        val result = Karna().toJsonString(m)
        assertThat(result).isEqualTo("""{"x": "y", "n": null}""")
    }
}
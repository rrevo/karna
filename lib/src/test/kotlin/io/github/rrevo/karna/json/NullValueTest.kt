package io.github.rrevo.karna.json

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

@Test
class NullValueTest {
    fun nullStrings() {
        assertThat(Karna().toJsonString(listOf(1, 2, null, null, 3)))
                .isEqualTo("[1, 2, null, null, 3]")
    }
}

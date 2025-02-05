package io.github.rrevo.karna.json

import org.assertj.core.api.Assertions

object Asserts {
    fun assertContains(s1: String, s2: String) = Assertions.assertThat(s1).contains(s2)
}
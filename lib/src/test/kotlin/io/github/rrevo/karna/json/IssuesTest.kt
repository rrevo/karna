package io.github.rrevo.karna.json

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

@Test
class IssuesTest {
    fun issue219() {
        class Test(val values: Array<Int>)
        val test: Test = Karna().parse(""" { "values": [1,2,4] } """.trimIndent())!!
        assertThat(test.values).isEqualTo(arrayOf(1, 2, 4))
    }

    data class Product(
            val foo: Double? = 0.0
    )

    fun issue282() {
        val expected = 434343000000
        val r = Karna().parse<Product>("""{"foo": $expected}""")
        assertThat(r!!.foo).isEqualTo(expected.toDouble())
    }
}
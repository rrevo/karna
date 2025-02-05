package io.github.rrevo.karna.json

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test
import kotlin.reflect.KProperty

@Test
class PropertyStrategyTest {
    private fun runTest(enabled: Boolean) {
        data class Simple(val field1: String, val field2: String = "right")
        val ps = object: PropertyStrategy {
            override fun accept(property: KProperty<*>) = property.name != "field2"
        }
        val ps2 = object: PropertyStrategy {
            override fun accept(property: KProperty<*>) = true
        }
        val karna = Karna()
                .propertyStrategy(ps2)
        if (enabled) karna.propertyStrategy(ps)

        val r = karna.parse<Simple>("""
                { "field1": "b", "field2": "shouldBeIgnored" }
            """)
        assertThat(r).isEqualTo(if (enabled) Simple("b", "right") else Simple("b", "shouldBeIgnored"))
    }

    @Test
    fun test1() = runTest(true)

    @Test
    fun test2() = runTest(false)
}
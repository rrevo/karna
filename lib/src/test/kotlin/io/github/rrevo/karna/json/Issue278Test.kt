package io.github.rrevo.karna.json

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test
import java.math.BigDecimal

@Test
class Issue278Test {
    val BigDecimalConverter = object : Converter {
        override fun canConvert(cls: Class<*>): Boolean = cls == BigDecimal::class.java

        override fun fromJson(jv: JsonValue): Any? {
            println("JsonValue = ${jv}")
            return BigDecimal.valueOf(jv.longValue!!)
        }

        override fun toJson(value: Any): String {
            TODO("not implemented")
        }
    }

    fun test() {
        data class Economy (
                val nationalDebt : BigDecimal
        )
        val expected = 9007199254740991
        val json = "{ \"nationalDebt\" : $expected }"
        val Karna = Karna().converter(BigDecimalConverter)
        val obj = Karna.parse<Economy>(json)
        assertThat(obj!!.nationalDebt).isEqualTo(BigDecimal.valueOf(expected))
    }
}
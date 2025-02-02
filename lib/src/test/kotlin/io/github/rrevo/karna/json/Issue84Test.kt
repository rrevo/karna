package io.github.rrevo.karna.json

import org.testng.annotations.Test
import java.time.Instant

class Issue84Test {
    /**
     * This test passes.
     */
    @Test
    fun serializeNestedInstant() {
        data class Person(val firstName: String, val dob: Instant)

        class EpochMilliInstantConverter: Converter {
            override fun canConvert(cls: Class<*>) = cls == Instant::class.java
            override fun toJson(value: Any) = (value as Instant).toEpochMilli().toString()
            override fun fromJson(jv: JsonValue) = throw NotImplementedError()
        }

        // No custom converter, should be the toString() of the instant
        val obj = Person("John", Instant.ofEpochMilli(9001))
        val result = Karna().toJsonString(obj)

        // Custom converter, expect the converted value
        val mapper = Karna().converter(EpochMilliInstantConverter())
        Asserts.assertContains(mapper.toJsonString(obj), "9001")
    }

    @Test
    fun serializeListOfInstants() {
        val dates = listOf(Instant.ofEpochMilli(9001))

        class EpochMilliInstantConverter: Converter{
            override fun canConvert(cls: Class<*>) = cls == Instant::class.java
            override fun toJson(value: Any) = (value as Instant).toEpochMilli().toString()
            override fun fromJson(jv: JsonValue) = throw NotImplementedError()
        }

        // despite custom converter being provided, instant is not serialized.  Empty value in list
        val mapper = Karna().converter(EpochMilliInstantConverter())
        val result = mapper.toJsonString(dates)
        Asserts.assertContains(result, "9001")
    }
}
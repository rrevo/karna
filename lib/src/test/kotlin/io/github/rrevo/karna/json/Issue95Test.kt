package io.github.rrevo.karna.json

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

class Issue95Test {
    @Test
    fun deserializeStringArray() {
        val mapper = Karna()
        val data = listOf("foo", "bar", "baz")
        val json = mapper.toJsonString(data)
        assertThat(mapper.parseArray<String>(json)).isEqualTo(data)
    }

    @Test
    fun deserializeIntArray() {
        val mapper = Karna()
        val data = listOf(1,2,3)
        val json = mapper.toJsonString(data)
        assertThat(mapper.parseArray<Int>(json)).isEqualTo(data)
    }

    @Test
    fun deserializeObjectArray() {
        val mapper = Karna()
        data class Person(val name: String)
        val data = listOf(Person("John"), Person("Jane"))
        val json = mapper.toJsonString(data)
        assertThat(mapper.parseArray<Person>(json)).isEqualTo(data)
    }

    @Test
    fun serializeStringArrayToObjectArray() {
        data class Person(val id: String, val name: String)
        class PersonConverter: Converter {
            override fun canConvert(cls: Class<*>) = cls == Person::class.java
            override fun toJson(value: Any) = (value as Person).let { value -> "\"${value.id}:${value.name}\"" }
            override fun fromJson(jv: JsonValue): Person {
                val (id, name) = jv.string!!.split(":")
                return Person(id, name)
            }
        }

        val data = listOf(Person("1", "John"), Person("2", "Jane"))
        val mapper = Karna().converter(PersonConverter())
        val json = mapper.toJsonString(data)
        val result = mapper.parseArray<Person>(json)
        assertThat(result).isEqualTo(data)
    }
}

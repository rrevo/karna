package io.github.rrevo.karna.json

import org.testng.Assert
import org.testng.annotations.Test

@Test
class ParseFromEmptyNameTest {

    fun nameSetToEmptyString() {
        data class EmptyName (
                @Json(name = "")
                val empty: String)
        val sampleJson = """{"":"value"}"""
        val result = Karna().parse<EmptyName>(sampleJson)

        Assert.assertNotNull(result)
        Assert.assertEquals(result!!.empty, "value")
    }

    fun nameSetToDefaultValue() {
        data class SpecificName (
                @Json(name = NAME_NOT_INITIALIZED)
                val oddName: String)
        val sampleJson = """{"$NAME_NOT_INITIALIZED":"value"}"""
        Assert.assertThrows(KarnaException::class.java) {
            Karna().parse<SpecificName>(sampleJson)
        }
    }
}

package io.github.rrevo.karna.json

import org.testng.Assert
import org.testng.annotations.Test

@Test
class Issue82Test {

    fun serializePrivateVal() {
        data class Person(private val id: String, val firstName: String)
        val obj = Person("1", "John")
        assertTest(Karna().toJsonString(obj))
    }

    /**
     * Ignoring a field with the @Json annotation does nothing
     * Test fails.  Serialized output is actually "{\"firstName\" : \"John\", \"id\" : \"1\"}"
     */
    fun serializeIgnoredVal() {
        data class Person(@Json(ignored=true) val id: String, val firstName: String)
        val obj = Person("1", "John")
        assertTest(Karna().toJsonString(obj))
    }

    private fun assertTest(jv: String) {
        Assert.assertTrue(jv.contains("firstName"))
        Assert.assertTrue(jv.contains("John"))
        Assert.assertFalse(jv.contains("id"))
        Assert.assertFalse(jv.contains("1"))
    }
}
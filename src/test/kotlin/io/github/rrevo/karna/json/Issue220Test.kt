package io.github.rrevo.karna.json

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.testng.annotations.Test
import java.time.LocalDate
import kotlin.test.fail

@Test
class Issue220Test {
    private val dateTime = object : Converter {
        override fun canConvert(cls: Class<*>) = cls == LocalDate::class.java
        override fun fromJson(jv: JsonValue): LocalDate = LocalDate.parse(jv.string)
        override fun toJson(value: Any): String = "\"$value\""
    }

    // numberOfEyes contains a String instead of an Int
    private val referenceFailingTestFixture = """
    {
      "lastName": "Doe",
      "firstName": "Jane",
      "dateOfBirth": "1990-11-23",
      "numberOfEyes": "2"
    }
    """.trimIndent()

    data class Person(
            val lastName: String,
            val firstName: String,
            val dateOfBirth: LocalDate,
            val numberOfEyes: Int
    )

    fun displayMeaningfulErrorMessage() {
        val karna = Karna().apply {
            converter(dateTime)
        }

        try {
            val janeDoe = karna.parse<Person>(referenceFailingTestFixture)
            assertThat(janeDoe!!.numberOfEyes).isGreaterThan(0)
            fail("Should have failed to parse that JSON")
        } catch(e: KarnaException) {
            assertThat(e.message).contains("Parameter numberOfEyes")
        }
    }

}
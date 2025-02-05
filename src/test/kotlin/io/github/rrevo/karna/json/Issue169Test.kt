package io.github.rrevo.karna.json

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

@Test
class Issue169Test {

    data class Person(val id: Int,
                      val name: String,
                      val isEUResident: Boolean = false,
                      val city: String = "Paris"
                     )

    private val expected = Person(id = 2,
                                  name = "Arthur")

    fun issue169() {

        // language=JSON
        val jsonToTest = """
            {
              "id": 2,
              "name": "Arthur"
            }
        """.trimIndent()

        val toTest = Karna().parse<Person>(jsonToTest)!!

        assertThat(toTest.city)
                .isNotNull()
        assertThat(toTest)
                .isEqualTo(expected)
    }
}
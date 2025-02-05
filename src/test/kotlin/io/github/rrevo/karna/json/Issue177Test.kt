package io.github.rrevo.karna.json

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

@Test
class Issue177Test {

    data class UserData(val id: Int,
                      val name: String,
                      val role: String,
                      val additionalRole: String? = ""
                     )

    private val expected = UserData(1, "Jason", "SuperUser", null)

    fun issue177() {

        // language=JSON
        val jsonToTest = """
            {
              "id": 1,
              "name": "Jason",
              "role": "SuperUser",
              "additionalRole": null
            }
        """.trimIndent()

        val toTest = Karna().parse<UserData>(jsonToTest)

        toTest?.let{
            assertThat(toTest.additionalRole)
                    .isNull()
            assertThat(toTest)
                    .isEqualTo(expected)
        } ?: throw AssertionError("Expected object to be not null")

    }
}
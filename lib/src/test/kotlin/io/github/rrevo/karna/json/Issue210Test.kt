package io.github.rrevo.karna.json

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

@Test(description = "Tests that null JSON values are correctly handled")
class Issue210Test {
    data class DClass(val some: String, val none: String?)

    fun issue210() {
        val json = """
        {
        "some": "test",
        "none": null
        }
        """

        val p = Karna().parse<DClass>(json)
        assertThat(p!!.none).isNull()

    }
}

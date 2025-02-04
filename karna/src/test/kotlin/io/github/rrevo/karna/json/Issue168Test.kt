package io.github.rrevo.karna.json

import org.testng.annotations.Test

@Test
class Issue168Test {
    val jsonString = """
    {
        "data": [
            ["a", "b", null, "c"],
            ["d", "e", null, "f"]
        ]
    }
    """

    data class Data(
        val data: List<List<String?>>
    )

    fun issue168() {
        val karna = Karna()
        val parsed = karna.parse<Data>(jsonString)
    }
}
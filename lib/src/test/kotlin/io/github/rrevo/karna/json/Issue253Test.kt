package io.github.rrevo.karna.json

import org.testng.annotations.Test
import kotlin.test.assertEquals

@Test
class Issue253Test {

    data class ObjWithNullAttr(
            val myAttr: Int?
    )

    fun issue253() {
        val obj = ObjWithNullAttr(null)
        val jsonStr = Karna().toJsonString(obj)
        assertEquals(
                """{"myAttr":null}""",
                jsonStr.replace(" ", "")
        )
        Karna().parse<ObjWithNullAttr>(jsonStr)
    }
}

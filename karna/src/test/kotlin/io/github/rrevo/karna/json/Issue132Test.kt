package io.github.rrevo.karna.json

import org.testng.annotations.Test

@Test
class Issue132Test {
    @Test(expectedExceptions = [(KarnaException::class)])
    fun recursion() {

        class KNode(val next: KNode)

        val converter = object : Converter {
            override fun canConvert(cls: Class<*>): Boolean = cls == Node::class.java

            override fun toJson(value: Any): String {
                return "string"
            }

            override fun fromJson(jv: JsonValue): Any {
                return ""
            }
        }

        Karna().converter(converter).parse<KNode>("{}")

    }
}
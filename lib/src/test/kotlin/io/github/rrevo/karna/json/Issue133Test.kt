package io.github.rrevo.karna.json

import org.testng.annotations.Test

enum class THING {
    TYPE1,
    TYPE2
}
data class Data(val types: List<THING> =listOf(THING.TYPE1))

@Test
class Issue133Test {
    fun issue133() {
        val json= """{"types" : ["TYPE1"]}"""
        val instance = Karna().parse<Data>(json)!! //parse JSON without failure
        /*
            The line below will fail. The (correctly) inferred type is THING,
            but what we get back is type String.
         */
        val type = instance.types[0]
    }
}

package io.github.rrevo.karna.json

import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.io.StringReader

data class Thinger(
        val width: Float?=null
)

class ThingerTest {
    @Test
    fun issue197() {
        val input = """{"width": 2}"""
        val thinger = Karna().parse<Thinger>(StringReader(input))!!

        assertEquals(thinger.width!!.toFloat(), 2f)
    }
}
package io.github.rrevo.karna.json

import org.testng.annotations.Test
import kotlin.test.assertEquals

@Test
class ParseObjectTest {

    object Foo

    fun `test parsing an object`() {
        assertEquals(Foo, Karna().parse<Foo>("{}"))
    }
}

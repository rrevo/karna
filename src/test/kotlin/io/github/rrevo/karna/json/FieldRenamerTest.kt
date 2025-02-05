package io.github.rrevo.karna.json

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

@Test
class FieldRenamerTest {
    fun stringTest() {
        assertThat(FieldRenamer.camelToUnderscores("abcDefGhi")).isEqualTo("abc_def_ghi")
        assertThat(FieldRenamer.underscoreToCamel("abc_def_ghi")).isEqualTo("abcDefGhi")
    }

    val renamer = object: FieldRenamer {
        override fun toJson(fieldName: String) = FieldRenamer.camelToUnderscores(fieldName)
        override fun fromJson(fieldName: String) = FieldRenamer.underscoreToCamel(fieldName)
    }

    class C(val someField: Int)

    private fun privateRenaming(useRenamer: Boolean): C? {
        val json = """
           {
              "some_field": 42
           }
        """


        val karna = Karna()
        if (useRenamer) karna.fieldRenamer(renamer)
        return karna.parse<C>(json)
    }

    @Test(expectedExceptions = [KarnaException::class])
    fun withoutRenamerFromJson() {
        privateRenaming(false)
    }

    fun withRenamerFromJson() {
        val c = privateRenaming(true)
        assertThat(c!!.someField).isEqualTo(42)
    }

    private fun privateRenamingToJson(useRenamer: Boolean): String {
        val c = C(42)
        val Karna = Karna()
        if (useRenamer) Karna.fieldRenamer(renamer)
        return Karna.toJsonString(c)
    }

    fun withoutRenamerToJson() {
        assertThat(privateRenamingToJson(false)).contains("someField")
    }

    fun withRenamerToJson() {
        assertThat(privateRenamingToJson(true)).contains("some_field")
    }
}


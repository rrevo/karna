package io.github.rrevo.karna.json

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Percentage
import org.testng.Assert
import org.testng.annotations.Test

data class Card(val value: Int, val suit: String)
data class Deck1(val card: Card, val cardCount: Int)

@Test
class BindingTest {

    //
    // Tests objects -> JSON string
    //

    data class ArrayHolder(var listOfInts: List<Int> = emptyList(),
            var listOfStrings : List<String> = emptyList(),
            var listOfBooleans: List<Boolean> = emptyList(),
            var string: String = "foo", var isTrue: Boolean = true, var isFalse: Boolean = false)

    fun arrayToJson() {
        val karna = Karna()
        val h = ArrayHolder(listOf(1, 3, 5),
                listOf("d", "e", "f"),
                listOf(true, false, true))
        val s = karna.toJsonString(h)
        listOf("\"listOfInts\" : [1, 3, 5]",
                "\"listOfStrings\" : [\"d\", \"e\", \"f\"]",
                "\"listOfBooleans\" : [true, false, true]",
                "\"string\" : \"foo\"",
                "\"isTrue\" : true",
                "\"isFalse\" : false").forEach {
            assertThat(s).contains(it)
        }
    }

    val CARD_CONVERTER = object: Converter {
        override fun canConvert(cls: Class<*>) = cls == Card::class.java

        override fun fromJson(jv: JsonValue) = Card(jv.objInt("value"), jv.objString("suit"))

        override fun toJson(v: Any) = (v as Card).let { value ->
            """
                    "value" : ${value.value},
                    "suit": "${value.suit.toUpperCase()}"
                """
        }
    }

    fun objectsToJson() {
        val deck1 = Deck1(cardCount = 1, card = Card(13, "Clubs"))
        val s = Karna()
                .converter(CARD_CONVERTER)
                .toJsonString(deck1)
        listOf("\"CLUBS\"", "\"suit\"", "\"value\"", "13", "\"cardCount\"", "1").forEach {
            Asserts.assertContains(s, it)
        }
    }

    fun doubleTest() {
        class C(val f: Float)
        val json = """ { "f": 1.23 } """
        val r = Karna().parse<C>(json)
        assertThat(r?.f).isCloseTo(1.23f, Percentage.withPercentage(1.0))
    }

    //
    // Tests parsing
    //

    data class AllTypes constructor(var int: Int? = null,
            var string: String? = null,
            var isTrue: Boolean? = null,
            var isFalse: Boolean? = null,
            val balanceDouble: Double,
            var array: List<Int> = emptyList())
    fun allTypes() {
        val expectedDouble = Double.MAX_VALUE - 1
        val result = Karna().parse<AllTypes>("""
        {
            "int": 42,
            "array": [11, 12],
            "string": "foo",
            "isTrue": true,
            "isFalse": false,
            "balanceDouble": $expectedDouble
        }
        """)
        Assert.assertEquals(result, AllTypes(42, "foo", true, false, expectedDouble, listOf(11, 12)))
    }

    fun compoundObject() {
        val jsonString = json {
            obj(
                "cardCount" to 2,
                "card" to obj(
                    "value" to 5,
                    "suit" to "Hearts"
                )
            )
        }.toJsonString()

        val result = Karna().parse<Deck1>(jsonString)
        if (result != null) {
            Assert.assertEquals(result, Deck1(Card(5, "Hearts"), 2))
        } else {
            Assert.fail("Should have received a non null deck")
        }
    }

    fun compoundObjectWithConverter() {
        val result = Karna()
                .converter(CARD_CONVERTER)
                .parse<Deck1>("""
        {
          "cardCount": 2,
          "card":
            {"value" : 5,"suit" : "Hearts"}
        }
        """)

        if (result != null) {
            Assert.assertEquals(result, Deck1(Card(5, "Hearts"), 2))
        } else {
            Assert.fail("Should have received a non null deck")
        }
    }

    data class Deck2(
        var cards: List<Card> = emptyList(),
        var cardCount: Int? = null
    )

    fun compoundObjectWithArray() {
        val result = Karna()
                .parse<Deck2>("""
        {
          "cardCount": 2,
          "cards": [
            {"value" : 5, "suit" : "Hearts"},
            {"value" : 8, "suit" : "Spades"},
          ]
        }
    """)

        if (result != null) {
            Assert.assertEquals(result.cardCount!!.toInt(), 2)
            Assert.assertEquals(result.cards, listOf(Card(5, "Hearts"), Card(8, "Spades")))
        } else {
            Assert.fail("Should have received a non null deck")
        }
    }

    fun compoundObjectWithObjectWithConverter() {
        val json = """{
            "preferences": [1,2,3],
            "properties":{"a":"b"}
            }"""

        data class Person(
                val preferences: List<Int>,
                val properties: Map<String, String> = sortedMapOf("a" to "b")
        )

        val p: Person = Karna().parse(json)!!
        Assert.assertEquals(p, Person(listOf(1, 2, 3), mapOf("a" to "b")))
    }

    fun compoundObjectWithArrayWithConverter() {
        val result = Karna()
                .converter(CARD_CONVERTER)
                .parse<Deck2>("""
        {
          "cardCount": 2,
          "cards": [
            {"value" : 5, "suit" : "Hearts"},
            {"value" : 8, "suit" : "Spades"},
          ]
        }
    """)

        if (result != null) {
            Assert.assertEquals(result.cardCount!!.toInt(), 2)
            Assert.assertEquals(result.cards, listOf(Card(5, "Hearts"), Card(8, "Spades")))
        } else {
            Assert.fail("Should have received a non null deck")
        }
    }

    class Mapping(
        @Json(name = "theName")
        val name: String
    )

    fun toJsonStringHonorsJsonAnnotation() {
        val s = Karna().toJsonString(Mapping("John"))
        Assert.assertTrue(s.contains("theName"))
    }

    @Test(expectedExceptions = [(KarnaException::class)])
    fun badFieldMapping() {
        Karna().parse<Mapping>("""
        {
          "name": "foo"
        }
        """)
    }

    fun goodFieldMapping() {
        val result = Karna().parse<Mapping>("""
        {
          "theName": "foo"
        }
        """)
        Assert.assertEquals(result?.name, "foo")
    }

    enum class Cardinal { NORTH, SOUTH }
    class Direction(var cardinal: Cardinal? = null)
    fun enum() {
        val result = Karna().parse<Direction>("""
            { "cardinal": "NORTH" }
        """
        )
        Assert.assertEquals(result?.cardinal, Cardinal.NORTH)
    }

    class TestObj(var idShort: Long? = null, var idLong: Long? = null)

    fun longTest() {
        val expectedShort = 123 // Test widening Int -> Long property
        val expectedLong = 53147483640L
        val result = Karna().parse<TestObj>(""" {"idShort": $expectedShort, "idLong": $expectedLong } """)
        Assert.assertEquals(result?.idShort!!.toLong(), expectedShort.toLong())
        Assert.assertEquals(result?.idLong!!.toLong(), expectedLong)
    }

    class PersonWithDefaults(val age: Int, var name: String = "Foo")
    fun defaultParameters() {
        val result = Karna().parse<PersonWithDefaults>(json {
            obj(
                "age" to 23
            )
        }.toJsonString())!!

        Assert.assertEquals(result.age, 23)
        Assert.assertEquals(result.name, "Foo")
    }

    sealed class Dir(val name: String) {
        class Left(val n: Int): Dir("Left")
    }

    fun sealedClass() {
        val result = Karna().parse<Dir.Left>("""{
            "n": 2
        }"""
        )!!

        Assert.assertEquals(result.n, 2)
    }

    fun serializeMap() {
        val data = mapOf("firstName" to "John")
        val result = Karna().toJsonString(data)
        Assert.assertTrue(result.contains("firstName"))
        Assert.assertTrue(result.contains("John"))
    }

    interface Entity<T> {
        val value: T
    }

    fun generics() {
        class LongEntity(override val value: Long) : Entity<Long>

        val result = Karna().parse<LongEntity>("""{
            "value": 42
        }""")
        assertThat(result?.value).isEqualTo(42)
    }

    fun set() {
        data class A(val data: Set<String>)
        val b = A(setOf("test"))
        val json = Karna().toJsonString(b)
        Karna().parse<A>(json)
    }
}
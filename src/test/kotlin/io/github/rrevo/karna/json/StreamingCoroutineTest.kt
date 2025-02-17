package io.github.rrevo.karna.json

import kotlinx.coroutines.runBlocking
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.Reader
import java.io.StringReader
import java.math.BigInteger

/*
 * Cloned tests from @StreamingTest with coroutines.
 */
@Test
class StreamingCoroutineTest {
//    data class Person2(var name: String? = null, var age: Int? = null, var flag: Boolean? = false,
//            var array: List<Int> = emptyList())
//    fun streaming1() {
//        val jr = JsonCoroutineReader(StringReader(array))
//    }

    fun streamingObject() {
        val objectString = """{
             "name": "Joe", "age": 23, "height": 1.85, "flag": true, "array": [1, 3],
             "obj1": { "a":1, "b":2 }
        }"""

        runBlocking {
            JsonCoroutineReader(StringReader(objectString)).use { reader ->
                reader.beginObject() {
                    var name: String? = null
                    var age: Int? = null
                    var height: Double? = null
                    var flag: Boolean? = null
                    var array: List<Any> = arrayListOf<Any>()
                    var obj1: JsonObject? = null
                    val expectedObj1 = JsonObject().apply {
                        this["a"] = 1
                        this["b"] = 2
                    }
                    while (reader.hasNext()) {
                        val readName = reader.nextName()
                        when (readName) {
                            "name" -> name = reader.nextString()
                            "age" -> age = reader.nextInt()
                            "height" -> height = reader.nextDouble()
                            "flag" -> flag = reader.nextBoolean()
                            "array" -> array = reader.nextArray()
                            "obj1" -> obj1 = reader.nextObject()
                            else -> Assert.fail("Expected either \"name\" or \"age\" but got $name")
                        }
                    }
                    Assert.assertEquals(name, "Joe")
                    Assert.assertEquals(age!!.toInt(), 23)
                    Assert.assertEquals(height!!.toDouble(), 1.85)
                    Assert.assertTrue(flag!!)
                    Assert.assertEquals(array, listOf(1, 3))
                    Assert.assertEquals(obj1, expectedObj1)
                }
            }
        }
    }

    data class Person1(val name: String, val age: Int)

    val array = """[
            { "name": "Joe", "age": 23 },
            { "name": "Jill", "age": 35 }
        ]"""

    fun streamingArray() {
        runBlocking {
            val karna = Karna()
            JsonCoroutineReader(StringReader(array)).use { reader ->
                val result = arrayListOf<Person1>()
                reader.beginArray {
                    while (reader.hasNext()) {
                        val person = karna.parse<Person1>(reader)
                        result.add(person!!)
                    }
                    Assert.assertEquals(result, listOf(Person1("Joe", 23), Person1("Jill", 35)))
                }
            }
        }
    }

    @Test(enabled = false)
    fun streamingArrayAtScale() {
        runBlocking {
            val karna = Karna()
            val MAX = 10_000_000
            val PRINT_FACTOR = 10_000
            val lazyReader = object : Reader() {
                var started = false
                var done = false
                var count = 0
                val objectData = """{ "name": "Joe", "age": 23 },""".toCharArray()
                val objectDataSize = objectData.size

                override fun read(cbuf: CharArray, off: Int, len: Int): Int {
                    if (len == 0) {
                        return 0
                    }
                    if (done) {
                        return -1
                    }
                    if (!started) {
                        started = true
                        cbuf[off] = '['
                        return 1
                    }
                    var currentOff = off
                    var remainingLen = len
                    while (count <= MAX && objectDataSize < remainingLen) {
                        // NOTE Assume that a single object is smaller than the buffer size
                        System.arraycopy(objectData, 0, cbuf, currentOff, objectDataSize)
                        remainingLen -= objectDataSize
                        currentOff += objectDataSize
                        count++

                        if (count == MAX) {
                            // Replace the last comma with a closing bracket
                            cbuf[currentOff - 1] = ']'
                        }

                    }
                    if (count == MAX) {
                        done = true
                    }
                    return len - remainingLen
                }

                override fun close() {
                }
            }

            JsonCoroutineReader(lazyReader).use { reader ->
                var counter = 0
                val result = arrayListOf<Person1>()
                reader.beginArray {
                    while (reader.hasNext()) {
                        val person = karna.parse<Person1>(reader)
//                        result.add(person!!)
                        counter++
                        if (counter % PRINT_FACTOR == 0) {
                            println("Remaining ${(MAX - counter) / PRINT_FACTOR} of ${MAX / PRINT_FACTOR} objects (* $PRINT_FACTOR)")
                        }
                    }
                }
                Assert.assertEquals(counter, MAX)
                println("results size when leaking ${result.size}")
            }
        }
    }

    data class Address(val street: String)
    data class Person2(val name: String, val address: Address)

    fun nestedObjects() {
        runBlocking {
            val objectString = """[
            { "name": "Joe", "address": { "street": "Karna Road" }}
        ]""".trimIndent()

            val karna = Karna()
            JsonCoroutineReader(StringReader(objectString)).use { reader ->
                val result = arrayListOf<Person2>()
                reader.beginArray {
                    while (reader.hasNext()) {
                        val person = karna.parse<Person2>(reader)
                        result.add(person!!)
                    }
                }
                Assert.assertEquals(result.get(0), Person2("Joe", Address("Karna Road")))
            }
        }
    }

    val arrayInObject = """{ "array": [
            { "name": "Joe", "age": 23 },
            { "name": "Jill", "age": 35 }
        ] }"""

    fun streamingArrayInObject() {
        runBlocking {
            val karna = Karna()
            JsonCoroutineReader(StringReader(arrayInObject)).use { reader ->
                val result = arrayListOf<Person1>()
                reader.beginObject {
                    val name = reader.nextName()
                    Assert.assertEquals(name, "array")
                    reader.beginArray {
                        while (reader.hasNext()) {
                            val person = karna.parse<Person1>(reader)
                            result.add(person!!)
                        }
                        Assert.assertEquals(result, listOf(Person1("Joe", 23), Person1("Jill", 35)))
                    }
                }
            }
        }
    }

    fun testNextString() {
        runBlocking {
            // String read normally
            JsonCoroutineReader(StringReader("[\"text\"]")).use { reader ->
                val actual = reader.beginArray { reader.nextString() }
                Assert.assertEquals(actual, "text")
            }
        }
    }

    fun testNextObject() {
        runBlocking {
            JsonCoroutineReader(
                StringReader(
                    """
            { "key" : "text" }
            """.trimIndent()
                )
            ).use { reader ->
                val obj = reader.nextObject()
                Assert.assertEquals(obj["key"], "text")
            }
        }
    }

    fun testNextObjectParsingString() {
        var key: String? = null
        var value: String? = null

        runBlocking {
            JsonCoroutineReader(
                StringReader(
                    """
              { "key" : "text" }
            """.trimIndent()
                )
            ).use { reader ->
                reader.beginObject {
                    key = reader.nextName()
                    value = reader.nextString()
                }
            }
        }
        Assert.assertEquals(key, "key")
        Assert.assertEquals(value, "text")
    }

    fun testNextObjectParsingStringWithNull() {
        var key: String? = null
        var value: String? = null

        runBlocking {
            JsonCoroutineReader(
                StringReader(
                    """
              { "key" : null }
            """.trimIndent()
                )
            ).use { reader ->
                reader.beginObject {
                    key = reader.nextName()
                    value = reader.nextStringOrNull()
                }
            }
        }
        Assert.assertEquals(key, "key")
        Assert.assertEquals(value, null)
    }

    @DataProvider(name = "invalid-strings")
    fun createinvalidStringData() = arrayOf(
        arrayOf("[null]"), // null
        arrayOf("[true]"), // Boolean
        arrayOf("[123]"), // Int
        arrayOf("[9223372036854775807]"), // Long
        arrayOf("[0.123]") // Double
    )

    @Test(dataProvider = "invalid-strings")
    fun testNextStringInvalidInput(nonStringValue: String) {
        runBlocking {
            assertParsingExceptionFromArray(nonStringValue) { reader ->
                reader.nextString()
            }
        }
    }

    fun testNextInt() {
        runBlocking {
            // Int read normally
            JsonCoroutineReader(StringReader("[0]")).use { reader ->
                val actual = reader.beginArray { reader.nextInt() }
                Assert.assertEquals(actual, 0)
            }
        }
    }

    @DataProvider(name = "invalid-ints")
    fun createinvalidIntData() = arrayOf(
        arrayOf("[null]"), // null
        arrayOf("[true]"), // Boolean
        arrayOf("[\"123\"]"), // String
        arrayOf("[9223372036854775807]"), // Long
        arrayOf("[0.123]") // Double
    )

    @Test(dataProvider = "invalid-ints")
    fun testNextIntInvalidInput(nonIntValue: String) {
        runBlocking {
            assertParsingExceptionFromArray(nonIntValue) { reader ->
                reader.nextInt()
            }
        }
    }

    fun testNextLong() {
        runBlocking {
            // Integer values should be auto-converted
            JsonCoroutineReader(StringReader("[0]")).use { reader ->
                val actual = reader.beginArray { reader.nextLong() }
                Assert.assertEquals(actual, 0L)
            }

            // Long read normally
            JsonCoroutineReader(StringReader("[9223372036854775807]")).use { reader ->
                val actual = reader.beginArray { reader.nextLong() }
                Assert.assertEquals(actual, Long.MAX_VALUE)
            }
        }
    }

    @DataProvider(name = "invalid-longs")
    fun createinvalidLongData() = arrayOf(
        arrayOf("[null]"), // null
        arrayOf("[true]"), // Boolean
        arrayOf("[\"123\"]"), // String
        arrayOf("[0.123]") // Double
    )

    @Test(dataProvider = "invalid-longs")
    fun testNextLongInvalidInput(nonLongValue: String) {
        runBlocking {
            assertParsingExceptionFromArray(nonLongValue) { reader ->
                reader.nextLong()
            }
        }
    }

    fun testNextBigInteger() {
        runBlocking {
            // Integer values should be auto-converted
            JsonCoroutineReader(StringReader("[0]")).use { reader ->
                val actual = reader.beginArray { reader.nextBigInteger() }
                Assert.assertEquals(actual, BigInteger.valueOf(0))
            }

            // Long values should be auto-converted
            JsonCoroutineReader(StringReader("[9223372036854775807]")).use { reader ->
                val actual = reader.beginArray { reader.nextBigInteger() }
                Assert.assertEquals(actual, BigInteger.valueOf(Long.MAX_VALUE))
            }

            // Long read normally
            JsonCoroutineReader(StringReader("[9223372036854775808]")).use { reader ->
                val actual = reader.beginArray { reader.nextBigInteger() }
                Assert.assertEquals(actual, BigInteger.valueOf(Long.MAX_VALUE) + BigInteger.valueOf(1))
            }
        }
    }

    @DataProvider(name = "invalid-biginteger")
    fun createinvalidBigIntegerData() = arrayOf(
        arrayOf("[null]"), // null
        arrayOf("[true]"), // Boolean
        arrayOf("[\"123\"]"), // String
        arrayOf("[0.123]") // Double
    )

    @Test(dataProvider = "invalid-biginteger")
    fun testNextBigIntegerInvalidInput(nonBigIntegerValue: String) {
        runBlocking {
            assertParsingExceptionFromArray(nonBigIntegerValue) { reader ->
                reader.nextBigInteger()
            }
        }
    }

    fun testNextDouble() {
        runBlocking {
            // Integer values should be auto-converted
            JsonCoroutineReader(StringReader("[0]")).use { reader ->
                val actual = reader.beginArray { reader.nextDouble() }
                Assert.assertEquals(actual, 0.0)
            }

            // Native doubles
            JsonCoroutineReader(StringReader("[0.123]")).use { reader ->
                val actual = reader.beginArray { reader.nextDouble() }
                Assert.assertEquals(actual, 0.123)
            }
        }
    }

    @DataProvider(name = "invalid-doubles")
    fun createinvalidDoubleData() = arrayOf(
        arrayOf("[null]"), // null
        arrayOf("[true]"), // Boolean
        arrayOf("[\"123\"]"), // String
        arrayOf("[\"NAN\"]"), // NAN is not really specified
        arrayOf("[9223372036854775807]") // Long
    )

    @Test(dataProvider = "invalid-doubles")
    fun testNextDoubleInvalidInput(nonDoubleValue: String) {
        runBlocking {
            assertParsingExceptionFromArray(nonDoubleValue) { reader ->
                reader.nextDouble()
            }
        }
    }

    fun testNextBoolean() {
        runBlocking {
            // true read normally
            JsonCoroutineReader(StringReader("[true]")).use { reader ->
                val actual = reader.beginArray { reader.nextBoolean() }
                Assert.assertEquals(actual, true)
            }

            // false read normally
            JsonCoroutineReader(StringReader("[false]")).use { reader ->
                val actual = reader.beginArray { reader.nextBoolean() }
                Assert.assertEquals(actual, false)
            }
        }
    }

    @DataProvider(name = "invalid-booleans")
    fun createinvalidBooleanData() = arrayOf(
        arrayOf("[null]"), // null
        arrayOf("[\"123\"]"), // String
        arrayOf("[\"true\"]"), // true as a String
        arrayOf("[\"false\"]"), // false as a String
        arrayOf("[123]"), // Int
        arrayOf("[9223372036854775807]"), // Long
        arrayOf("[0.123]") // Double
    )

    @Test(dataProvider = "invalid-booleans")
    fun testNextBooleanInvalidInput(nonBooleanValue: String) {
        runBlocking {
            assertParsingExceptionFromArray(nonBooleanValue) { reader ->
                reader.nextBoolean()
            }
        }
    }

    private suspend fun assertParsingExceptionFromArray(
        json: String,
        nextValue: suspend (JsonCoroutineReader) -> Unit
    ) {
        JsonCoroutineReader(StringReader(json)).use { reader ->
            var exceptionCount = 0
            reader.beginArray {
                try {
                    nextValue(reader)
                } catch (e: Exception) {
                    Assert.assertTrue(e is JsonParsingException)
                    exceptionCount++
                }
            }
            Assert.assertEquals(exceptionCount, 1)
        }
    }

//    fun streaming1() {
//        val reader = JsonCoroutineReader(StringReader(array))//FileReader("src/test/resources/generated.json"))
//        reader.beginArray()
//        val gson = Gson()
////        gson.fromJson<>()
//        while (reader.hasNext()) {
//            val person = gson.fromJson<Person>(reader, Person::class.java)
//            println("Person:" + person)
//        }
//        reader.endArray()
//    }
}

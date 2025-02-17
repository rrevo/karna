package io.github.rrevo.karna.json

val convertTuple = object: Converter {
    override fun canConvert(cls: Class<*>) = cls == Tuple::class.java

    override fun toJson(value: Any): String {
        return ""
    }

    override fun fromJson(jv: JsonValue): Tuple {
        val x = jv.array
        return Tuple().apply {
            add(TupleValue.IntegerValue(3))
        }
    }
}

data class TopLevel(val tuples: List<List<Tuple>>)

class Tuple : ArrayList<TupleValue>()

sealed class TupleValue {
    class IntegerValue(val value: Int) : TupleValue()
    class StringValue(val value: String): TupleValue()

}

//fun main(args: Array<String>) {
//    val karna = Karna().converter(convertTuple)
//    val result = karna.parse<TopLevel>("""[0, "zero"]""")
//    println(result)
//}


@file:Suppress("UnnecessaryVariable")

package io.github.rrevo.karna.json

import java.lang.reflect.ParameterizedType
import java.lang.reflect.TypeVariable
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.jvm.jvmErasure

/**
 * The default Klaxon converter, which attempts to convert the given value as an enum first and if this fails,
 * using reflection to enumerate the fields of the passed object and assign them values.
 */
class DefaultConverter(private val karna: Karna, private val allPaths: HashMap<String, Any>) : Converter {
    override fun canConvert(cls: Class<*>) = true

    override fun fromJson(jv: JsonValue): Any? {
        val value = jv.inside
        val propertyType = jv.propertyClass
        val classifier = jv.propertyKClass?.classifier
        val result =
            when(value) {
                is Boolean, is String -> value
                is Int -> fromInt(value, propertyType)
                is BigInteger, is BigDecimal -> value
                is Double ->
                    if (classifier == Float::class) fromFloat(value.toFloat(), propertyType)
                    else fromDouble(value, propertyType)
                is Float ->
                    if (classifier == Double::class) fromDouble(value.toDouble(), propertyType)
                    else fromFloat(value, propertyType)
                is Long ->
                    when (classifier) {
                        Double::class -> fromDouble(value.toDouble(), propertyType)
                        Float::class -> fromFloat(value.toFloat(), propertyType)
                        else -> value
                    }
                is Collection<*> -> fromCollection(value, jv)
                is JsonObject -> fromJsonObject(value, jv)
                null -> null
                else -> {
                    throw KarnaException("Don't know how to convert $value")
                }
            }
        return result

    }

    override fun toJson(value: Any): String {
        fun joinToString(list: Collection<*>, open: String, close: String)
            = open + list.joinToString(", ") + close

        val result = when (value) {
            is String, is Enum<*> -> "\"" + Render.escapeString(value.toString()) + "\""
            is Double, is Float, is Int, is Boolean, is Long -> value.toString()
            is Array<*> -> {
                val elements = value.map { karna.toJsonString(it) }
                joinToString(elements, "[", "]")
            }
            is Collection<*> -> {
                val elements = value.map { karna.toJsonString(it) }
                joinToString(elements, "[", "]")
            }
            is Map<*, *> -> {
                val valueList = arrayListOf<String>()
                value.entries.forEach { entry ->
                    val jsonValue =
                        if (entry.value == null) "null"
                        else karna.toJsonString(entry.value as Any)
                    valueList.add("\"${entry.key}\": $jsonValue")
                }
                joinToString(valueList, "{", "}")
            }
            is BigInteger -> value.toString()
            else -> {
                val valueList = arrayListOf<String>()
                val properties = Annotations.findNonIgnoredProperties(value::class, karna.propertyStrategies)
                properties.forEach { prop ->
                    val getValue = prop.getter.call(value)
                    val getAnnotation = Annotations.findJsonAnnotation(value::class, prop.name)

                    // Use instance settings only when no local settings exist
                    if (getValue != null
                        || (getAnnotation?.serializeNull == true) // Local settings have precedence to instance settings
                        || (getAnnotation == null && karna.instanceSettings.serializeNull)
                    ) {
                            val jsonValue = karna.toJsonString(getValue, prop)
                            val fieldName = Annotations.retrieveJsonFieldName(karna, value::class, prop)
                            valueList.add("\"$fieldName\" : $jsonValue")
                        }
                }
                joinToString(valueList, "{", "}")
            }

        }
        return result
    }

    private fun fromInt(value: Int, propertyType: java.lang.reflect.Type?): Any {
        // If the value is an Int and the property is a Long, widen it
        val isLong = java.lang.Long::class.java == propertyType || Long::class.java == propertyType
        val result: Any = when {
            isLong -> value.toLong()
            propertyType == BigDecimal::class.java -> BigDecimal(value)
            else -> value
        }
        return result
    }

    private fun fromDouble(value: Double, propertyType: java.lang.reflect.Type?): Any {
        return if (propertyType == BigDecimal::class.java) {
            BigDecimal(value)
        } else {
            value
        }
    }

    private fun fromFloat(value: Float, propertyType: java.lang.reflect.Type?): Any {
        return if (propertyType == BigDecimal::class.java) {
            BigDecimal(value.toDouble())
        } else {
            value
        }
    }

    private fun fromCollection(value: Collection<*>, jv: JsonValue): Any {
        val kt = jv.propertyKClass
        val jt = jv.propertyClass
        val convertedCollection = value.map {
            // Try to find a converter for the element type of the collection
            if (jt is ParameterizedType) {
                val typeArgument = jt.actualTypeArguments[0]
                val converter =
                    when (typeArgument) {
                        is Class<*> -> karna.findConverterFromClass(typeArgument, null)
                        is ParameterizedType -> {
                            when (val ta = typeArgument.actualTypeArguments[0]) {
                                is Class<*> -> karna.findConverterFromClass(ta, null)
                                is ParameterizedType -> karna.findConverterFromClass(ta.rawType.javaClass, null)
                                else -> throw KarnaException("SHOULD NEVER HAPPEN")
                            }
                        }
                        else -> throw IllegalArgumentException("Should never happen")
                    }
                val kTypeArgument = kt?.arguments!![0].type
                converter.fromJson(JsonValue(it, typeArgument, kTypeArgument, karna))
            } else {
                if (it != null) {
                    val converter = karna.findConverter(it)
                    converter.fromJson(JsonValue(it, jt, kt, karna))
                } else {
                    throw KarnaException("Don't know how to convert null value in array $jv")
                }
            }

        }

        val result =
                when {
                    Annotations.isSet(jt) -> {
                        convertedCollection.toSet()
                    }
                    Annotations.isArray(kt) -> {
                        val componentType = (jt as Class<*>).componentType
                        val array = java.lang.reflect.Array.newInstance(componentType, convertedCollection.size)
                        convertedCollection.indices.forEach { i ->
                            java.lang.reflect.Array.set(array, i, convertedCollection[i])
                        }
                        array
                    }
                    else -> {
                        convertedCollection
                    }
                }
        return result
    }

    private fun fromJsonObject(value: JsonObject, jv: JsonValue): Any {
        val jt = jv.propertyClass
        val result =
            if (jt is ParameterizedType) {
                val isMap = Map::class.java.isAssignableFrom(jt.rawType as Class<*>)
                val isCollection = List::class.java.isAssignableFrom(jt.rawType as Class<*>)
                when {
                    isMap -> {
                        // Map
                        val result = linkedMapOf<String, Any?>()
                        value.entries.forEach { kv ->
                            val key = kv.key
                            kv.value?.let { mv ->
                                val typeValue = jt.actualTypeArguments[1]
                                val converter = karna.findConverterFromClass(
                                        typeValue.javaClass, null)
                                val convertedValue = converter.fromJson(
                                        JsonValue(mv, typeValue, jv.propertyKClass!!.arguments[1].type,
                                                karna))
                                result[key] = convertedValue
                            }
                        }
                        result
                    }
                    isCollection -> {
                        when(val type =jt.actualTypeArguments[0]) {
                            is Class<*> -> {
                                val cls = jt.actualTypeArguments[0] as Class<*>
                                karna.fromJsonObject(value, cls, cls.kotlin)
                            }
                            is ParameterizedType -> {
                                val result2 = JsonObjectConverter(karna, HashMap()).fromJson(value,
                                        jv.propertyKClass!!.jvmErasure)
                                result2

                            }
                            else -> {
                                throw IllegalArgumentException("Couldn't interpret type $type")
                            }
                        }
                    }
                    else -> throw KarnaException("Don't know how to convert the JsonObject with the following keys" +
                            ":\n  $value")
                }
            } else {
                if (jt is Class<*>) {
                    if (jt.isArray) {
                        val typeValue = jt.componentType
                        karna.fromJsonObject(value, typeValue, typeValue.kotlin)
                    } else {
                        JsonObjectConverter(karna, allPaths).fromJson(jv.obj!!, jv.propertyKClass!!.jvmErasure)
                    }
                } else {
                    val typeName: Any? =
                        if (jt is TypeVariable<*>) {
                            jt.genericDeclaration
                        } else {
                            jt
                        }
                    throw IllegalArgumentException("Generic type not supported: $typeName")
                }
            }
        return result
    }

}

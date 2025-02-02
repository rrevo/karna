package io.github.rrevo.karna.json.internal

import io.github.rrevo.karna.json.Converter
import kotlin.reflect.KProperty

interface ConverterFinder {
    fun findConverter(value: Any, prop: KProperty<*>? = null): Converter
}

package io.github.rrevo.karna.json

import kotlin.reflect.KProperty

interface PropertyStrategy {
    /**
     * @return true if this property should be mapped.
     */
    fun accept(property: KProperty<*>): Boolean
}
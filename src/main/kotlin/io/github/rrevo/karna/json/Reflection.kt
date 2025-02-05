package io.github.rrevo.karna.json

import kotlin.reflect.KClass

class Reflection {
    companion object {
        fun isAssignableFromAny(type: Class<*>, vararg kc: KClass<*>)
                = kc.any { type.isAssignableFrom(type) }
    }
}
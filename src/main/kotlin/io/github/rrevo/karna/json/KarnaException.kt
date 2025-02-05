package io.github.rrevo.karna.json

open class KarnaException(s: String) : RuntimeException(s)
class JsonParsingException(s: String) : KarnaException(s)
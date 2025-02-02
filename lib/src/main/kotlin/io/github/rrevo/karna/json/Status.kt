package io.github.rrevo.karna.json

enum class Status {
    INIT,
    IN_FINISHED_VALUE,
    IN_OBJECT,
    IN_ARRAY,
    PASSED_PAIR_KEY,
    PAIR_VALUE,
    IN_ERROR,
    EOF
}

package io.github.rrevo.karna.json

import org.testng.annotations.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

@Test
class InstanceSettingsTest {

    @Suppress("unused")
    class UnannotatedGeolocationCoordinates(
        val latitude: Int,
        val longitude: Int,
        val speed: Int? // nullable field
    )

    @Suppress("unused")
    class NoNullAnnotatedGeolocationCoordinates(
        val latitude: Int,
        val longitude: Int,
        @Json(serializeNull = false) val speed: Int? // nullable field
    )

    @Suppress("unused")
    class NullAnnotatedGeolocationCoordinates(
        val latitude: Int,
        val longitude: Int,
        @Json(serializeNull = true) val speed: Int? // nullable field
    )

    private val unannotatedCoordinates = UnannotatedGeolocationCoordinates(1, 2, null)
    private val noNullCoordinates = NoNullAnnotatedGeolocationCoordinates(1, 2, null)
    private val nullCoordinates = NullAnnotatedGeolocationCoordinates(1, 2, null)

    // Defaults & single-type settings

    @Test
    fun defaultSerialization() {
        val karna = Karna()
        val json = karna.toJsonString(unannotatedCoordinates)
        assertContains(json, "null") // {"latitude" : 1, "longitude" : 2, "speed" : null}
    }

    @Test // no local settings, instance serializeNull = true -> null
    fun instanceSettingsNullSerialization() {
        val karna = Karna(instanceSettings = KarnaSettings(serializeNull = true))
        val json = karna.toJsonString(unannotatedCoordinates)
        assertContains(json, "null") // {"latitude" : 1, "longitude" : 2, "speed" : null}
    }

    @Test // no local settings, instance serializeNull = false -> no null
    fun instanceSettingsNoNullSerialization() {
        val karna = Karna(KarnaSettings(serializeNull = false))
        val json = karna.toJsonString(unannotatedCoordinates)
        assertFalse { json.contains("null") } // {"latitude" : 1, "longitude" : 2}
    }

    @Test // local serializeNull = false, no instance settings -> no null
    fun localSettingsNoNullSerialization() {
        val karna = Karna()
        val json = karna.toJsonString(noNullCoordinates)
        assertFalse { json.contains("null") } // {"latitude" : 1, "longitude" : 2}
    }

    @Test // local serializeNull = true, no instance settings -> null
    fun localSettingsNullSerialization() {
        val karna = Karna()
        val json = karna.toJsonString(nullCoordinates)
        assertContains(json, "null") // {"latitude" : 1, "longitude" : 2, "speed" : null}
    }

    //
    // Mixed tests

    @Test // local serializeNull = true, instance serializeNull = false -> null
    fun localNullInstanceNoNullSerialization() {
        val karna = Karna(KarnaSettings(serializeNull = false))
        val json = karna.toJsonString(nullCoordinates)
        assertContains(json, "null") // {"latitude" : 1, "longitude" : 2, "speed" : null}
    }

    @Test // local serializeNull = false, instance serializeNull = true -> no null
    fun localNoNullInstanceNullSerialization() {
        val karna = Karna(KarnaSettings(serializeNull = true))
        val json = karna.toJsonString(noNullCoordinates)
        assertFalse { json.contains("null") } // {"latitude" : 1, "longitude" : 2}
    }
}

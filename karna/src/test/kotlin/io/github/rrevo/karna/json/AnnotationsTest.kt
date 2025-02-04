package io.github.rrevo.karna.json

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test
import kotlin.reflect.full.memberProperties

@Test
class AnnotationsTest {
    sealed class Message(open val text: String,
            @Json(name = "message_type")
            val messageType: KarnaTest2.MessageType) {

        data class MessageReceived(val sender: String,
                override val text: String) : Message(text, KarnaTest2.MessageType.RECEIVED)
    }

    fun test() {
        val prop = Message.MessageReceived::class.memberProperties.find { it.name == "messageType" }
        val json = Annotations.findJsonAnnotation(Message.MessageReceived::class, prop!!.name)
        assertThat(json).isNotNull()
    }
}
package io.github.rrevo.karna.json

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

class KarnaTest2 {

    enum class MessageType {
        RECEIVED, SENDING
    }

    sealed class Message(open val text: String,
            @Json(name = "message_type")
            val messageType: MessageType
    ) {

        data class MessageReceived(val sender: String,
                override val text: String) : Message(text, MessageType.RECEIVED)

        data class MessageSending(val recipient: String,
                override val text: String) : Message(text, MessageType.SENDING)
    }

    @Test
    fun testEncoding() {
        val karna = Karna()
        val received = Message.MessageReceived("Alice", "Hello")
        val receivedString = karna.toJsonString(received)
        assertThat(receivedString).contains("message_type")
    }
}

class KarnaTest3 {

    enum class MessageType {
        RECEIVED, SENDING
    }

    abstract class Message(open val text: String,
            @Json(name = "message_type")
            val messageType: MessageType
    )

    data class MessageReceived(val sender: String,
            override val text: String) : Message(text, MessageType.RECEIVED)

    data class MessageSending(val recipient: String,
            override val text: String) : Message(text, MessageType.SENDING)

    @Test
    fun testEncoding() {
        val karna = Karna()
        val received = MessageReceived("Alice", "Hello")
        val receivedString = karna.toJsonString(received)
        assertThat(receivedString).contains("message_type")
    }
}
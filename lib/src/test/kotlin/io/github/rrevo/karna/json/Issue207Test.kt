package io.github.rrevo.karna.json

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

class Issue207Test {
    private val karna = Karna()
    interface Message {
        val text: String
    }

    data class MessageReceived(val sender: String,
            override val text: String) : Message

    data class MessageToSend(val recipient: String,
            override val text: String) : Message



    data class Root(val id: Long, val message: Message)

    @Test
    fun testReceived() {
        val jsonString = """
        {
            "sender":"Alice",
            "text":"Hello"
        }
    """.trimIndent()
        val root = karna.parse<MessageReceived>(jsonString)
        assertThat(root!!.sender).isEqualToIgnoringCase("Alice")
        assertThat(root.text).isEqualTo("Hello")
    }

    @Test
    fun testSending() {
        val jsonString = """
        {
            "recipient":"Bob",
            "text":"Hello"
        }
    """.trimIndent()
        val root = karna.parse<MessageToSend>(jsonString)
        assertThat(root!!.recipient).isEqualToIgnoringCase("Bob")
        assertThat(root.text).isEqualTo("Hello")
    }
}
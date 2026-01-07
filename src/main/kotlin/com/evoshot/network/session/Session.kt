package com.evoshot.network.session

import io.netty.channel.Channel
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import java.util.UUID

class Session(
    val id: String = UUID.randomUUID().toString(),
    private val channel: Channel,
) {
    val isActive: Boolean
        get() = channel.isActive

    fun send(text: String) {
        if (isActive) {
            channel.writeAndFlush(TextWebSocketFrame(text))
        }
    }

    fun close() {
        channel.close()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Session) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

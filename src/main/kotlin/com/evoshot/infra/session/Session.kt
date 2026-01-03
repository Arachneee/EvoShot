package com.evoshot.infra.session

import com.evoshot.core.handler.message.GameMessage
import com.evoshot.core.player.Player
import com.evoshot.infra.codec.MessageCodec
import io.netty.channel.Channel
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import java.util.UUID

class Session(
    override val playerId: String = UUID.randomUUID().toString(),
    val channel: Channel,
    private val codec: MessageCodec,
) : Player {
    private var _playerName: String? = null

    override val playerName: String?
        get() = _playerName

    val isConnected: Boolean
        get() = channel.isActive

    override fun setPlayerName(name: String) {
        this._playerName = name
    }

    override fun send(message: GameMessage) {
        if (isConnected) {
            val json = codec.encodeToString(message)
            channel.writeAndFlush(TextWebSocketFrame(json))
        }
    }

    fun close() = channel.close()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Session) return false
        return playerId == other.playerId
    }

    override fun hashCode(): Int = playerId.hashCode()
}

package com.evoshot.infra.session

import com.evoshot.infra.codec.MessageCodec
import io.netty.channel.Channel
import java.util.concurrent.ConcurrentHashMap

class SessionManager(private val codec: MessageCodec) {

    private val sessionsByChannel = ConcurrentHashMap<Channel, Session>()
    private val sessionsByPlayerId = ConcurrentHashMap<String, Session>()

    val sessionCount: Int
        get() = sessionsByChannel.size

    fun createSession(channel: Channel): Session {
        val session = Session(channel = channel, codec = codec)
        sessionsByChannel[channel] = session
        sessionsByPlayerId[session.playerId] = session
        return session
    }

    fun getSession(channel: Channel): Session? = sessionsByChannel[channel]

    fun getSessionByPlayerId(playerId: String): Session? = sessionsByPlayerId[playerId]

    fun removeSession(channel: Channel): Session? {
        val session = sessionsByChannel.remove(channel)
        session?.let { sessionsByPlayerId.remove(it.playerId) }
        return session
    }

    fun getAllSessions(): Collection<Session> = sessionsByChannel.values
}


package com.evoshot.network.session

import io.netty.channel.Channel
import java.util.concurrent.ConcurrentHashMap

class SessionManager {
    private val sessionsByChannel = ConcurrentHashMap<Channel, Session>()
    private val sessionsById = ConcurrentHashMap<String, Session>()

    val sessionCount: Int
        get() = sessionsByChannel.size

    fun createSession(channel: Channel): Session {
        val session = Session(channel = channel)
        sessionsByChannel[channel] = session
        sessionsById[session.id] = session
        return session
    }

    fun getSession(channel: Channel): Session? = sessionsByChannel[channel]

    fun getSessionById(sessionId: String): Session? = sessionsById[sessionId]

    fun removeSession(channel: Channel): Session? {
        val session = sessionsByChannel.remove(channel)
        session?.let { sessionsById.remove(it.id) }
        return session
    }

    fun getAllSessions(): Collection<Session> = sessionsByChannel.values
}

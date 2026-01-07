package com.evoshot.network.session

import com.evoshot.network.handler.MessageBroadcaster

class SessionMessageBroadcaster(
    private val sessionManager: SessionManager,
) : MessageBroadcaster {
    override fun broadcast(message: String) {
        sessionManager.getAllSessions().forEach { it.send(message) }
    }

    override fun broadcastExcept(
        message: String,
        excludeSessionId: String,
    ) {
        sessionManager
            .getAllSessions()
            .filter { it.id != excludeSessionId }
            .forEach { it.send(message) }
    }

    override fun sendTo(
        sessionId: String,
        message: String,
    ) {
        sessionManager.getSessionById(sessionId)?.send(message)
    }

    override fun close(sessionId: String) {
        sessionManager.getSessionById(sessionId)?.close()
    }
}

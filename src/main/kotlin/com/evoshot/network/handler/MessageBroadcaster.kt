package com.evoshot.network.handler

interface MessageBroadcaster {
    fun broadcast(message: String)

    fun broadcastExcept(
        message: String,
        excludeSessionId: String,
    )

    fun sendTo(
        sessionId: String,
        message: String,
    )

    fun close(sessionId: String)

    fun forEachSession(action: (String) -> Unit)
}

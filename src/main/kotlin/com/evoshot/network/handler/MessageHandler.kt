package com.evoshot.network.handler

interface MessageHandler {
    fun onConnect(sessionId: String)

    fun onMessage(
        sessionId: String,
        text: String,
    )

    fun onDisconnect(sessionId: String)
}

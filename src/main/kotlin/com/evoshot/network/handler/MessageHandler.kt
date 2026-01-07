package com.evoshot.network.handler

import com.evoshot.network.session.Session

interface MessageHandler {
    fun onConnect(session: Session)
    fun onMessage(session: Session, text: String)
    fun onDisconnect(session: Session)
}


package com.evoshot.core.player

import com.evoshot.core.handler.message.GameMessage

interface Player {
    val playerId: String
    val playerName: String?

    fun setPlayerName(name: String)

    fun send(message: GameMessage)

    fun disconnect()
}

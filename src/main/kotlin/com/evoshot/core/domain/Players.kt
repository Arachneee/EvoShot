package com.evoshot.core.domain

import java.util.concurrent.ConcurrentHashMap

class Players(private val maxPlayers: Int) {
    private val playersById = ConcurrentHashMap<String, Player>()
    private val playersBySessionId = ConcurrentHashMap<String, Player>()

    val count: Int
        get() = playersById.size

    val isFull: Boolean
        get() = count >= maxPlayers

    fun add(player: Player) {
        playersById[player.id] = player
        playersBySessionId[player.sessionId] = player
    }

    fun remove(playerId: String): Player? {
        val player = playersById.remove(playerId)
        player?.let { playersBySessionId.remove(it.sessionId) }
        return player
    }

    fun findById(playerId: String): Player? = playersById[playerId]

    fun findBySessionId(sessionId: String): Player? = playersBySessionId[sessionId]

    fun findAll(): List<Player> = playersById.values.toList()

    fun findAllAlive(): List<Player> = playersById.values.filter { it.isAlive }

    fun findAllDead(): List<Player> = playersById.values.filter { !it.isAlive }
}


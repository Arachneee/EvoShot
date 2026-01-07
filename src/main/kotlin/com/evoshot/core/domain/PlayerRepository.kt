package com.evoshot.core.domain

interface PlayerRepository {
    fun add(player: Player)

    fun remove(playerId: String): Player?

    fun findById(playerId: String): Player?

    fun findBySessionId(sessionId: String): Player?

    fun findAllAlive(): List<Player>

    fun findAllDead(): List<Player>

    fun getAll(): List<Player>

    fun count(): Int

    fun isFull(): Boolean
}

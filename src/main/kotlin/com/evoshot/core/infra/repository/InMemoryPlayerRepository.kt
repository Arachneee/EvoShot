package com.evoshot.core.infra.repository

import com.evoshot.core.domain.Player
import com.evoshot.core.domain.PlayerRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryPlayerRepository(
    private val maxPlayers: Int = 2000,
) : PlayerRepository {
    private val players = ConcurrentHashMap<String, Player>()

    override fun add(player: Player) {
        players[player.id] = player
    }

    override fun remove(playerId: String): Player? = players.remove(playerId)

    override fun findById(playerId: String): Player? = players[playerId]

    override fun findBySessionId(sessionId: String): Player? = players.values.find { it.sessionId == sessionId }

    override fun findAllAlive(): List<Player> = players.values.filter { it.isAlive }

    override fun findAllDead(): List<Player> = players.values.filter { !it.isAlive }

    override fun getAll(): List<Player> = players.values.toList()

    override fun count(): Int = players.size

    override fun isFull(): Boolean = count() >= maxPlayers
}

package com.evoshot.core.domain

import com.evoshot.core.domain.Bullet
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class Room(
    val id: String,
    val width: Float = 1600f,
    val height: Float = 900f,
    private val maxPlayers: Int = 2000,
) {
    private val players = ConcurrentHashMap<String, Player>()
    private val bullets = ConcurrentHashMap<String, Bullet>()

    val playerCount: Int
        get() = players.size

    val isFull: Boolean
        get() = playerCount >= maxPlayers

    fun join(
        sessionId: String,
        playerName: String,
    ): Player? {
        if (isFull) return null

        val player = Player.create(sessionId = sessionId, playerName = playerName)
        players[player.id] = player

        return player
    }

    fun leave(sessionId: String): String? {
        val player = players.values.find { it.sessionId == sessionId } ?: return null
        val playerId = player.id
        players.remove(playerId)

        return playerId
    }

    fun play() {
        bullets.replaceAll { _, bullet -> bullet.move() }
        removeOutOfBoundsBullets()
        checkPlayerHits()
    }

    private fun removeOutOfBoundsBullets() {
        bullets.entries.removeIf { (_, bullet) ->
            bullet.x !in 0.0..width.toDouble() || bullet.y !in 0.0..height.toDouble()
        }
    }

    private fun checkPlayerHits() {
        players.values.filter { it.isAlive }.forEach { player ->
            player.checkHit(bullets.values.toList())?.let { bullet ->
                bullets.remove(bullet.id)
            }
        }
    }

    fun handlePlayerInput(
        sessionId: String,
        mouseX: Float,
        mouseY: Float,
        shoot: Boolean,
    ) {
        val playerId = getPlayerIdBySessionId(sessionId) ?: return
        val player = players[playerId] ?: return

        player.move(tx = mouseX, ty = mouseY)

        if (shoot) {
            val bullet =
                Bullet.Companion.create(
                    id = UUID.randomUUID().toString(),
                    x = player.x,
                    y = player.y,
                    tx = mouseX,
                    ty = mouseY,
                )

            bullets[bullet.id] = bullet
        }
    }

    fun getPlayerIdBySessionId(sessionId: String): String? = players.values.find { it.sessionId == sessionId }?.id

    fun getSessionIdByPlayerId(playerId: String): String? = players[playerId]?.sessionId

    fun getAlivePlayerStates(): List<PlayerState> = players.values.filter { it.isAlive }.map { it.toState() }

    fun getAllBullets(): List<Bullet> = bullets.values.toList()

    fun getAndRemoveDeadPlayers(): List<String> {
        val deadPlayerIds =
            players.values
                .filter { !it.isAlive }
                .map { it.id }

        deadPlayerIds.forEach { playerId ->
            players.remove(playerId)
        }

        return deadPlayerIds
    }
}

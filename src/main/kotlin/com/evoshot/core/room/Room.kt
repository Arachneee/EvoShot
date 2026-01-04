package com.evoshot.core.room

import com.evoshot.core.bullet.Bullet
import com.evoshot.core.handler.message.GameMessage
import com.evoshot.core.handler.message.PlayerDeadMessage
import com.evoshot.core.handler.message.PlayerLeaveMessage
import com.evoshot.core.player.Player
import com.evoshot.core.player.PlayerState
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.toList

class Room(
    val id: String,
    private val maxPlayers: Int = 2000,
    val width: Float = 1600f,
    val height: Float = 900f,
) {
    private val players = ConcurrentHashMap<String, Player>()
    private val playerStates = ConcurrentHashMap<String, PlayerState>()
    private val bullets = ConcurrentHashMap<String, Bullet>()

    val playerCount: Int
        get() = players.size

    val isFull: Boolean
        get() = playerCount >= maxPlayers

    fun join(player: Player): Boolean {
        if (isFull) return false

        val playerName = player.playerName ?: return false
        players[player.playerId] = player

        val initialState =
            PlayerState(
                id = player.playerId,
                name = playerName,
                x = (100..(width.toInt() - 100)).random().toFloat(),
                y = (100..(height.toInt() - 100)).random().toFloat(),
            )
        playerStates[player.playerId] = initialState

        return true
    }

    fun leave(player: Player): Boolean {
        val removed = players.remove(player.playerId) != null
        playerStates.remove(player.playerId)

        if (removed) {
            broadcast(PlayerLeaveMessage(playerId = player.playerId))
        }

        return removed
    }

    fun getPlayerState(playerId: String): PlayerState? = playerStates[playerId]

    fun updatePlayerState(
        playerId: String,
        updater: (PlayerState) -> PlayerState,
    ) {
        playerStates.computeIfPresent(playerId) { _, state ->
            val updated = updater(state)
            clampToBounds(updated)
        }
    }

    private fun clampToBounds(state: PlayerState): PlayerState {
        val clampedX = state.x.coerceIn(0f, width)
        val clampedY = state.y.coerceIn(0f, height)
        return if (clampedX != state.x || clampedY != state.y) {
            state.copy(x = clampedX, y = clampedY)
        } else {
            state
        }
    }

    fun getAlivePlayerStates(): List<PlayerState> = playerStates.values.toList().filter { it.isAlive }

    fun addBullet(bullet: Bullet) {
        bullets[bullet.id] = bullet
    }

    fun removeBullet(bulletId: String) {
        bullets.remove(bulletId)
    }

    fun getAllBullets(): List<Bullet> = bullets.values.toList()

    fun play() {
        bullets.replaceAll { _, bullet -> bullet.move() }
        removeOutOfBoundsBullets()
        checkPlayerHits()
        processDeadPlayers()
    }

    private fun checkPlayerHits() {
        getAlivePlayerStates().forEach { playerState ->
            playerState.heat(bullets.values.toList())?.let { bullet ->
                removeBullet(bullet.id)
            }
        }
    }

    private fun processDeadPlayers() {
        val deadPlayerIds = playerStates.values
            .filter { !it.isAlive }
            .map { it.id }

        deadPlayerIds.forEach { playerId ->
            val player = players[playerId] ?: return@forEach

            player.send(PlayerDeadMessage(playerId = playerId, killedByBulletId = ""))
            player.disconnect()

            players.remove(playerId)
            playerStates.remove(playerId)

            broadcast(PlayerLeaveMessage(playerId = playerId))
        }
    }

    private fun removeOutOfBoundsBullets() {
        bullets.entries.removeIf { (_, bullet) ->
            bullet.x !in 0.0..width.toDouble() || bullet.y !in 0.0..height.toDouble()
        }
    }

    fun broadcast(message: GameMessage) {
        players.values.forEach { it.send(message) }
    }

    fun broadcastExcept(
        message: GameMessage,
        excludePlayerId: String,
    ) {
        players.forEach { (playerId, player) ->
            if (playerId != excludePlayerId) {
                player.send(message)
            }
        }
    }
}

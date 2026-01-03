package com.evoshot.core.room

import com.evoshot.core.handler.message.GameMessage
import com.evoshot.core.handler.message.PlayerLeaveMessage
import com.evoshot.core.player.Player
import com.evoshot.core.player.PlayerState
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.toList

class Room(
    val id: String,
    private val maxPlayers: Int = 2000,
) {
    private val players = ConcurrentHashMap<String, Player>()
    private val playerStates = ConcurrentHashMap<String, PlayerState>()

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
                x = (100..700).random().toFloat(),
                y = (100..500).random().toFloat(),
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
        playerStates.computeIfPresent(playerId) { _, state -> updater(state) }
    }

    fun getAllPlayerStates(): List<PlayerState> = playerStates.values.toList()

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

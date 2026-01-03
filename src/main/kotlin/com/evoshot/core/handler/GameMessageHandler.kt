package com.evoshot.core.handler

import com.evoshot.core.handler.message.ConnectMessage
import com.evoshot.core.handler.message.ConnectedMessage
import com.evoshot.core.handler.message.ErrorMessage
import com.evoshot.core.handler.message.GameMessage
import com.evoshot.core.handler.message.PingMessage
import com.evoshot.core.handler.message.PlayerInputMessage
import com.evoshot.core.handler.message.PlayerJoinMessage
import com.evoshot.core.handler.message.PongMessage
import com.evoshot.core.player.Player
import com.evoshot.core.room.Room

class GameMessageHandler(
    private val room: Room,
) {
    fun handle(
        player: Player,
        message: GameMessage,
    ) {
        when (message) {
            is PingMessage -> {
                handlePing(player, message)
            }

            is ConnectMessage -> {
                handleConnect(player, message)
            }

            is PlayerInputMessage -> {
                handlePlayerInput(player, message)
            }

            else -> {}
        }
    }

    private fun handlePing(
        player: Player,
        message: PingMessage,
    ) {
        player.send(PongMessage(timestamp = message.timestamp))
    }

    private fun handleConnect(
        player: Player,
        message: ConnectMessage,
    ) {
        player.setPlayerName(message.playerName)

        if (room.join(player)) {
            val playerState = room.getPlayerState(player.playerId)!!

            player.send(
                ConnectedMessage(
                    playerId = player.playerId,
                    players = room.getAllPlayerStates(),
                ),
            )

            room.broadcastExcept(
                PlayerJoinMessage(player = playerState),
                excludePlayerId = player.playerId,
            )
        } else {
            player.send(ErrorMessage(code = "ROOM_FULL", message = "Room is full"))
        }
    }

    private fun handlePlayerInput(
        player: Player,
        message: PlayerInputMessage,
    ) {
        room.updatePlayerState(player.playerId) { state ->
            state.move(tx = message.mouseX, ty = message.mouseY)
        }
    }
}

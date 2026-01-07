package com.evoshot.core.controller

import com.evoshot.core.controller.message.ConnectMessage
import com.evoshot.core.controller.message.ConnectedMessage
import com.evoshot.core.controller.message.ErrorMessage
import com.evoshot.core.controller.message.GameMessage
import com.evoshot.core.controller.message.GameStateMessage
import com.evoshot.core.controller.message.MessageCodec
import com.evoshot.core.controller.message.PingMessage
import com.evoshot.core.controller.message.PlayerDeadMessage
import com.evoshot.core.controller.message.PlayerInputMessage
import com.evoshot.core.controller.message.PlayerJoinMessage
import com.evoshot.core.controller.message.PlayerLeaveMessage
import com.evoshot.core.controller.message.PongMessage
import com.evoshot.core.domain.Room
import com.evoshot.core.engine.GameTickHandler
import com.evoshot.network.handler.MessageBroadcaster
import com.evoshot.network.handler.MessageHandler
import org.slf4j.LoggerFactory

class GameController(
    private val room: Room,
    private val codec: MessageCodec,
    private val broadcaster: MessageBroadcaster,
) : MessageHandler,
    GameTickHandler {
    private val logger = LoggerFactory.getLogger(GameController::class.java)

    override fun onConnect(sessionId: String) {
    }

    override fun onMessage(
        sessionId: String,
        text: String,
    ) {
        try {
            val message = codec.decodeFromString(text)
            handleGameMessage(sessionId, message)
        } catch (e: Exception) {
            logger.error("Failed to decode message: ${e.message}")
        }
    }

    override fun onDisconnect(sessionId: String) {
        val playerId = room.leave(sessionId)
        if (playerId != null) {
            broadcast(PlayerLeaveMessage(playerId = playerId))
        }
    }

    override fun onTick(tick: Long) {
        room.tick()
        processDeadPlayers()
        broadcastGameState(tick)
    }

    private fun handleGameMessage(
        sessionId: String,
        message: GameMessage,
    ) {
        when (message) {
            is PingMessage -> {
                handlePing(sessionId, message)
            }

            is ConnectMessage -> {
                handleConnect(sessionId, message)
            }

            is PlayerInputMessage -> {
                handlePlayerInput(sessionId, message)
            }

            else -> {}
        }
    }

    private fun handlePing(
        sessionId: String,
        message: PingMessage,
    ) {
        broadcaster.sendTo(sessionId, codec.encodeToString(PongMessage(timestamp = message.timestamp)))
    }

    private fun handleConnect(
        sessionId: String,
        message: ConnectMessage,
    ) {
        val player = room.join(sessionId = sessionId, playerName = message.playerName)

        if (player != null) {
            broadcaster.sendTo(
                sessionId,
                codec.encodeToString(
                    ConnectedMessage(
                        playerId = player.id,
                        players = room.getAlivePlayerStates(),
                    ),
                ),
            )

            broadcaster.broadcastExcept(
                codec.encodeToString(PlayerJoinMessage(player = player.toState())),
                excludeSessionId = sessionId,
            )
        } else {
            broadcaster.sendTo(
                sessionId,
                codec.encodeToString(ErrorMessage(code = "ROOM_FULL", message = "Room is full")),
            )
        }
    }

    private fun handlePlayerInput(
        sessionId: String,
        message: PlayerInputMessage,
    ) {
        room.handlePlayerInput(
            sessionId = sessionId,
            mouseX = message.mouseX,
            mouseY = message.mouseY,
            shoot = message.shoot,
        )
    }

    private fun processDeadPlayers() {
        val deadPlayerIds = room.getAndRemoveDeadPlayers()

        deadPlayerIds.forEach { playerId ->
            val sessionId = room.getSessionIdByPlayerId(playerId)
            if (sessionId != null) {
                broadcaster.sendTo(
                    sessionId,
                    codec.encodeToString(PlayerDeadMessage(playerId = playerId, killedByBulletId = "")),
                )
                broadcaster.close(sessionId)
            }
            broadcast(PlayerLeaveMessage(playerId = playerId))
        }
    }

    private fun broadcastGameState(tick: Long) {
        if (room.playerCount > 0) {
            val gameState =
                GameStateMessage(
                    tick = tick,
                    players = room.getAlivePlayerStates(),
                    bullet = room.getAllBullets(),
                )
            broadcast(gameState)
        }
    }

    private fun broadcast(message: GameMessage) {
        broadcaster.broadcast(codec.encodeToString(message))
    }
}

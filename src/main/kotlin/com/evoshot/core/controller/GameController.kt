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
import com.evoshot.core.domain.RoomManager
import com.evoshot.core.engine.GameTickHandler
import com.evoshot.network.handler.MessageBroadcaster
import com.evoshot.network.handler.MessageHandler
import org.slf4j.LoggerFactory

class GameController(
    private val roomManager: RoomManager,
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
        val roomSessionIds = roomManager.getRoomSessionIds(sessionId)
        val playerId = roomManager.leave(sessionId)
        if (playerId != null) {
            broadcastToSessions(PlayerLeaveMessage(playerId = playerId), roomSessionIds)
        }
    }

    override fun onTick(tick: Long) {
        roomManager.tick()
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
        val player = roomManager.join(sessionId = sessionId, playerName = message.playerName)

        if (player != null) {
            broadcaster.sendTo(
                sessionId,
                codec.encodeToString(
                    ConnectedMessage(
                        playerId = player.id,
                        players = roomManager.getAlivePlayerStates(sessionId),
                    ),
                ),
            )

            val roomSessionIds = roomManager.getRoomSessionIds(sessionId)
            broadcastToSessionsExcept(
                PlayerJoinMessage(player = player.toState()),
                roomSessionIds,
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
        roomManager.handlePlayerInput(
            sessionId = sessionId,
            mouseX = message.mouseX,
            mouseY = message.mouseY,
            shoot = message.shoot,
        )
    }

    private fun processDeadPlayers() {
        val deadPlayerInfos = roomManager.getAndRemoveDeadPlayers()

        deadPlayerInfos.forEach { info ->
            if (info.sessionId != null) {
                broadcaster.sendTo(
                    info.sessionId,
                    codec.encodeToString(PlayerDeadMessage(playerId = info.playerId, killedByBulletId = "")),
                )
                broadcaster.close(info.sessionId)
            }
            broadcastToSessions(PlayerLeaveMessage(playerId = info.playerId), info.roomSessionIds)
        }
    }

    private fun broadcastGameState(tick: Long) {
        roomManager.getAllRoomStates().forEach { roomState ->
            val gameState =
                GameStateMessage(
                    tick = tick,
                    players = roomState.players,
                    bullet = roomState.bullets,
                )
            val encodedMessage = codec.encodeToString(gameState)
            roomState.sessionIds.forEach { sessionId ->
                broadcaster.sendTo(sessionId, encodedMessage)
            }
        }
    }

    private fun broadcastToSessions(
        message: GameMessage,
        sessionIds: List<String>,
    ) {
        val encodedMessage = codec.encodeToString(message)
        sessionIds.forEach { sessionId ->
            broadcaster.sendTo(sessionId, encodedMessage)
        }
    }

    private fun broadcastToSessionsExcept(
        message: GameMessage,
        sessionIds: List<String>,
        excludeSessionId: String,
    ) {
        val encodedMessage = codec.encodeToString(message)
        sessionIds.filter { it != excludeSessionId }.forEach { sessionId ->
            broadcaster.sendTo(sessionId, encodedMessage)
        }
    }
}

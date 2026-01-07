package com.evoshot.core.handler

import com.evoshot.core.domain.Room
import com.evoshot.core.handler.message.ConnectMessage
import com.evoshot.core.handler.message.ConnectedMessage
import com.evoshot.core.handler.message.ErrorMessage
import com.evoshot.core.handler.message.GameMessage
import com.evoshot.core.handler.message.GameStateMessage
import com.evoshot.core.handler.message.MessageCodec
import com.evoshot.core.handler.message.PingMessage
import com.evoshot.core.handler.message.PlayerDeadMessage
import com.evoshot.core.handler.message.PlayerInputMessage
import com.evoshot.core.handler.message.PlayerJoinMessage
import com.evoshot.core.handler.message.PlayerLeaveMessage
import com.evoshot.core.handler.message.PongMessage
import com.evoshot.network.handler.MessageHandler
import com.evoshot.network.session.Session
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class EvoShotMessageHandler(
    private val room: Room,
    private val codec: MessageCodec,
) : MessageHandler {
    private val logger = LoggerFactory.getLogger(EvoShotMessageHandler::class.java)
    private val sessionsBySessionId = ConcurrentHashMap<String, Session>()

    override fun onConnect(session: Session) {
    }

    override fun onMessage(
        session: Session,
        text: String,
    ) {
        try {
            val message = codec.decodeFromString(text)
            handleGameMessage(session, message)
        } catch (e: Exception) {
            logger.error("Failed to decode message: ${e.message}")
        }
    }

    override fun onDisconnect(session: Session) {
        val playerId = room.leave(session.id)
        if (playerId != null) {
            sessionsBySessionId.remove(session.id)
            broadcast(PlayerLeaveMessage(playerId = playerId))
        }
    }

    fun play(tick: Long) {
        room.play()
        processDeadPlayers()
        broadcastGameState(tick)
    }

    private fun handleGameMessage(
        session: Session,
        message: GameMessage,
    ) {
        when (message) {
            is PingMessage -> {
                handlePing(session, message)
            }

            is ConnectMessage -> {
                handleConnect(session, message)
            }

            is PlayerInputMessage -> {
                handlePlayerInput(session, message)
            }

            else -> {}
        }
    }

    private fun handlePing(
        session: Session,
        message: PingMessage,
    ) {
        session.send(codec.encodeToString(PongMessage(timestamp = message.timestamp)))
    }

    private fun handleConnect(
        session: Session,
        message: ConnectMessage,
    ) {
        val player = room.join(sessionId = session.id, playerName = message.playerName)

        if (player != null) {
            sessionsBySessionId[session.id] = session

            session.send(
                codec.encodeToString(
                    ConnectedMessage(
                        playerId = player.id,
                        players = room.getAlivePlayerStates(),
                    ),
                ),
            )

            broadcastExcept(
                PlayerJoinMessage(player = player.toState()),
                excludeSessionId = session.id,
            )
        } else {
            session.send(codec.encodeToString(ErrorMessage(code = "ROOM_FULL", message = "Room is full")))
        }
    }

    private fun handlePlayerInput(
        session: Session,
        message: PlayerInputMessage,
    ) {
        room.handlePlayerInput(
            sessionId = session.id,
            mouseX = message.mouseX,
            mouseY = message.mouseY,
            shoot = message.shoot,
        )
    }

    private fun processDeadPlayers() {
        val deadPlayerIds = room.getAndRemoveDeadPlayers()

        deadPlayerIds.forEach { playerId ->
            sendToPlayer(playerId, PlayerDeadMessage(playerId = playerId, killedByBulletId = ""))
            closePlayer(playerId)
            broadcast(PlayerLeaveMessage(playerId = playerId))
        }
    }

    private fun sendToPlayer(
        playerId: String,
        message: GameMessage,
    ) {
        val sessionId = room.getSessionIdByPlayerId(playerId) ?: return
        sessionsBySessionId[sessionId]?.send(codec.encodeToString(message))
    }

    private fun closePlayer(playerId: String) {
        val sessionId = room.getSessionIdByPlayerId(playerId) ?: return
        sessionsBySessionId[sessionId]?.close()
        sessionsBySessionId.remove(sessionId)
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
        val json = codec.encodeToString(message)
        sessionsBySessionId.values.forEach { it.send(json) }
    }

    private fun broadcastExcept(
        message: GameMessage,
        excludeSessionId: String,
    ) {
        val json = codec.encodeToString(message)
        sessionsBySessionId.forEach { (sessionId, session) ->
            if (sessionId != excludeSessionId) {
                session.send(json)
            }
        }
    }
}

package com.evoshot.core.controller.message

import com.evoshot.core.domain.Bullet
import com.evoshot.core.domain.PlayerState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface GameMessage

@Serializable
@SerialName("ping")
data class PingMessage(
    val timestamp: Long = System.currentTimeMillis(),
) : GameMessage

@Serializable
@SerialName("pong")
data class PongMessage(
    val timestamp: Long = System.currentTimeMillis(),
) : GameMessage

@Serializable
@SerialName("connect")
data class ConnectMessage(
    val playerName: String,
) : GameMessage

@Serializable
@SerialName("connected")
data class ConnectedMessage(
    val playerId: String,
    val players: List<PlayerState>,
) : GameMessage

@Serializable
@SerialName("player_join")
data class PlayerJoinMessage(
    val player: PlayerState,
) : GameMessage

@Serializable
@SerialName("player_leave")
data class PlayerLeaveMessage(
    val playerId: String,
) : GameMessage

@Serializable
@SerialName("player_input")
data class PlayerInputMessage(
    val dx: Int = 0,
    val jump: Boolean = false,
    val mouseX: Float,
    val mouseY: Float,
    val shoot: Boolean = false,
) : GameMessage

@Serializable
@SerialName("game_state")
data class GameStateMessage(
    val tick: Long,
    val players: List<PlayerState>,
    val bullet: List<Bullet>,
) : GameMessage

@Serializable
@SerialName("error")
data class ErrorMessage(
    val code: String,
    val message: String,
) : GameMessage

@Serializable
@SerialName("player_dead")
data class PlayerDeadMessage(
    val playerId: String,
    val killedByBulletId: String,
) : GameMessage

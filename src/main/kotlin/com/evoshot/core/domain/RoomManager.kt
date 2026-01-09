package com.evoshot.core.domain

import com.evoshot.core.engine.GameEngine

class RoomManager(
    private val gameEngine: GameEngine,
) {
    private val rooms = mutableMapOf<String, Room>()
    private val sessionToRoom = mutableMapOf<String, Room>()

    fun join(
        sessionId: String,
        playerName: String,
    ): Player? {
        val room = rooms.values.firstOrNull { !it.isFull } ?: createRoom()
        val player = room.join(sessionId = sessionId, playerName = playerName)
        if (player != null) {
            sessionToRoom[sessionId] = room
        }
        return player
    }

    fun leave(sessionId: String): String? {
        val room = sessionToRoom[sessionId] ?: return null
        val playerId = room.leave(sessionId)
        sessionToRoom.remove(sessionId)
        if (room.playerCount == 0) {
            rooms.remove(room.id)
        }
        return playerId
    }

    fun tick() {
        rooms.values.forEach { it.tick() }
    }

    fun handlePlayerInput(
        sessionId: String,
        dx: Int,
        jump: Boolean,
        mouseX: Float,
        mouseY: Float,
        shoot: Boolean,
    ) {
        val room = sessionToRoom[sessionId] ?: return
        room.handlePlayerInput(sessionId, dx, jump, mouseX, mouseY, shoot)
    }

    fun getAndRemoveDeadPlayers(): List<DeadPlayerInfo> {
        val deadPlayerInfos = mutableListOf<DeadPlayerInfo>()
        rooms.values.forEach { room ->
            val roomSessionIds = room.getAllSessionIds()
            val deadPlayers = room.getAndRemoveDeadPlayers()
            deadPlayers.forEach { result ->
                sessionToRoom.remove(result.sessionId)
                deadPlayerInfos.add(
                    DeadPlayerInfo(
                        playerId = result.playerId,
                        sessionId = result.sessionId,
                        roomSessionIds = roomSessionIds,
                    ),
                )
            }
        }
        return deadPlayerInfos
    }

    fun getAlivePlayerStates(sessionId: String): List<PlayerState> {
        val room = sessionToRoom[sessionId] ?: return emptyList()
        return room.getAlivePlayerStates()
    }

    fun getRoomSessionIds(sessionId: String): List<String> {
        val room = sessionToRoom[sessionId] ?: return emptyList()
        return room.getAllSessionIds()
    }

    fun getAllRoomStates(): List<RoomState> =
        rooms.values
            .filter { it.playerCount > 0 }
            .map { room ->
                RoomState(
                    sessionIds = room.getAllSessionIds(),
                    players = room.getAlivePlayerStates(),
                    bullets = room.getAllBullets(),
                )
            }

    private fun createRoom(): Room {
        val room = Room.create(gameEngine)
        rooms[room.id] = room
        return room
    }
}

data class DeadPlayerInfo(
    val playerId: String,
    val sessionId: String?,
    val roomSessionIds: List<String>,
)

data class RoomState(
    val sessionIds: List<String>,
    val players: List<PlayerState>,
    val bullets: List<Bullet>,
)

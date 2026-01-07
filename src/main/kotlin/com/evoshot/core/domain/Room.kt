package com.evoshot.core.domain

import com.evoshot.core.engine.GameEngine
import java.util.UUID

class Room(
    val id: String,
    private val playerRepository: PlayerRepository,
    private val bulletRepository: BulletRepository,
    private val gameEngine: GameEngine,
) {
    val playerCount: Int
        get() = playerRepository.count()

    val isFull: Boolean
        get() = playerRepository.isFull()

    fun join(
        sessionId: String,
        playerName: String,
    ): Player? {
        if (isFull) return null

        val player = Player.create(sessionId = sessionId, playerName = playerName)
        playerRepository.add(player)

        return player
    }

    fun leave(sessionId: String): String? {
        val player = playerRepository.findBySessionId(sessionId) ?: return null
        playerRepository.remove(player.id)
        return player.id
    }

    fun tick() {
        moveBullets()
        removeOutOfBoundsBullets()
        checkPlayerHits()
    }

    fun handlePlayerInput(
        sessionId: String,
        mouseX: Float,
        mouseY: Float,
        shoot: Boolean,
    ) {
        val player = playerRepository.findBySessionId(sessionId) ?: return

        player.move(tx = mouseX, ty = mouseY)

        if (shoot) {
            val bullet = Bullet.create(
                id = UUID.randomUUID().toString(),
                x = player.x,
                y = player.y,
                tx = mouseX,
                ty = mouseY,
            )
            bulletRepository.add(bullet)
        }
    }

    fun getAndRemoveDeadPlayers(): List<String> {
        val deadPlayers = playerRepository.findAllDead()
        val deadPlayerIds = deadPlayers.map { it.id }

        deadPlayerIds.forEach { playerId ->
            playerRepository.remove(playerId)
        }

        return deadPlayerIds
    }

    fun getPlayerIdBySessionId(sessionId: String): String? =
        playerRepository.findBySessionId(sessionId)?.id

    fun getSessionIdByPlayerId(playerId: String): String? =
        playerRepository.findById(playerId)?.sessionId

    fun getAlivePlayerStates(): List<PlayerState> =
        playerRepository.findAllAlive().map { it.toState() }

    fun getAllBullets(): List<Bullet> = bulletRepository.getAll()

    private fun moveBullets() {
        val bullets = bulletRepository.getAll()
        val movedBullets = gameEngine.moveBullets(bullets)
        bulletRepository.replaceAll(movedBullets)
    }

    private fun removeOutOfBoundsBullets() {
        val bullets = bulletRepository.getAll()
        val inBoundsBullets = gameEngine.filterInBounds(bullets)
        bulletRepository.replaceAll(inBoundsBullets)
    }

    private fun checkPlayerHits() {
        val bullets = bulletRepository.getAll()
        val alivePlayers = playerRepository.findAllAlive()

        val hitResult = gameEngine.checkHits(alivePlayers, bullets)

        hitResult.hitBulletIds.forEach { bulletId ->
            bulletRepository.remove(bulletId)
        }
    }
}

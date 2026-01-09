package com.evoshot.core.domain

import com.evoshot.core.engine.GameEngine
import java.util.UUID

class Room private constructor(
    val id: String,
    private val players: Players,
    private val bullets: Bullets,
    private val gameEngine: GameEngine,
) {
    val playerCount: Int
        get() = players.count
    val isFull: Boolean
        get() = players.isFull

    fun join(
        sessionId: String,
        playerName: String,
    ): Player? {
        if (isFull) return null

        val player = Player.create(sessionId = sessionId, playerName = playerName)
        players.add(player)

        return player
    }

    fun leave(sessionId: String): String? {
        val player = players.findBySessionId(sessionId) ?: return null
        players.remove(player.id)
        return player.id
    }

    fun tick() {
        val alivePlayers = players.findAllAlive()

        alivePlayers.forEach { it.applyInput() }

        val allBullets = bullets.getAll()
        val activeBullets = gameEngine.moveAndFilterBullets(allBullets)
        val hitResult = gameEngine.checkHits(alivePlayers, activeBullets)

        hitResult.hitPlayerIds.forEach { playerId ->
            players.findById(playerId)?.takeDamage()
        }

        val remainingBullets = activeBullets.filterNot { it.id in hitResult.hitBulletIds }
        bullets.replaceAll(remainingBullets)
    }

    fun handlePlayerInput(
        sessionId: String,
        dx: Int,
        jump: Boolean,
        mouseX: Float,
        mouseY: Float,
        shoot: Boolean,
    ) {
        val player = players.findBySessionId(sessionId) ?: return

        player.setInput(dx = dx, jump = jump)

        if (shoot && player.canShoot()) {
            bullets.createAndAdd(ownerId = player.id, x = player.x, y = player.y, tx = mouseX, ty = mouseY)
            player.recordShoot()
        }
    }

    fun getAndRemoveDeadPlayers(): List<DeadPlayerResult> {
        val deadPlayers = players.findAllDead()
        val results = deadPlayers.map { DeadPlayerResult(it.id, it.sessionId) }
        results.forEach { players.remove(it.playerId) }
        return results
    }

    fun getAlivePlayerStates(): List<PlayerState> = players.findAllAlive().map { it.toState() }

    fun getAllBullets(): List<Bullet> = bullets.getAll()

    fun getAllSessionIds(): List<String> = players.findAll().map { it.sessionId }

    data class DeadPlayerResult(
        val playerId: String,
        val sessionId: String,
    )

    companion object {
        private const val DEFAULT_MAX_PLAYERS = 2

        fun create(
            gameEngine: GameEngine,
            maxPlayers: Int = DEFAULT_MAX_PLAYERS,
        ): Room =
            Room(
                id = UUID.randomUUID().toString(),
                players = Players(maxPlayers),
                bullets = Bullets(),
                gameEngine = gameEngine,
            )
    }
}

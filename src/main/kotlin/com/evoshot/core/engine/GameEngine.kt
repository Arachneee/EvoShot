package com.evoshot.core.engine

import com.evoshot.core.domain.Bullet
import com.evoshot.core.domain.Player

class GameEngine(
    private val worldWidth: Float,
    private val worldHeight: Float,
) {
    fun moveBullets(bullets: List<Bullet>): List<Bullet> = bullets.map { it.move() }

    fun filterInBounds(bullets: List<Bullet>): List<Bullet> =
        bullets.filter { bullet ->
            bullet.x in 0f..worldWidth && bullet.y in 0f..worldHeight
        }

    fun checkHits(
        players: List<Player>,
        bullets: List<Bullet>,
    ): HitResult {
        val hitBulletIds = mutableSetOf<String>()

        players.forEach { player ->
            player.checkHit(bullets)?.let { hitBullet ->
                hitBulletIds.add(hitBullet.id)
            }
        }

        return HitResult(hitBulletIds = hitBulletIds)
    }
}

data class HitResult(
    val hitBulletIds: Set<String>,
)

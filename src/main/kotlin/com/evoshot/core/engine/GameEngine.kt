package com.evoshot.core.engine

import com.evoshot.core.domain.Bullet
import com.evoshot.core.domain.Player
import com.evoshot.core.util.VectorMath

class GameEngine {
    private val spatialGrid = SpatialGrid(
        cellSize = HIT_RADIUS * 2,
        worldWidth = WORLD_WIDTH,
        worldHeight = WORLD_HEIGHT,
    )

    fun moveAndFilterBullets(bullets: List<Bullet>): List<Bullet> =
        bullets.mapNotNull { bullet ->
            val moved = bullet.move()
            val isInBounds = moved.x in 0f..WORLD_WIDTH && moved.y in 0f..WORLD_HEIGHT
            val isNotOnGround = !moved.isOnGround()
            if (isInBounds && isNotOnGround) moved else null
        }

    fun checkHits(
        players: List<Player>,
        bullets: List<Bullet>,
    ): HitResult {
        val bulletGrid = spatialGrid.buildGrid(bullets)
        val hitBulletIds = mutableSetOf<String>()
        val hitPlayerIds = mutableSetOf<String>()

        for (player in players) {
            val nearbyBullets = spatialGrid.getNearbyItems(player.x, player.y, bulletGrid)

            for (bullet in nearbyBullets) {
                if (bullet.id in hitBulletIds) continue
                if (bullet.ownerId == player.id) continue
                if (isHit(player, bullet)) {
                    hitBulletIds.add(bullet.id)
                    hitPlayerIds.add(player.id)
                    break
                }
            }
        }

        return HitResult(hitBulletIds = hitBulletIds, hitPlayerIds = hitPlayerIds)
    }

    private fun isHit(player: Player, bullet: Bullet): Boolean =
        VectorMath.distanceSquared(player.x, player.y, bullet.x, bullet.y) <= HIT_RADIUS_SQUARED

    companion object {
        const val WORLD_WIDTH = 1600f
        const val WORLD_HEIGHT = 900f
        private const val PLAYER_RADIUS = 20f
        private const val BULLET_RADIUS = 5f
        private const val HIT_RADIUS = PLAYER_RADIUS + BULLET_RADIUS
        private const val HIT_RADIUS_SQUARED = HIT_RADIUS * HIT_RADIUS
    }
}

data class HitResult(
    val hitBulletIds: Set<String>,
    val hitPlayerIds: Set<String>,
)

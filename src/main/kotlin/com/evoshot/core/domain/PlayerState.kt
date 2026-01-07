package com.evoshot.core.domain

import com.evoshot.core.domain.Bullet
import com.evoshot.core.util.VectorMath
import kotlinx.serialization.Serializable
import java.util.UUID

@ConsistentCopyVisibility
@Serializable
data class PlayerState private constructor(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val isAlive: Boolean = true,
) {
    fun move(
        tx: Float,
        ty: Float,
    ): PlayerState {
        if (VectorMath.distance(x, y, tx, ty) == 0f) return this

        val (dx, dy) = VectorMath.normalizedDirection(x, y, tx, ty)
        val newX = (x + dx * SPEED).coerceIn(SPAWN_X_MIN.toFloat(), SPAWN_X_MAX.toFloat())
        val newY = (y + dy * SPEED).coerceIn(SPAWN_Y_MIN.toFloat(), SPAWN_Y_MAX.toFloat())
        return copy(
            x = newX,
            y = newY,
        )
    }

    fun checkHit(bullets: List<Bullet>): Pair<PlayerState, Bullet?> {
        for (bullet in bullets) {
            if (isHitBy(bullet)) {
                return copy(isAlive = false) to bullet
            }
        }
        return this to null
    }

    private fun isHitBy(bullet: Bullet): Boolean = VectorMath.distance(x, y, bullet.x, bullet.y) <= HEAT_SCAN_RADIUS

    companion object {
        const val SPEED: Float = 5f
        const val HEAT_SCAN_RADIUS: Float = 10f
        const val SPAWN_X_MIN: Int = 100
        const val SPAWN_X_MAX: Int = 1500
        const val SPAWN_Y_MIN: Int = 100
        const val SPAWN_Y_MAX: Int = 800

        fun create(name: String): PlayerState =
            PlayerState(
                id = UUID.randomUUID().toString(),
                name = name,
                x = (SPAWN_X_MIN..SPAWN_X_MAX).random().toFloat(),
                y = (SPAWN_Y_MIN..SPAWN_Y_MAX).random().toFloat(),
            )

        internal fun createForTest(
            id: String,
            name: String,
            x: Float,
            y: Float,
            isAlive: Boolean = true,
        ): PlayerState = PlayerState(id, name, x, y, isAlive)
    }
}

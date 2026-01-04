package com.evoshot.core.player

import com.evoshot.core.bullet.Bullet
import com.evoshot.core.util.VectorMath
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class PlayerState(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    var isAlive: Boolean = true,
) {
    fun move(
        tx: Float,
        ty: Float,
    ): PlayerState {
        if (VectorMath.distance(x, y, tx, ty) == 0f) return this

        val (dx, dy) = VectorMath.normalizedDirection(x, y, tx, ty)
        return copy(
            x = x + dx * SPEED,
            y = y + dy * SPEED,
        )
    }

    fun heat(bullets: List<Bullet>): Bullet? {
        for (bullet in bullets) {
            if (isHeated(bullet)) {
                isAlive = false
                return bullet
            }
        }
        return null
    }

    fun isHeated(bullet: Bullet): Boolean = VectorMath.distance(x, y, bullet.x, bullet.y) <= HEAT_SCAN_RADIUS

    companion object {
        const val SPEED: Float = 5f
        const val HEAT_SCAN_RADIUS: Float = 10f
    }
}

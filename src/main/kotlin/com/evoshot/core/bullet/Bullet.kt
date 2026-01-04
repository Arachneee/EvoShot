package com.evoshot.core.bullet

import com.evoshot.core.util.VectorMath
import kotlinx.serialization.Serializable

@Serializable
data class Bullet(
    val id: String,
    val x: Float,
    val y: Float,
    val headingX: Float,
    val headingY: Float,
) {
    fun move(): Bullet = copy(x = x + headingX * SPEED, y = y + headingY * SPEED)

    companion object {
        const val SPEED: Float = 20f

        fun create(
            id: String,
            x: Float,
            y: Float,
            tx: Float,
            ty: Float,
        ): Bullet {
            val (headingX, headingY) = VectorMath.normalizedDirection(x, y, tx, ty)
            return Bullet(id, x, y, headingX, headingY)
        }
    }
}

package com.evoshot.core.domain

import com.evoshot.core.util.VectorMath
import kotlinx.serialization.Serializable

@ConsistentCopyVisibility
@Serializable
data class Bullet private constructor(
    val id: String,
    override val x: Float,
    override val y: Float,
    val headingX: Float,
    val headingY: Float,
) : Positionable {
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

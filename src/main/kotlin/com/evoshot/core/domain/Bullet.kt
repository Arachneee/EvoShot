package com.evoshot.core.domain

import com.evoshot.core.engine.Physics
import com.evoshot.core.engine.Positionable
import com.evoshot.core.util.VectorMath
import kotlinx.serialization.Serializable

@ConsistentCopyVisibility
@Serializable
data class Bullet private constructor(
    val id: String,
    val ownerId: String,
    override val x: Float,
    override val y: Float,
    val velocityX: Float,
    val velocityY: Float,
) : Positionable {
    fun move(): Bullet {
        val newVelocityY = velocityY + Physics.GRAVITY
        return copy(
            x = x + velocityX,
            y = y + newVelocityY,
            velocityY = newVelocityY,
        )
    }

    fun isOnGround(): Boolean = y >= Physics.GROUND_Y

    companion object {
        const val SPEED: Float = 30f

        fun create(
            id: String,
            ownerId: String,
            x: Float,
            y: Float,
            tx: Float,
            ty: Float,
        ): Bullet {
            val (headingX, headingY) = VectorMath.normalizedDirection(x, y, tx, ty)
            return Bullet(
                id = id,
                ownerId = ownerId,
                x = x,
                y = y,
                velocityX = headingX * SPEED,
                velocityY = headingY * SPEED,
            )
        }
    }
}

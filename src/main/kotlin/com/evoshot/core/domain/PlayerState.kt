package com.evoshot.core.domain

import com.evoshot.core.engine.Physics
import com.evoshot.core.engine.Positionable
import kotlinx.serialization.Serializable
import java.util.UUID

@ConsistentCopyVisibility
@Serializable
data class PlayerState private constructor(
    val id: String,
    val name: String,
    override val x: Float,
    override val y: Float,
    val velocityY: Float = 0f,
    val hp: Int = MAX_HP,
) : Positionable {
    val isAlive: Boolean
        get() = hp > 0
    val isOnGround: Boolean
        get() = y >= Physics.GROUND_Y

    fun move(dx: Int): PlayerState {
        val newX = (x + dx * SPEED).coerceIn(SPAWN_X_MIN.toFloat(), SPAWN_X_MAX.toFloat())
        return if (newX == x) this else copy(x = newX)
    }

    fun applyGravity(): PlayerState {
        if (isOnGround && velocityY >= 0f) {
            return if (y == Physics.GROUND_Y && velocityY == 0f) {
                this
            } else {
                copy(y = Physics.GROUND_Y, velocityY = 0f)
            }
        }

        val newVelocityY = (velocityY + Physics.GRAVITY).coerceAtMost(Physics.MAX_FALL_VELOCITY)
        val newY = (y + newVelocityY).coerceAtMost(Physics.GROUND_Y)

        return if (newY >= Physics.GROUND_Y) {
            copy(y = Physics.GROUND_Y, velocityY = 0f)
        } else {
            copy(y = newY, velocityY = newVelocityY)
        }
    }

    fun jump(): PlayerState {
        if (!isOnGround) return this
        return copy(velocityY = Physics.JUMP_VELOCITY)
    }

    fun takeDamage(damage: Int = 1): PlayerState {
        val newHp = (hp - damage).coerceAtLeast(0)
        return if (newHp == hp) this else copy(hp = newHp)
    }

    companion object {
        const val MAX_HP: Int = 10
        const val SPEED: Float = 5f
        const val SPAWN_X_MIN: Int = 100
        const val SPAWN_X_MAX: Int = 1500
        const val SPAWN_Y_MIN: Int = 100
        const val SPAWN_Y_MAX: Int = 800

        fun create(name: String): PlayerState =
            PlayerState(
                id = UUID.randomUUID().toString(),
                name = name,
                x = (SPAWN_X_MIN..SPAWN_X_MAX).random().toFloat(),
                y = Physics.GROUND_Y,
                velocityY = 0f,
            )

        internal fun createForTest(
            id: String,
            name: String,
            x: Float,
            y: Float,
            velocityY: Float = 0f,
            hp: Int = MAX_HP,
        ): PlayerState = PlayerState(id, name, x, y, velocityY, hp)
    }
}

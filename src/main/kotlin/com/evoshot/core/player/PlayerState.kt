package com.evoshot.core.player

import kotlinx.serialization.Serializable
import kotlin.math.hypot

@Serializable
data class PlayerState(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
) {
    fun move(
        tx: Float,
        ty: Float,
    ): PlayerState {
        val dx = tx - x
        val dy = ty - y
        val distance = hypot(dx, dy)

        if (distance == 0f) return this

        return copy(
            x = x + (dx / distance) * SPEED,
            y = y + (dy / distance) * SPEED,
        )
    }

    companion object {
        const val SPEED: Float = 5f
    }
}

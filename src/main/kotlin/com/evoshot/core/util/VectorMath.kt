package com.evoshot.core.util

import kotlin.math.hypot

object VectorMath {
    fun normalizedDirection(
        fromX: Float,
        fromY: Float,
        toX: Float,
        toY: Float,
    ): Pair<Float, Float> {
        val dx = toX - fromX
        val dy = toY - fromY
        val length = hypot(dx, dy)
        if (length == 0f) return Pair(0f, 0f)
        return Pair(dx / length, dy / length)
    }

    fun distance(
        fromX: Float,
        fromY: Float,
        toX: Float,
        toY: Float,
    ): Float = hypot(toX - fromX, toY - fromY)
}


package com.evoshot.core.engine

import com.evoshot.core.domain.Bullet

class SpatialGrid(
    private val cellSize: Float,
    private val worldWidth: Float,
    private val worldHeight: Float,
) {
    private val cols = (worldWidth / cellSize).toInt() + 1
    private val rows = (worldHeight / cellSize).toInt() + 1

    fun buildBulletGrid(bullets: List<Bullet>): Map<Int, List<Bullet>> = bullets.groupBy { cellIndex(it.x, it.y) }

    private fun cellIndex(
        x: Float,
        y: Float,
    ): Int {
        val col = (x / cellSize).toInt().coerceIn(0, cols - 1)
        val row = (y / cellSize).toInt().coerceIn(0, rows - 1)
        return row * cols + col
    }

    fun getNearbyBullets(
        x: Float,
        y: Float,
        bulletGrid: Map<Int, List<Bullet>>,
    ): List<Bullet> {
        val centerCol = (x / cellSize).toInt()
        val centerRow = (y / cellSize).toInt()

        val result = mutableListOf<Bullet>()
        for (dRow in -1..1) {
            for (dCol in -1..1) {
                val col = (centerCol + dCol).coerceIn(0, cols - 1)
                val row = (centerRow + dRow).coerceIn(0, rows - 1)
                bulletGrid[row * cols + col]?.let { result.addAll(it) }
            }
        }
        return result
    }
}

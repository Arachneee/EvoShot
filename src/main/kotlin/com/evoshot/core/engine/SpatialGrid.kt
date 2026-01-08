package com.evoshot.core.engine

import com.evoshot.core.engine.Positionable

class SpatialGrid(
    private val cellSize: Float,
    private val worldWidth: Float,
    private val worldHeight: Float,
) {
    private val cols = (worldWidth / cellSize).toInt() + 1
    private val rows = (worldHeight / cellSize).toInt() + 1

    fun <T : Positionable> buildGrid(items: List<T>): Map<Int, List<T>> = items.groupBy { cellIndex(it.x, it.y) }

    private fun cellIndex(
        x: Float,
        y: Float,
    ): Int {
        val col = (x / cellSize).toInt().coerceIn(0, cols - 1)
        val row = (y / cellSize).toInt().coerceIn(0, rows - 1)
        return row * cols + col
    }

    fun <T : Positionable> getNearbyItems(
        x: Float,
        y: Float,
        grid: Map<Int, List<T>>,
    ): List<T> {
        val centerCol = (x / cellSize).toInt()
        val centerRow = (y / cellSize).toInt()

        val result = mutableListOf<T>()
        for (dRow in -1..1) {
            for (dCol in -1..1) {
                val col = (centerCol + dCol).coerceIn(0, cols - 1)
                val row = (centerRow + dRow).coerceIn(0, rows - 1)
                grid[row * cols + col]?.let { result.addAll(it) }
            }
        }
        return result
    }
}

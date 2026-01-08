package com.evoshot.core.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SpatialGridTest {
    private val cellSize = 100f
    private val worldWidth = 1000f
    private val worldHeight = 1000f
    private val spatialGrid = SpatialGrid(cellSize, worldWidth, worldHeight)

    private data class TestItem(override val x: Float, override val y: Float) : Positionable

    @Nested
    inner class BuildGrid {
        @Test
        fun `빈 리스트는 빈 맵을 반환한다`() {
            val items = emptyList<TestItem>()

            val grid = spatialGrid.buildGrid(items)

            assertThat(grid).isEmpty()
        }

        @Test
        fun `같은 셀에 있는 아이템들은 같은 그룹에 들어간다`() {
            val items = listOf(
                TestItem(10f, 10f),
                TestItem(20f, 20f),
                TestItem(50f, 50f),
            )

            val grid = spatialGrid.buildGrid(items)

            assertThat(grid).hasSize(1)
            assertThat(grid.values.first()).hasSize(3)
        }

        @Test
        fun `다른 셀에 있는 아이템들은 다른 그룹에 들어간다`() {
            val items = listOf(
                TestItem(50f, 50f),
                TestItem(150f, 50f),
                TestItem(50f, 150f),
            )

            val grid = spatialGrid.buildGrid(items)

            assertThat(grid).hasSize(3)
            grid.values.forEach { assertThat(it).hasSize(1) }
        }

        @Test
        fun `경계값에서 올바른 셀에 배치된다`() {
            val items = listOf(
                TestItem(99f, 99f),
                TestItem(100f, 100f),
            )

            val grid = spatialGrid.buildGrid(items)

            assertThat(grid).hasSize(2)
        }

        @Test
        fun `월드 경계를 벗어난 좌표는 경계 셀에 배치된다`() {
            val items = listOf(
                TestItem(-50f, -50f),
                TestItem(1500f, 1500f),
            )

            val grid = spatialGrid.buildGrid(items)

            assertThat(grid).hasSize(2)
        }
    }

    @Nested
    inner class GetNearbyItems {
        @Test
        fun `같은 셀의 아이템을 반환한다`() {
            val item = TestItem(150f, 150f)
            val grid = spatialGrid.buildGrid(listOf(item))

            val nearby = spatialGrid.getNearbyItems(150f, 150f, grid)

            assertThat(nearby).contains(item)
        }

        @Test
        fun `인접한 8개 셀의 아이템도 반환한다`() {
            val centerItem = TestItem(150f, 150f)
            val adjacentItems = listOf(
                TestItem(50f, 50f),
                TestItem(150f, 50f),
                TestItem(250f, 50f),
                TestItem(50f, 150f),
                TestItem(250f, 150f),
                TestItem(50f, 250f),
                TestItem(150f, 250f),
                TestItem(250f, 250f),
            )
            val allItems = adjacentItems + centerItem
            val grid = spatialGrid.buildGrid(allItems)

            val nearby = spatialGrid.getNearbyItems(150f, 150f, grid)

            assertThat(nearby).containsAll(allItems)
        }

        @Test
        fun `2칸 이상 떨어진 셀의 아이템은 반환하지 않는다`() {
            val nearbyItem = TestItem(150f, 150f)
            val farItem = TestItem(450f, 450f)
            val items = listOf(nearbyItem, farItem)
            val grid = spatialGrid.buildGrid(items)

            val nearby = spatialGrid.getNearbyItems(150f, 150f, grid)

            assertThat(nearby).contains(nearbyItem)
            assertThat(nearby).doesNotContain(farItem)
        }

        @Test
        fun `빈 그리드에서는 빈 리스트를 반환한다`() {
            val grid = spatialGrid.buildGrid(emptyList<TestItem>())

            val nearby = spatialGrid.getNearbyItems(150f, 150f, grid)

            assertThat(nearby).isEmpty()
        }

        @Test
        fun `월드 경계 근처에서도 인접 셀의 아이템을 반환한다`() {
            val item = TestItem(50f, 50f)
            val grid = spatialGrid.buildGrid(listOf(item))

            val nearby = spatialGrid.getNearbyItems(0f, 0f, grid)

            assertThat(nearby).contains(item)
        }

        @Test
        fun `음수 좌표로 검색해도 경계 셀의 아이템을 반환한다`() {
            val item = TestItem(50f, 50f)
            val grid = spatialGrid.buildGrid(listOf(item))

            val nearby = spatialGrid.getNearbyItems(-50f, -50f, grid)

            assertThat(nearby).contains(item)
        }

        @Test
        fun `월드 최대 경계를 벗어난 좌표로 검색해도 동작한다`() {
            val item = TestItem(1050f, 1050f)
            val grid = spatialGrid.buildGrid(listOf(item))

            val nearby = spatialGrid.getNearbyItems(1100f, 1100f, grid)

            assertThat(nearby).contains(item)
        }
    }
}


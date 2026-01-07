package com.evoshot.core.util

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VectorMathTest {
    private val offset = Offset.offset(0.0001f)

    @Nested
    inner class NormalizedDirection {
        @Test
        fun `오른쪽 방향은 (1, 0)을 반환한다`() {
            val (dx, dy) = VectorMath.normalizedDirection(0f, 0f, 100f, 0f)

            assertThat(dx).isCloseTo(1f, offset)
            assertThat(dy).isCloseTo(0f, offset)
        }

        @Test
        fun `왼쪽 방향은 (-1, 0)을 반환한다`() {
            val (dx, dy) = VectorMath.normalizedDirection(100f, 0f, 0f, 0f)

            assertThat(dx).isCloseTo(-1f, offset)
            assertThat(dy).isCloseTo(0f, offset)
        }

        @Test
        fun `위쪽 방향은 (0, 1)을 반환한다`() {
            val (dx, dy) = VectorMath.normalizedDirection(0f, 0f, 0f, 100f)

            assertThat(dx).isCloseTo(0f, offset)
            assertThat(dy).isCloseTo(1f, offset)
        }

        @Test
        fun `아래쪽 방향은 (0, -1)을 반환한다`() {
            val (dx, dy) = VectorMath.normalizedDirection(0f, 100f, 0f, 0f)

            assertThat(dx).isCloseTo(0f, offset)
            assertThat(dy).isCloseTo(-1f, offset)
        }

        @Test
        fun `대각선 방향은 정규화된 벡터를 반환한다`() {
            val (dx, dy) = VectorMath.normalizedDirection(0f, 0f, 100f, 100f)

            val expectedComponent = 1f / kotlin.math.sqrt(2f)
            assertThat(dx).isCloseTo(expectedComponent, offset)
            assertThat(dy).isCloseTo(expectedComponent, offset)
        }

        @Test
        fun `정규화된 벡터의 길이는 1이다`() {
            val (dx, dy) = VectorMath.normalizedDirection(10f, 20f, 50f, 80f)

            val length = kotlin.math.hypot(dx, dy)
            assertThat(length).isCloseTo(1f, offset)
        }

        @Test
        fun `같은 위치에서는 (0, 0)을 반환한다`() {
            val (dx, dy) = VectorMath.normalizedDirection(100f, 100f, 100f, 100f)

            assertThat(dx).isCloseTo(0f, offset)
            assertThat(dy).isCloseTo(0f, offset)
        }

        @Test
        fun `거리에 상관없이 정규화된 방향은 동일하다`() {
            val (dx1, dy1) = VectorMath.normalizedDirection(0f, 0f, 10f, 0f)
            val (dx2, dy2) = VectorMath.normalizedDirection(0f, 0f, 1000f, 0f)

            assertThat(dx1).isCloseTo(dx2, offset)
            assertThat(dy1).isCloseTo(dy2, offset)
        }
    }

    @Nested
    inner class Distance {
        @Test
        fun `같은 위치의 거리는 0이다`() {
            val distance = VectorMath.distance(100f, 100f, 100f, 100f)

            assertThat(distance).isCloseTo(0f, offset)
        }

        @Test
        fun `수평 거리를 올바르게 계산한다`() {
            val distance = VectorMath.distance(0f, 0f, 100f, 0f)

            assertThat(distance).isCloseTo(100f, offset)
        }

        @Test
        fun `수직 거리를 올바르게 계산한다`() {
            val distance = VectorMath.distance(0f, 0f, 0f, 100f)

            assertThat(distance).isCloseTo(100f, offset)
        }

        @Test
        fun `대각선 거리를 올바르게 계산한다`() {
            val distance = VectorMath.distance(0f, 0f, 3f, 4f)

            assertThat(distance).isCloseTo(5f, offset)
        }

        @Test
        fun `음수 좌표에서도 올바르게 계산한다`() {
            val distance = VectorMath.distance(-10f, -10f, 10f, 10f)

            val expected = kotlin.math.hypot(20f, 20f)
            assertThat(distance).isCloseTo(expected, offset)
        }

        @Test
        fun `순서를 바꿔도 거리는 동일하다`() {
            val distance1 = VectorMath.distance(10f, 20f, 50f, 80f)
            val distance2 = VectorMath.distance(50f, 80f, 10f, 20f)

            assertThat(distance1).isCloseTo(distance2, offset)
        }
    }
}


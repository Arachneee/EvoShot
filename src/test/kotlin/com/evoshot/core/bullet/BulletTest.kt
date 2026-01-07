package com.evoshot.core.bullet

import com.evoshot.core.domain.Bullet
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test

class BulletTest {
    private val offset = Offset.offset(0.0001f)

    @Test
    fun `Bullet 생성 시 방향이 정규화된다`() {
        val bullet = Bullet.create(
            id = "bullet-1",
            x = 0f,
            y = 0f,
            tx = 100f,
            ty = 0f,
        )

        assertThat(bullet.headingX).isCloseTo(1f, offset)
        assertThat(bullet.headingY).isCloseTo(0f, offset)
    }

    @Test
    fun `대각선 방향으로 생성 시 방향 벡터가 정규화된다`() {
        val bullet = Bullet.create(
            id = "bullet-1",
            x = 0f,
            y = 0f,
            tx = 100f,
            ty = 100f,
        )

        val expectedComponent = 1f / kotlin.math.sqrt(2f)
        assertThat(bullet.headingX).isCloseTo(expectedComponent, offset)
        assertThat(bullet.headingY).isCloseTo(expectedComponent, offset)
    }

    @Test
    fun `move 호출 시 SPEED만큼 방향으로 이동한다`() {
        val bullet = Bullet.create(
            id = "bullet-1",
            x = 100f,
            y = 100f,
            tx = 200f,
            ty = 100f,
        )

        val moved = bullet.move()

        assertThat(moved.x).isCloseTo(100f + Bullet.SPEED, offset)
        assertThat(moved.y).isCloseTo(100f, offset)
    }

    @Test
    fun `대각선 방향으로 이동 시 정규화된 방향으로 SPEED만큼 이동한다`() {
        val bullet = Bullet.create(
            id = "bullet-1",
            x = 100f,
            y = 100f,
            tx = 200f,
            ty = 200f,
        )

        val moved = bullet.move()

        val expectedComponent = Bullet.SPEED / kotlin.math.sqrt(2f)
        assertThat(moved.x).isCloseTo(100f + expectedComponent, offset)
        assertThat(moved.y).isCloseTo(100f + expectedComponent, offset)
    }

    @Test
    fun `move 호출 후에도 id와 방향은 유지된다`() {
        val bullet = Bullet.create(
            id = "bullet-1",
            x = 0f,
            y = 0f,
            tx = 100f,
            ty = 50f,
        )

        val moved = bullet.move()

        assertThat(moved.id).isEqualTo("bullet-1")
        assertThat(moved.headingX).isEqualTo(bullet.headingX)
        assertThat(moved.headingY).isEqualTo(bullet.headingY)
    }

    @Test
    fun `연속 move 호출 시 누적 이동한다`() {
        val bullet = Bullet.create(
            id = "bullet-1",
            x = 0f,
            y = 0f,
            tx = 100f,
            ty = 0f,
        )

        val movedTwice = bullet.move().move()

        assertThat(movedTwice.x).isCloseTo(Bullet.SPEED * 2, offset)
        assertThat(movedTwice.y).isCloseTo(0f, offset)
    }
}


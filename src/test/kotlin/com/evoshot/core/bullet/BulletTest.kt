package com.evoshot.core.bullet

import com.evoshot.core.domain.Bullet
import com.evoshot.core.engine.Physics
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BulletTest {
    private val offset = Offset.offset(0.0001f)

    @Nested
    inner class Create {
        @Test
        fun `Bullet 생성 시 속도가 정규화된 방향에 SPEED를 곱한 값이다`() {
            val bullet = Bullet.create(
                id = "bullet-1",
                ownerId = "player-1",
                x = 0f,
                y = 0f,
                tx = 100f,
                ty = 0f,
            )

            assertThat(bullet.velocityX).isCloseTo(Bullet.SPEED, offset)
            assertThat(bullet.velocityY).isCloseTo(0f, offset)
        }

        @Test
        fun `대각선 방향으로 생성 시 속도 벡터가 정규화된 방향으로 설정된다`() {
            val bullet = Bullet.create(
                id = "bullet-1",
                ownerId = "player-1",
                x = 0f,
                y = 0f,
                tx = 100f,
                ty = 100f,
            )

            val expectedComponent = Bullet.SPEED / kotlin.math.sqrt(2f)
            assertThat(bullet.velocityX).isCloseTo(expectedComponent, offset)
            assertThat(bullet.velocityY).isCloseTo(expectedComponent, offset)
        }
    }

    @Nested
    inner class Move {
        @Test
        fun `move 호출 시 속도만큼 이동하고 중력이 적용된다`() {
            val bullet = Bullet.create(
                id = "bullet-1",
                ownerId = "player-1",
                x = 100f,
                y = 100f,
                tx = 200f,
                ty = 100f,
            )

            val moved = bullet.move()

            assertThat(moved.x).isCloseTo(100f + Bullet.SPEED, offset)
            assertThat(moved.y).isGreaterThan(100f)
            assertThat(moved.velocityY).isCloseTo(Physics.GRAVITY, offset)
        }

        @Test
        fun `move 호출 후에도 id와 velocityX는 유지된다`() {
            val bullet = Bullet.create(
                id = "bullet-1",
                ownerId = "player-1",
                x = 0f,
                y = 0f,
                tx = 100f,
                ty = 50f,
            )

            val moved = bullet.move()

            assertThat(moved.id).isEqualTo("bullet-1")
            assertThat(moved.velocityX).isEqualTo(bullet.velocityX)
        }

        @Test
        fun `연속 move 호출 시 중력이 누적 적용된다`() {
            val bullet = Bullet.create(
                id = "bullet-1",
                ownerId = "player-1",
                x = 0f,
                y = 0f,
                tx = 100f,
                ty = 0f,
            )

            val movedTwice = bullet.move().move()

            assertThat(movedTwice.x).isCloseTo(Bullet.SPEED * 2, offset)
            val expectedVelocityY = Physics.GRAVITY * 2
            assertThat(movedTwice.velocityY).isCloseTo(expectedVelocityY, offset)
        }
    }

    @Nested
    inner class IsOnGround {
        @Test
        fun `y가 GROUND_Y 이상이면 바닥에 있다`() {
            val bullet = Bullet.create(
                id = "bullet-1",
                ownerId = "player-1",
                x = 100f,
                y = Physics.GROUND_Y,
                tx = 200f,
                ty = Physics.GROUND_Y,
            )

            assertThat(bullet.isOnGround()).isTrue()
        }

        @Test
        fun `y가 GROUND_Y 미만이면 공중에 있다`() {
            val bullet = Bullet.create(
                id = "bullet-1",
                ownerId = "player-1",
                x = 100f,
                y = Physics.GROUND_Y - 100f,
                tx = 200f,
                ty = Physics.GROUND_Y - 100f,
            )

            assertThat(bullet.isOnGround()).isFalse()
        }
    }
}


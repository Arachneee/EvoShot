package com.evoshot.core.player

import com.evoshot.core.domain.PlayerState
import com.evoshot.core.engine.Physics
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PlayerStateTest {
    private val offset = Offset.offset(0.0001f)

    @Nested
    inner class Move {
        @Test
        fun `dx가 0이면 이동하지 않는다`() {
            val state = PlayerState.createForTest(id = "1", name = "player", x = 100f, y = 100f)

            val result = state.move(dx = 0)

            assertThat(result).isEqualTo(state)
        }

        @Test
        fun `dx가 1이면 오른쪽으로 SPEED만큼 이동한다`() {
            val state = PlayerState.createForTest(id = "1", name = "player", x = 100f, y = 100f)

            val result = state.move(dx = 1)

            assertThat(result.x).isCloseTo(100f + PlayerState.SPEED, offset)
            assertThat(result.y).isCloseTo(100f, offset)
        }

        @Test
        fun `dx가 -1이면 왼쪽으로 SPEED만큼 이동한다`() {
            val state = PlayerState.createForTest(id = "1", name = "player", x = 200f, y = 100f)

            val result = state.move(dx = -1)

            assertThat(result.x).isCloseTo(200f - PlayerState.SPEED, offset)
            assertThat(result.y).isCloseTo(100f, offset)
        }

        @Test
        fun `이동 후 id와 name은 유지된다`() {
            val state = PlayerState.createForTest(id = "player-1", name = "홍길동", x = 150f, y = 150f)

            val result = state.move(dx = 1)

            assertThat(result.id).isEqualTo("player-1")
            assertThat(result.name).isEqualTo("홍길동")
        }

        @Test
        fun `맵 경계를 벗어나지 않는다`() {
            val stateAtMinX = PlayerState.createForTest(id = "1", name = "player", x = 100f, y = 100f)

            val result = stateAtMinX.move(dx = -1)

            assertThat(result.x).isGreaterThanOrEqualTo(PlayerState.SPAWN_X_MIN.toFloat())
        }
    }

    @Nested
    inner class Gravity {
        @Test
        fun `바닥에 있으면 중력이 적용되지 않는다`() {
            val state = PlayerState.createForTest(
                id = "1",
                name = "player",
                x = 100f,
                y = Physics.GROUND_Y,
                velocityY = 0f,
            )

            val result = state.applyGravity()

            assertThat(result.y).isCloseTo(Physics.GROUND_Y, offset)
            assertThat(result.velocityY).isCloseTo(0f, offset)
        }

        @Test
        fun `공중에 있으면 중력이 적용되어 떨어진다`() {
            val state = PlayerState.createForTest(
                id = "1",
                name = "player",
                x = 100f,
                y = 500f,
                velocityY = 0f,
            )

            val result = state.applyGravity()

            assertThat(result.y).isGreaterThan(500f)
            assertThat(result.velocityY).isGreaterThan(0f)
        }

        @Test
        fun `떨어지다가 바닥에 도달하면 바닥에 멈춘다`() {
            val state = PlayerState.createForTest(
                id = "1",
                name = "player",
                x = 100f,
                y = Physics.GROUND_Y - 1f,
                velocityY = 10f,
            )

            val result = state.applyGravity()

            assertThat(result.y).isCloseTo(Physics.GROUND_Y, offset)
            assertThat(result.velocityY).isCloseTo(0f, offset)
        }

        @Test
        fun `최대 낙하 속도를 초과하지 않는다`() {
            val state = PlayerState.createForTest(
                id = "1",
                name = "player",
                x = 100f,
                y = 100f,
                velocityY = Physics.MAX_FALL_VELOCITY,
            )

            val result = state.applyGravity()

            assertThat(result.velocityY).isLessThanOrEqualTo(Physics.MAX_FALL_VELOCITY)
        }
    }

    @Nested
    inner class Jump {
        @Test
        fun `바닥에서 점프하면 위로 올라간다`() {
            val state = PlayerState.createForTest(
                id = "1",
                name = "player",
                x = 100f,
                y = Physics.GROUND_Y,
                velocityY = 0f,
            )

            val result = state.jump()

            assertThat(result.velocityY).isCloseTo(Physics.JUMP_VELOCITY, offset)
        }

        @Test
        fun `공중에서는 점프할 수 없다`() {
            val state = PlayerState.createForTest(
                id = "1",
                name = "player",
                x = 100f,
                y = 500f,
                velocityY = -5f,
            )

            val result = state.jump()

            assertThat(result.velocityY).isCloseTo(-5f, offset)
        }
    }

    @Nested
    inner class IsOnGround {
        @Test
        fun `y가 GROUND_Y 이상이면 바닥에 있다`() {
            val state = PlayerState.createForTest(
                id = "1",
                name = "player",
                x = 100f,
                y = Physics.GROUND_Y,
            )

            assertThat(state.isOnGround).isTrue()
        }

        @Test
        fun `y가 GROUND_Y 미만이면 공중에 있다`() {
            val state = PlayerState.createForTest(
                id = "1",
                name = "player",
                x = 100f,
                y = Physics.GROUND_Y - 1f,
            )

            assertThat(state.isOnGround).isFalse()
        }
    }
}

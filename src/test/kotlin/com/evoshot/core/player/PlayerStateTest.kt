package com.evoshot.core.player

import com.evoshot.core.domain.PlayerState
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test

class PlayerStateTest {
    private val offset = Offset.offset(0.0001f)

    @Test
    fun `목표 지점과 현재 위치가 같으면 이동하지 않는다`() {
        val state = PlayerState.createForTest(id = "1", name = "player", x = 100f, y = 100f)

        val result = state.move(tx = 100f, ty = 100f)

        assertThat(result).isEqualTo(state)
    }

    @Test
    fun `오른쪽 방향으로 SPEED만큼 이동한다`() {
        val state = PlayerState.createForTest(id = "1", name = "player", x = 100f, y = 100f)

        val result = state.move(tx = 200f, ty = 100f)

        assertThat(result.x).isCloseTo(100f + PlayerState.SPEED, offset)
        assertThat(result.y).isCloseTo(100f, offset)
    }

    @Test
    fun `위쪽 방향으로 SPEED만큼 이동한다`() {
        val state = PlayerState.createForTest(id = "1", name = "player", x = 100f, y = 100f)

        val result = state.move(tx = 100f, ty = 200f)

        assertThat(result.x).isCloseTo(100f, offset)
        assertThat(result.y).isCloseTo(100f + PlayerState.SPEED, offset)
    }

    @Test
    fun `대각선 방향으로 이동하면 정규화된 방향으로 SPEED만큼 이동한다`() {
        val state = PlayerState.createForTest(id = "1", name = "player", x = 100f, y = 100f)

        val result = state.move(tx = 200f, ty = 200f)

        val expectedComponent = PlayerState.SPEED / kotlin.math.sqrt(2f)
        assertThat(result.x).isCloseTo(100f + expectedComponent, offset)
        assertThat(result.y).isCloseTo(100f + expectedComponent, offset)
    }

    @Test
    fun `목표 지점까지의 거리와 상관없이 SPEED만큼만 이동한다`() {
        val state = PlayerState.createForTest(id = "1", name = "player", x = 100f, y = 100f)

        val shortDistance = state.move(tx = 102f, ty = 100f)
        val longDistance = state.move(tx = 1000f, ty = 100f)

        assertThat(shortDistance.x).isCloseTo(longDistance.x, offset)
        assertThat(shortDistance.y).isCloseTo(longDistance.y, offset)
    }

    @Test
    fun `이동 후 id와 name은 유지된다`() {
        val state = PlayerState.createForTest(id = "player-1", name = "홍길동", x = 150f, y = 150f)

        val result = state.move(tx = 200f, ty = 200f)

        assertThat(result.id).isEqualTo("player-1")
        assertThat(result.name).isEqualTo("홍길동")
    }
}

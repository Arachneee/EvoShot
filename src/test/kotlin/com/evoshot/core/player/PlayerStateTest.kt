package com.evoshot.core.player

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test

class PlayerStateTest {
    private val offset = Offset.offset(0.0001f)

    @Test
    fun `목표 지점과 현재 위치가 같으면 이동하지 않는다`() {
        val state = PlayerState(id = "1", name = "player", x = 100f, y = 100f)

        val result = state.move(tx = 100f, ty = 100f)

        assertThat(result).isEqualTo(state)
    }

    @Test
    fun `오른쪽 방향으로 SPEED만큼 이동한다`() {
        val state = PlayerState(id = "1", name = "player", x = 0f, y = 0f)

        val result = state.move(tx = 10f, ty = 0f)

        assertThat(result.x).isCloseTo(PlayerState.SPEED, offset)
        assertThat(result.y).isCloseTo(0f, offset)
    }

    @Test
    fun `위쪽 방향으로 SPEED만큼 이동한다`() {
        val state = PlayerState(id = "1", name = "player", x = 0f, y = 0f)

        val result = state.move(tx = 0f, ty = 10f)

        assertThat(result.x).isCloseTo(0f, offset)
        assertThat(result.y).isCloseTo(PlayerState.SPEED, offset)
    }

    @Test
    fun `대각선 방향으로 이동하면 정규화된 방향으로 SPEED만큼 이동한다`() {
        val state = PlayerState(id = "1", name = "player", x = 0f, y = 0f)

        val result = state.move(tx = 10f, ty = 10f)

        val expectedComponent = PlayerState.SPEED / kotlin.math.sqrt(2f)
        assertThat(result.x).isCloseTo(expectedComponent, offset)
        assertThat(result.y).isCloseTo(expectedComponent, offset)
    }

    @Test
    fun `목표 지점까지의 거리와 상관없이 SPEED만큼만 이동한다`() {
        val state = PlayerState(id = "1", name = "player", x = 0f, y = 0f)

        val shortDistance = state.move(tx = 2f, ty = 0f)
        val longDistance = state.move(tx = 1000f, ty = 0f)

        assertThat(shortDistance.x).isCloseTo(longDistance.x, offset)
        assertThat(shortDistance.y).isCloseTo(longDistance.y, offset)
    }

    @Test
    fun `이동 후 id와 name은 유지된다`() {
        val state = PlayerState(id = "player-1", name = "홍길동", x = 50f, y = 50f)

        val result = state.move(tx = 100f, ty = 100f)

        assertThat(result.id).isEqualTo("player-1")
        assertThat(result.name).isEqualTo("홍길동")
    }
}

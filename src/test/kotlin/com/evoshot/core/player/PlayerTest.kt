package com.evoshot.core.player

import com.evoshot.core.domain.Player
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PlayerTest {
    @Test
    fun `Player 생성 시 이름이 올바르게 설정된다`() {
        val player = Player.create(sessionId = "session-1", playerName = "홍길동")

        assertThat(player.name).isEqualTo("홍길동")
    }

    @Test
    fun `Player 생성 시 sessionId가 올바르게 설정된다`() {
        val player = Player.create(sessionId = "session-1", playerName = "홍길동")

        assertThat(player.sessionId).isEqualTo("session-1")
    }

    @Test
    fun `Player 생성 시 isAlive가 true이다`() {
        val player = Player.create(sessionId = "session-1", playerName = "홍길동")

        assertThat(player.isAlive).isTrue()
    }

    @Test
    fun `Player 생성 시 고유 id가 생성된다`() {
        val player1 = Player.create(sessionId = "session-1", playerName = "홍길동")
        val player2 = Player.create(sessionId = "session-2", playerName = "김철수")

        assertThat(player1.id).isNotEqualTo(player2.id)
    }

    @Test
    fun `move 호출 시 위치가 변경된다`() {
        val player = Player.create(sessionId = "session-1", playerName = "홍길동")
        val initialX = player.x
        val initialY = player.y

        player.move(tx = initialX + 100f, ty = initialY + 100f)

        assertThat(player.x).isNotEqualTo(initialX)
        assertThat(player.y).isNotEqualTo(initialY)
    }

    @Test
    fun `toState 호출 시 현재 상태를 반환한다`() {
        val player = Player.create(sessionId = "session-1", playerName = "홍길동")

        val state = player.toState()

        assertThat(state.id).isEqualTo(player.id)
        assertThat(state.name).isEqualTo(player.name)
        assertThat(state.x).isEqualTo(player.x)
        assertThat(state.y).isEqualTo(player.y)
        assertThat(state.isAlive).isEqualTo(player.isAlive)
    }

    @Test
    fun `move 후 toState는 변경된 위치를 반환한다`() {
        val player = Player.create(sessionId = "session-1", playerName = "홍길동")
        val initialX = player.x
        val initialY = player.y

        player.move(tx = initialX + 100f, ty = initialY + 100f)
        val state = player.toState()

        assertThat(state.x).isNotEqualTo(initialX)
        assertThat(state.y).isNotEqualTo(initialY)
    }

    @Test
    fun `kill 호출 시 isAlive가 false가 된다`() {
        val player = Player.create(sessionId = "session-1", playerName = "홍길동")

        player.kill()

        assertThat(player.isAlive).isFalse()
    }
}


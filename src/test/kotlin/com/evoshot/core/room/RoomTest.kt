package com.evoshot.core.room

import com.evoshot.core.domain.Room
import com.evoshot.core.engine.GameEngine
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RoomTest {
    private lateinit var room: Room
    private lateinit var gameEngine: GameEngine

    @BeforeEach
    fun setUp() {
        gameEngine = GameEngine()
        room = Room.create(gameEngine = gameEngine, maxPlayers = 10)
    }

    @Test
    fun `플레이어가 룸에 입장할 수 있다`() {
        val player = room.join(sessionId = "session1", playerName = "홍길동")

        assertThat(player).isNotNull
        assertThat(room.playerCount).isEqualTo(1)
    }

    @Test
    fun `입장한 플레이어 정보가 올바르게 설정된다`() {
        val player = room.join(sessionId = "session1", playerName = "홍길동")

        assertThat(player).isNotNull
        assertThat(player!!.name).isEqualTo("홍길동")
        assertThat(player.isAlive).isTrue()
    }

    @Test
    fun `플레이어가 룸에서 퇴장할 수 있다`() {
        val player = room.join(sessionId = "session1", playerName = "홍길동")

        val leftPlayerId = room.leave("session1")

        assertThat(leftPlayerId).isEqualTo(player!!.id)
        assertThat(room.playerCount).isEqualTo(0)
    }

    @Test
    fun `퇴장 시 퇴장한 플레이어 ID를 반환한다`() {
        val player = room.join(sessionId = "session1", playerName = "홍길동")

        val leftPlayerId = room.leave("session1")

        assertThat(leftPlayerId).isEqualTo(player!!.id)
    }

    @Test
    fun `최대 인원 초과 시 입장할 수 없다`() {
        val smallRoom = Room.create(gameEngine = gameEngine, maxPlayers = 2)
        smallRoom.join(sessionId = "s1", playerName = "Player1")
        smallRoom.join(sessionId = "s2", playerName = "Player2")

        val result = smallRoom.join(sessionId = "s3", playerName = "Player3")

        assertThat(result).isNull()
        assertThat(smallRoom.playerCount).isEqualTo(2)
    }

    @Test
    fun `플레이어 입력 처리 시 플레이어가 이동한다`() {
        val player = room.join(sessionId = "session1", playerName = "홍길동")
        val initialX = player!!.x
        val initialY = player.y

        room.handlePlayerInput(
            sessionId = "session1",
            mouseX = initialX + 100f,
            mouseY = initialY + 100f,
            shoot = false,
        )

        val states = room.getAlivePlayerStates()
        val updatedState = states.find { it.id == player.id }
        assertThat(updatedState).isNotNull
        assertThat(updatedState!!.x).isNotEqualTo(initialX)
        assertThat(updatedState.y).isNotEqualTo(initialY)
    }

    @Test
    fun `전체 플레이어 상태를 조회할 수 있다`() {
        room.join(sessionId = "s1", playerName = "Player1")
        room.join(sessionId = "s2", playerName = "Player2")
        room.join(sessionId = "s3", playerName = "Player3")

        val states = room.getAlivePlayerStates()

        assertThat(states).hasSize(3)
    }
}

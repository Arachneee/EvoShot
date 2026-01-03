package com.evoshot.core.room

import com.evoshot.core.handler.message.GameMessage
import com.evoshot.core.handler.message.PlayerLeaveMessage
import com.evoshot.infra.codec.MessageCodec
import com.evoshot.infra.session.Session
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.netty.channel.Channel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RoomTest {
    private lateinit var room: Room
    private lateinit var codec: MessageCodec

    @BeforeEach
    fun setUp() {
        room = Room(id = "test-room", maxPlayers = 10)
        codec = mockk(relaxed = true)
    }

    private fun createSession(
        playerId: String,
        playerName: String,
    ): Session {
        val channel = mockk<Channel>(relaxed = true)
        every { channel.isActive } returns true

        val session = Session(playerId = playerId, channel = channel, codec = codec)
        session.setPlayerName(playerName)
        return session
    }

    @Test
    fun `플레이어가 룸에 입장할 수 있다`() {
        val session = createSession("player1", "홍길동")

        val result = room.join(session)

        assertThat(result).isTrue()
        assertThat(room.playerCount).isEqualTo(1)
    }

    @Test
    fun `입장 시 플레이어 상태가 생성된다`() {
        val session = createSession("player1", "홍길동")

        room.join(session)

        val state = room.getPlayerState("player1")
        assertThat(state).isNotNull
        assertThat(state!!.id).isEqualTo("player1")
        assertThat(state.name).isEqualTo("홍길동")
        assertThat(state.x).isBetween(100f, 700f)
        assertThat(state.y).isBetween(100f, 500f)
    }

    @Test
    fun `플레이어가 룸에서 퇴장할 수 있다`() {
        val session = createSession("player1", "홍길동")
        room.join(session)

        val result = room.leave(session)

        assertThat(result).isTrue()
        assertThat(room.playerCount).isEqualTo(0)
        assertThat(room.getPlayerState("player1")).isNull()
    }

    @Test
    fun `퇴장 시 다른 플레이어에게 알림이 간다`() {
        val session1 = createSession("player1", "홍길동")
        val session2 = createSession("player2", "김철수")
        room.join(session1)
        room.join(session2)

        val messageSlot = slot<GameMessage>()

        room.leave(session1)

        verify { codec.encodeToString(capture(messageSlot)) }
        assertThat(messageSlot.captured).isInstanceOf(PlayerLeaveMessage::class.java)
        assertThat((messageSlot.captured as PlayerLeaveMessage).playerId).isEqualTo("player1")
    }

    @Test
    fun `최대 인원 초과 시 입장할 수 없다`() {
        val smallRoom = Room(id = "small", maxPlayers = 2)
        smallRoom.join(createSession("p1", "Player1"))
        smallRoom.join(createSession("p2", "Player2"))

        val result = smallRoom.join(createSession("p3", "Player3"))

        assertThat(result).isFalse()
        assertThat(smallRoom.playerCount).isEqualTo(2)
    }

    @Test
    fun `플레이어 상태를 업데이트할 수 있다`() {
        val session = createSession("player1", "홍길동")
        room.join(session)

        room.updatePlayerState("player1") { state ->
            state.copy(x = 200f, y = 300f)
        }

        val state = room.getPlayerState("player1")
        assertThat(state!!.x).isEqualTo(200f)
        assertThat(state.y).isEqualTo(300f)
    }

    @Test
    fun `전체 플레이어 상태를 조회할 수 있다`() {
        room.join(createSession("p1", "Player1"))
        room.join(createSession("p2", "Player2"))
        room.join(createSession("p3", "Player3"))

        val states = room.getAllPlayerStates()

        assertThat(states).hasSize(3)
    }
}

package com.evoshot.infra.codec

import com.evoshot.core.handler.message.ConnectMessage
import com.evoshot.core.handler.message.GameStateMessage
import com.evoshot.core.handler.message.PingMessage
import com.evoshot.core.handler.message.PlayerInputMessage
import com.evoshot.core.player.PlayerState
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JsonMessageCodecTest {
    private val codec = JsonMessageCodec()

    @Test
    fun `PingMessage를 인코딩하고 디코딩할 수 있다`() {
        val original = PingMessage(timestamp = 12345L)

        val json = codec.encodeToString(original)
        val decoded = codec.decodeFromString(json)

        assertThat(decoded).isInstanceOf(PingMessage::class.java)
        assertThat((decoded as PingMessage).timestamp).isEqualTo(12345L)
    }

    @Test
    fun `ConnectMessage를 인코딩하고 디코딩할 수 있다`() {
        val original = ConnectMessage(playerName = "TestPlayer")

        val json = codec.encodeToString(original)
        val decoded = codec.decodeFromString(json)

        assertThat(decoded).isInstanceOf(ConnectMessage::class.java)
        assertThat((decoded as ConnectMessage).playerName).isEqualTo("TestPlayer")
    }

    @Test
    fun `PlayerInputMessage를 인코딩하고 디코딩할 수 있다`() {
        val original = PlayerInputMessage(mouseX = 100.5f, mouseY = 200.5f)

        val json = codec.encodeToString(original)
        val decoded = codec.decodeFromString(json)

        assertThat(decoded).isInstanceOf(PlayerInputMessage::class.java)
        val input = decoded as PlayerInputMessage
        assertThat(input.mouseX).isEqualTo(100.5f)
        assertThat(input.mouseY).isEqualTo(200.5f)
    }

    @Test
    fun `GameStateMessage를 인코딩하고 디코딩할 수 있다`() {
        val players =
            listOf(
                PlayerState(id = "1", name = "Player1", x = 10f, y = 20f),
                PlayerState(id = "2", name = "Player2", x = 30f, y = 40f),
            )
        val original = GameStateMessage(tick = 100L, players = players)

        val json = codec.encodeToString(original)
        val decoded = codec.decodeFromString(json)

        assertThat(decoded).isInstanceOf(GameStateMessage::class.java)
        val state = decoded as GameStateMessage
        assertThat(state.tick).isEqualTo(100L)
        assertThat(state.players).hasSize(2)
        assertThat(state.players[0].name).isEqualTo("Player1")
    }
}

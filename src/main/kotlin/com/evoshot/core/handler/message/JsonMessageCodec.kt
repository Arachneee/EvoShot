package com.evoshot.core.handler.message

import kotlinx.serialization.json.Json

class JsonMessageCodec : MessageCodec {
    private val json =
        Json {
            classDiscriminator = "type"
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    override fun encode(message: GameMessage): ByteArray = encodeToString(message).toByteArray(Charsets.UTF_8)

    override fun decode(data: ByteArray): GameMessage = decodeFromString(data.toString(Charsets.UTF_8))

    override fun encodeToString(message: GameMessage): String = json.encodeToString(GameMessage.serializer(), message)

    override fun decodeFromString(data: String): GameMessage = json.decodeFromString(GameMessage.serializer(), data)
}

package com.evoshot.core.controller.message

interface MessageCodec {
    fun encode(message: GameMessage): ByteArray

    fun decode(data: ByteArray): GameMessage

    fun encodeToString(message: GameMessage): String

    fun decodeFromString(data: String): GameMessage
}

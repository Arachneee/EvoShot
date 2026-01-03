package com.evoshot.infra.server

import com.evoshot.core.room.Room
import com.evoshot.infra.codec.MessageCodec
import com.evoshot.infra.handler.WebSocketFrameHandler
import com.evoshot.infra.session.SessionManager
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler

class GameServerInitializer(
    private val sessionManager: SessionManager,
    private val room: Room,
    private val codec: MessageCodec,
    private val websocketPath: String = "/ws",
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        ch.pipeline().apply {
            addLast(HttpServerCodec())
            addLast(HttpObjectAggregator(65536))
            addLast(WebSocketServerCompressionHandler())
            addLast(WebSocketServerProtocolHandler(websocketPath, null, true, 65536))
            addLast(WebSocketFrameHandler(sessionManager, room, codec))
        }
    }
}


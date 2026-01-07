package com.evoshot.network.server

import com.evoshot.network.handler.MessageHandler
import com.evoshot.network.handler.WebSocketFrameHandler
import com.evoshot.network.session.SessionManager
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler

class GameServerInitializer(
    private val sessionManager: SessionManager,
    private val messageHandler: MessageHandler,
    private val websocketPath: String = "/ws",
) : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
        ch.pipeline().apply {
            addLast(HttpServerCodec())
            addLast(HttpObjectAggregator(65536))
            addLast(WebSocketServerCompressionHandler())
            addLast(WebSocketServerProtocolHandler(websocketPath, null, true, 65536))
            addLast(WebSocketFrameHandler(sessionManager, messageHandler))
        }
    }
}

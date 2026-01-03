package com.evoshot.infra.handler

import com.evoshot.core.handler.GameMessageHandler
import com.evoshot.core.room.Room
import com.evoshot.infra.codec.MessageCodec
import com.evoshot.infra.session.SessionManager
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import org.slf4j.LoggerFactory

class WebSocketFrameHandler(
    private val sessionManager: SessionManager,
    private val room: Room,
    private val codec: MessageCodec
) : SimpleChannelInboundHandler<WebSocketFrame>() {

    private val logger = LoggerFactory.getLogger(WebSocketFrameHandler::class.java)
    private val messageHandler = GameMessageHandler(room)

    override fun channelActive(ctx: ChannelHandlerContext) {
        val session = sessionManager.createSession(ctx.channel())
        logger.info("New connection: playerId=${session.playerId}, total=${sessionManager.sessionCount}")
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        val session = sessionManager.removeSession(ctx.channel())
        session?.let {
            room.leave(it)
            logger.info("Connection closed: playerId=${it.playerId}, total=${sessionManager.sessionCount}")
        }
    }

    override fun channelRead0(ctx: ChannelHandlerContext, frame: WebSocketFrame) {
        when (frame) {
            is TextWebSocketFrame -> handleTextFrame(ctx, frame)
            is PingWebSocketFrame -> ctx.writeAndFlush(PongWebSocketFrame(frame.content().retain()))
            is CloseWebSocketFrame -> ctx.close()
            else -> {}
        }
    }

    private fun handleTextFrame(ctx: ChannelHandlerContext, frame: TextWebSocketFrame) {
        val session = sessionManager.getSession(ctx.channel()) ?: return

        try {
            val message = codec.decodeFromString(frame.text())
            messageHandler.handle(session, message)
        } catch (e: Exception) {
            logger.error("Failed to decode message: ${e.message}")
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.error("WebSocket error: ${cause.message}")
        ctx.close()
    }
}

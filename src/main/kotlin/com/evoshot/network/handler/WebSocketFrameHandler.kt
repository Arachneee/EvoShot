package com.evoshot.network.handler

import com.evoshot.network.session.SessionManager
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
    private val messageHandler: MessageHandler,
) : SimpleChannelInboundHandler<WebSocketFrame>() {
    private val logger = LoggerFactory.getLogger(WebSocketFrameHandler::class.java)

    override fun channelActive(ctx: ChannelHandlerContext) {
        val session = sessionManager.createSession(ctx.channel())
        logger.info("New connection: sessionId=${session.id}, total=${sessionManager.sessionCount}")
        messageHandler.onConnect(session.id)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        val session = sessionManager.removeSession(ctx.channel())
        session?.let {
            messageHandler.onDisconnect(it.id)
            logger.info("Connection closed: sessionId=${it.id}, total=${sessionManager.sessionCount}")
        }
    }

    override fun channelRead0(
        ctx: ChannelHandlerContext,
        frame: WebSocketFrame,
    ) {
        when (frame) {
            is TextWebSocketFrame -> {
                handleTextFrame(ctx, frame)
            }

            is PingWebSocketFrame -> {
                ctx.writeAndFlush(PongWebSocketFrame(frame.content().retain()))
            }

            is CloseWebSocketFrame -> {
                ctx.close()
            }

            else -> {}
        }
    }

    private fun handleTextFrame(
        ctx: ChannelHandlerContext,
        frame: TextWebSocketFrame,
    ) {
        val session = sessionManager.getSession(ctx.channel()) ?: return
        messageHandler.onMessage(session.id, frame.text())
    }

    override fun exceptionCaught(
        ctx: ChannelHandlerContext,
        cause: Throwable,
    ) {
        logger.error("WebSocket error: ${cause.message}")
        ctx.close()
    }
}

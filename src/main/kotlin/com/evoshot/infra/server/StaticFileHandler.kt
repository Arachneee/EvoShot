package com.evoshot.infra.server

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion

class StaticFileHandler : SimpleChannelInboundHandler<FullHttpRequest>() {

    override fun channelRead0(ctx: ChannelHandlerContext, request: FullHttpRequest) {
        val uri = request.uri()

        if (uri == "/" || uri == "/index.html" || uri == "/test-client.html") {
            serveTestClient(ctx)
        } else {
            sendNotFound(ctx)
        }
    }

    private fun serveTestClient(ctx: ChannelHandlerContext) {
        val resourcePath = "static/test-client.html"
        val inputStream = javaClass.classLoader.getResourceAsStream(resourcePath)

        if (inputStream == null) {
            sendNotFound(ctx)
            return
        }

        val content = inputStream.use { it.readAllBytes() }
        val response = DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.OK,
            Unpooled.wrappedBuffer(content)
        )

        response.headers().apply {
            set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8")
            set(HttpHeaderNames.CONTENT_LENGTH, content.size)
        }

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
    }

    private fun sendNotFound(ctx: ChannelHandlerContext) {
        val content = "404 Not Found".toByteArray()
        val response = DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.NOT_FOUND,
            Unpooled.wrappedBuffer(content)
        )

        response.headers().apply {
            set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
            set(HttpHeaderNames.CONTENT_LENGTH, content.size)
        }

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }
}

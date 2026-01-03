package com.evoshot.infra.server

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec

class StaticFileServerInitializer : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        ch.pipeline().apply {
            addLast(HttpServerCodec())
            addLast(HttpObjectAggregator(65536))
            addLast(StaticFileHandler())
        }
    }
}

